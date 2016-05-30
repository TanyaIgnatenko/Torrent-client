package ru.nsu.ignatenko.torrent;

import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ru.nsu.ignatenko.torrent.message.reaction.*;

public class TorrentClient
{
    private final static int  MAX_NUM_PEERS = 10;
    private ConnectionManager connectionManager;
    private MessageManager messageManager;
    private Coordinator coordinator;
    private Reader reader;
    private Writer writer;

    private BlockingQueue<Peer> connectedPeers;

    public void download(String[] args)
    {
        String pathToTorrent = args[0];
        DataInputStream torrentFile = new DataInputStream(
                ClassLoader.getSystemClassLoader().getResourceAsStream(pathToTorrent));

        TorrentInfo torrentInfo = Bencoder.parser(torrentFile);
        String pathToFile = args[1] + torrentInfo.getFilename();
        torrentInfo.setPath(pathToFile);

        Peer ourPeer = new Peer();
        ourPeer.setPeerID("12345678901234567891".getBytes(Charset.forName("ASCII")));

        BitSet bitfield = new BitSet(torrentInfo.getPiecesCount());
        ourPeer.setBitfield(bitfield);
        Peer[] peers = getPeersInfo();

        writer.initiate(torrentInfo.getFilename(),
                        torrentInfo.getPath(),
                        torrentInfo.getFileLength(),
                        torrentInfo.getPieceLength(),
                        torrentInfo.getPiecesCount());

        reader.initiate(torrentInfo.getFilename(),
                        torrentInfo.getPath(),
                        torrentInfo.getFileLength(),
                        torrentInfo.getPieceLength(),
                        torrentInfo.getPiecesCount());

        coordinator = new Coordinator(connectedPeers,
                                      messageManager,
                                      writer.getReadyWriteQueue(),
                                      reader.getReadyReadQueue(),
                                      ourPeer, writer, torrentInfo);

        connectionManager = new ConnectionManager(messageManager, connectedPeers, ourPeer, torrentInfo);
        connectionManager.workSelector();
        connectionManager.startListen();
        coordinator.start();
        reader.start();
        writer.start();

        for (Peer peer : peers)
        {
            if(connectedPeers.size() != MAX_NUM_PEERS)
            {
                connectionManager.connectTo(peer);
            }
            else
            {
                break;
            }
        }
    }

    public void load(String[] args)
    {
        String pathToTorrent = args[2];
        DataInputStream torrentFile = new DataInputStream(
                ClassLoader.getSystemClassLoader().getResourceAsStream(pathToTorrent));
        TorrentInfo torrentInfo = Bencoder.parser(torrentFile);

        String pathToFile = args[3] + torrentInfo.getFilename();
        torrentInfo.setPath(pathToFile);

        Peer ourPeer = new Peer();
        ourPeer.setPeerID("12345678901234567890".getBytes(Charset.forName("ASCII")));
        BitSet bitfield = new BitSet(torrentInfo.getPiecesCount());
        for(int i = 0; i < torrentInfo.getPiecesCount(); ++i)
        {
            bitfield.set(i);
        }
        ourPeer.setBitfield(bitfield);


        writer.initiate(torrentInfo.getFilename(),
                torrentInfo.getPath(),
                torrentInfo.getFileLength(),
                torrentInfo.getPieceLength(),
                torrentInfo.getPiecesCount());

        reader.initiate(torrentInfo.getFilename(),
                        torrentInfo.getPath(),
                        torrentInfo.getFileLength(),
                        torrentInfo.getPieceLength(),
                        torrentInfo.getPiecesCount());

        coordinator = new Coordinator(connectedPeers,
                                      messageManager,
                                      writer.getReadyWriteQueue(),
                                      reader.getReadyReadQueue(),
                                      ourPeer, writer, torrentInfo);

        connectionManager = new ConnectionManager(messageManager, connectedPeers, ourPeer, torrentInfo);
        connectionManager.workSelector();
        connectionManager.startListen();
        coordinator.start();
        reader.start();
    }

    private Peer[] getPeersInfo()
    {
//        System.out.println("Print how much peers has this torrent:");
//        Scanner scan = new Scanner(System.in);
//        int size = scan.nextInt();
//        Peer peers[] =  new Peer[size];
//
//        byte[] peerID;
//        String tmp;
//        int port;
//
//        for (int i = 0; i < size; ++i)
//        {
//            peers[i] = new Peer();
//            System.out.println("Print peerID № " + (i+1) + " : " );
//            peerID = scan.next().getBytes(Charset.forName("ASCII"));
//            peers[i].setPeerID(peerID);
//
//            System.out.println("Print ip № " + (i+1) + " : " );
//            tmp = scan.next();
//            InetAddress ip = null;
//            try
//            {
//                ip = InetAddress.getByName(tmp);
//            }
//            catch (UnknownHostException e)
//            {
//                e.printStackTrace();
//            }
//            peers[i].setIp(ip);
//
//            System.out.println("Print port № " + (i+1) + " : " );
//            port = scan.nextInt();
//            peers[i].setPort(port);
//        }
        Peer[] peers = new Peer[1];
        peers[0] = new Peer();
        peers[0].setPeerID("12345678901234567890".getBytes(Charset.forName("ASCII")));
        try
        {
            peers[0].setIp(InetAddress.getByName("127.0.0.1"));
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        peers[0].setPort(6881);
        return peers;
    }

    public TorrentClient()
    {
        connectedPeers = new LinkedBlockingQueue<>();
        reader = new Reader();
        writer = new Writer();

        HashMap<Byte, Reaction> messageReactions = new HashMap<>();
        messageReactions.put((byte)-1, new KeepAliveReaction());
        messageReactions.put((byte)0, new ChokeReaction());
        messageReactions.put((byte)1, new UnchokeReaction());
        messageReactions.put((byte)2, new InterestedReaction());
        messageReactions.put((byte)3, new UninterestedReaction());
        messageReactions.put((byte)4, new HaveReaction());
        messageReactions.put((byte)5, new BitfieldReaction());
        messageReactions.put((byte)6, new RequestReaction(reader.getMustReadQueue()));
        messageReactions.put((byte)7, new PieceReaction(writer.getMustWriteQueue()));
        messageReactions.put((byte)8, new CancelReaction(reader.getMustReadQueue()));
        messageManager = new MessageManager(messageReactions);
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
