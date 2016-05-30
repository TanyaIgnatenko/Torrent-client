package ru.nsu.ignatenko.torrent;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ru.nsu.ignatenko.torrent.message.*;
import ru.nsu.ignatenko.torrent.message.reaction.*;

public class TorrentClient
{
    private final static int  MAX_NUM_PEERS = 10;
    ConnectionManager connectionManager;
    MessageManager messageManager;
    Coordinator coordinator;
    Reader reader;
    Writer writer;

    ConcurrentMap<byte[], Peer> connectedPeers = new ConcurrentHashMap<>();

    public void download(String[] args)
    {
        DataInputStream torrentFile = new DataInputStream(
                ClassLoader.getSystemClassLoader().getResourceAsStream(args[0]));

        TorrentInfo torrentInfo = Bencoder.parser();
        String pathTorrentFile = ClassLoader.getSystemClassLoader().getResource(args[0]).getPath();
        String pathToDir = pathTorrentFile.substring(0, pathTorrentFile.length() - args[0].length());
        String pathToWrite = pathToDir + "download/" + torrentInfo.getFilename();
        String pathToRead = pathToDir + torrentInfo.getFilename();
        System.out.println(pathToRead);
        torrentInfo.setPath(pathToDir);

        Peer ourPeer = new Peer();
        BitSet bitfield = new BitSet(torrentInfo.getPiecesCount());
        ourPeer.setBitfield(bitfield);
        Peer[] peers = getPeersInfo();

        writer.initiate(torrentInfo.getFilename(),
                        pathToWrite,
                        torrentInfo.getFileLength(),
                        torrentInfo.getPieceLength(),
                        torrentInfo.getPiecesCount());

//        reader.initiate(torrentInfo.getFilename(),
//                        pathToRead,
//                        torrentInfo.getFileLength(),
//                        torrentInfo.getPieceLength(),
//                        torrentInfo.getPiecesCount());

        coordinator = new Coordinator(connectedPeers,
                                      messageManager,
                                      writer.getReadyWriteQueue(),
                                      reader.getReadyReadQueue(),
                                      ourPeer, writer, torrentInfo);
        writer.start();
        reader.start();
        connectionManager.startListen();
        coordinator.start();
        connectionManager.workSelector();

        for (Peer peer : peers)
        {
            if(connectedPeers.size() != MAX_NUM_PEERS)
            {
                connectionManager.connectTo(peer, torrentInfo);
            }
            else
            {
                break;
            }
        }
    }

    public void load(String[] args)
    {

        Peer ourPeer = new Peer();
        ourPeer.setPeerID("12345678901234567890".getBytes(Charset.forName("ASCII")));
        TorrentInfo torrentInfo = Bencoder.parser();
        BitSet bitfield = new BitSet(torrentInfo.getPiecesCount());
        for(int i = 0; i < torrentInfo.getPiecesCount(); ++i)
        {
            bitfield.set(i);
        }
        ourPeer.setBitfield(bitfield);
        String pathTorrentFile = ClassLoader.getSystemClassLoader().getResource(args[0]).getPath();
        String pathToDir = pathTorrentFile.substring(0, pathTorrentFile.length() - args[0].length());
        String pathToWrite = pathToDir + "download/" + torrentInfo.getFilename();
        String pathToRead = pathToDir + torrentInfo.getFilename();

//        writer.initiate(torrentInfo.getFilename(),
//                torrentInfo.getPath(),
//                torrentInfo.getFileLength(),
//                torrentInfo.getPieceLength(),
//                torrentInfo.getPiecesCount());

        reader.initiate(torrentInfo.getFilename(),
                pathToRead,
                torrentInfo.getFileLength(),
                torrentInfo.getPieceLength(),
                torrentInfo.getPiecesCount());

        coordinator = new Coordinator(connectedPeers,
                messageManager,
                writer.getReadyWriteQueue(),
                reader.getReadyReadQueue(),
                ourPeer, writer, torrentInfo);

        reader.start();
        connectionManager.workSelector();
        connectionManager.startListen();
        coordinator.start();
    }

    private Peer[] getPeersInfo()
    {
        System.out.println("Print how much peers has this torrent:");
        Scanner scan = new Scanner(System.in);
        int size = scan.nextInt();
        Peer peers[] =  new Peer[size];

        byte[] peerID;
        String ip;
        int port;

        for (int i = 0; i < size; ++i)
        {
            peers[i] = new Peer();
            System.out.println("Print peerID № " + (i+1) + " : " );
            peerID = scan.next().getBytes(Charset.forName("ASCII"));
            peers[i].setPeerID(peerID);

//            System.out.println("Print ip № " + (i+1) + " : " );
//            ip = scan.next();
//            peers[i].setIp(ip);

            System.out.println("Print port № " + (i+1) + " : " );
            port = scan.nextInt();
            peers[i].setPort(port);
        }
        return peers;
    }

    public TorrentClient()
    {
        connectedPeers = new ConcurrentHashMap<byte[],Peer>();
        reader = new Reader();
        writer = new Writer();

        HashMap<Byte, Reaction> messageReactions = new HashMap<>();
        messageReactions.put((byte)-1, new KeepAliveReaction(connectedPeers));
        messageReactions.put((byte)0, new ChokeReaction(connectedPeers));
        messageReactions.put((byte)1, new UnchokeReaction(connectedPeers));
        messageReactions.put((byte)2, new InterestedReaction(connectedPeers));
        messageReactions.put((byte)3, new UninterestedReaction(connectedPeers));
        messageReactions.put((byte)4, new HaveReaction(connectedPeers));
        messageReactions.put((byte)5, new BitfieldReaction(connectedPeers));
        messageReactions.put((byte)6, new RequestReaction(connectedPeers, reader.getMustReadQueue()));
        messageReactions.put((byte)7, new PieceReaction(connectedPeers, writer.getMustWriteQueue()));
        messageReactions.put((byte)8, new CancelReaction(connectedPeers, reader.getMustReadQueue()));

        messageManager = new MessageManager(messageReactions);
        connectionManager = new ConnectionManager(messageManager,connectedPeers);
    }

    public static void main(String[] args)
    {
        TorrentClient client = new TorrentClient();

        Scanner scan = new Scanner(System.in);
        System.out.println("Do you want to download? -y/n");
        if(scan.next().equals("y"))
        {
            client.download(args);
        }
        else
        {
            client.load(args);
        }
    }
}
