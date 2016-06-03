package ru.nsu.ignatenko.torrent;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.message.reaction.*;

public class TorrentClient
{
    private final static int  MAX_NUM_PEERS = 10;
    private ConnectionManager connectionManager;
    private MessageManager messageManager;
    private Coordinator coordinator;
    private Reader reader;
    private Writer writer;
    private InteractorWithUser userInteractor;
    private BlockingQueue<Peer> connectedPeers;
    private BlockingQueue<Peer> peers;
    private Peer ourPeer;
    private  boolean stop;
    private static Logger logger = LogManager.getLogger("default_logger");
    
    public TorrentClient()
    {
        connectedPeers = new LinkedBlockingQueue<>();
        peers = new LinkedBlockingQueue<>();
        reader = new Reader();
        writer = new Writer();

        HashMap<Byte, Reaction> messageReactions = new HashMap<>();
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
    public void createTorrent(String pathToFile) {}

    public void execute()
    {
        userInteractor = new InteractorWithUser(this, peers);
        PeerBehaviour ourPeerBehaviour = userInteractor.getInfoAboutOurPeer();

        ourPeer = new Peer();
        if (ourPeerBehaviour.isCreator())
        {
            String pathToFile = ourPeerBehaviour.getPathToFile();
            createTorrent(pathToFile);
        }
        else
        {
            TorrentInfo torrentInfo = null;
            String pathToTorrent = ourPeerBehaviour.getPathToTorrent();
            try(DataInputStream torrentFile = new DataInputStream(new FileInputStream(pathToTorrent)))
            {
                torrentInfo = Bencoder.parse(torrentFile);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            if (ourPeerBehaviour.isSeeder())
            {
                ourPeer.setSeeder(true);
                String pathToFile = ourPeerBehaviour.getPathToFile();
                BitSet bitfield = new BitSet(torrentInfo.getPiecesCount());
                for(int i = 0; i < torrentInfo.getPiecesCount(); ++i)
                {
                    bitfield.set(i);
                }
                ourPeer.setBitfield(bitfield);
                start(torrentInfo, pathToFile);
            }
            else if(ourPeerBehaviour.isLeecher())
            {
                ourPeer.setLeecher(true);
                String pathToFile = ourPeerBehaviour.getPathToDownloadDir() + torrentInfo.getFilename() ;
                BitSet bitfield = new BitSet(torrentInfo.getPiecesCount());
                ourPeer.setBitfield(bitfield);
                start(torrentInfo, pathToFile);
                userInteractor.run();
                while(!stop)
                {
                    try
                    {
                        synchronized (connectedPeers)
                        {
                            while (connectedPeers.size() == MAX_NUM_PEERS)
                            {
                                connectedPeers.wait();
                            }
                            connectionManager.connectTo(peers.take());
                        }
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    
    public void start(TorrentInfo torrentInfo, String pathToFile)
    {
        writer.initiate(torrentInfo.getFilename(),
                pathToFile,
                torrentInfo.getFileLength(),
                torrentInfo.getPieceLength(),
                torrentInfo.getPiecesCount());

        reader.initiate(torrentInfo.getFilename(),
                pathToFile,
                torrentInfo.getFileLength(),
                torrentInfo.getPieceLength(),
                torrentInfo.getPiecesCount());

        coordinator = new Coordinator(connectedPeers,
                messageManager,
                writer.getReadyWriteQueue(),
                reader.getReadyReadQueue(),
                ourPeer, writer, torrentInfo, this, userInteractor);

        connectionManager = new ConnectionManager(messageManager, connectedPeers, ourPeer, torrentInfo);
        connectionManager.processIncomingConnections();
        connectionManager.selectChannelsWithIncomingMessages();
        coordinator.start();
        reader.start();
        writer.start();
    }

    public void stop()
    {
        for(Peer peer: connectedPeers)
        {
            try
            {
                peer.getSocket().close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public Peer getOurPeer()
    {
        return ourPeer;
    }

    public static void main(String[] args)
    {
        TorrentClient client = new TorrentClient();
        client.execute();
    }
}
