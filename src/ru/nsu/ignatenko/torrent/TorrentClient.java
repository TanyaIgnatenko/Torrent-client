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
import ru.nsu.ignatenko.torrent.exceptions.PortNotFoundException;
import ru.nsu.ignatenko.torrent.exceptions.SelectorException;
import ru.nsu.ignatenko.torrent.message.reaction.*;

public class TorrentClient
{
    private static Logger logger = LogManager.getLogger("default_logger");
    private final static int  MAX_NUM_PEERS = 10;
    private ConnectionManager connectionManager;
    private MessageManager messageManager;
    private Coordinator coordinator;
    private Reader reader;
    private Writer writer;
    private InteractorWithUser userInteractor;
    private BlockingQueue<Peer> connectedPeers;
    private BlockingQueue<Peer> newConnectedPeers;
    private BlockingQueue<Peer> peers;
    private TorrentInfo torrentInfo;
    private Peer ourPeer;
    private  boolean stop;

    public TorrentClient()
    {
        connectedPeers = new LinkedBlockingQueue<>();
        newConnectedPeers = new LinkedBlockingQueue<>();
        torrentInfo = new TorrentInfo();
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
        messageReactions.put((byte)7, new PieceReaction(writer.getMustWriteQueue(), torrentInfo));
        messageReactions.put((byte)8, new CancelReaction(reader.getMustReadQueue()));
        messageManager = new MessageManager(messageReactions);
    }

    public void execute() throws PortNotFoundException, SelectorException, IOException
    {
        userInteractor = new InteractorWithUser(this, peers);
        PeerBehaviour ourPeerBehaviour = userInteractor.getInfoAboutOurPeer();

        ourPeer = new Peer();
        if (ourPeerBehaviour.isCreator())
        {
            String pathToFile = ourPeerBehaviour.getPathToFile();
            String pathToTorrent = ourPeerBehaviour.getPathToTorrent();
            try
            {
                createTorrent(pathToFile, pathToTorrent);
            }
            catch (IOException e)
            {
                System.out.println("Can't create torrent file");
                return;
            }
        }
        else
        {
            String pathToTorrent = ourPeerBehaviour.getPathToTorrent();
            try(DataInputStream torrentFile = new DataInputStream(new FileInputStream(pathToTorrent)))
            {
                Bencoder.parseTorrent(torrentFile, torrentInfo);
            }
            catch (IOException e)
            {
                System.out.println("Can't parse torrent file");
                return;
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
                String pathToFile = ourPeerBehaviour.getPathToFile() ;
                BitSet bitfield = new BitSet(torrentInfo.getPiecesCount());
                ourPeer.setBitfield(bitfield);
                start(torrentInfo, pathToFile);

                Thread thread = new Thread(userInteractor);
                thread.start();

                while(!stop)
                {
                    try
                    {
                        Peer peerToConnect = peers.take();
                        connectionManager.connectTo(peerToConnect);

                        synchronized (connectedPeers)
                        {
                            while (connectedPeers.size() == MAX_NUM_PEERS)
                            {
                                connectedPeers.wait();
                            }
                        }
                    }
                    catch (InterruptedException e)
                    {
                       logger.info("It never happens");
                    }
                }
            }
        }
    }

    public void createTorrent(String pathToFile, String pathToTorrent) throws IOException
    {
        Bencoder.generateTorrent(pathToFile, pathToTorrent);
    }

    public void start(TorrentInfo torrentInfo,
                           String pathToFile) throws PortNotFoundException, SelectorException, IOException
    {

        if(ourPeer.isLeecher())
        {
            writer.initiate(torrentInfo.getFilename(),
                    pathToFile,
                    torrentInfo.getFileLength(),
                    torrentInfo.getPieceLength(),
                    torrentInfo.getPiecesCount());
        }
        reader.initiate(torrentInfo.getFilename(),
                pathToFile,
                torrentInfo.getFileLength(),
                torrentInfo.getPieceLength(),
                torrentInfo.getPiecesCount());
        coordinator = new Coordinator(connectedPeers,
                newConnectedPeers,
                messageManager,
                writer.getReadyWriteQueue(),
                reader.getReadyReadQueue(),
                ourPeer, writer, torrentInfo, this, userInteractor);

        connectionManager = new ConnectionManager(messageManager,
                                                  connectedPeers,
                                                  newConnectedPeers,
                                                  ourPeer,
                                                  torrentInfo);
        
        connectionManager.processIncomingConnectionsAndMessages();
        coordinator.start();
        reader.start();
        writer.start();
    }

    public void stop()
    {
        connectionManager.stop();
        coordinator.stop();
        if(writer != null)
        {
            writer.stop();
        }
        reader.stop();
        stop = true;
    }

    public Peer getOurPeer()
    {
        return ourPeer;
    }

    public static void main(String[] args)
    {
        TorrentClient client = new TorrentClient();
        try
        {
            client.execute();
        }
        catch (PortNotFoundException | SelectorException | IOException e)
        {
            System.out.println("Some problem happened. For more information look in a log file. Torrent client is terminated.");
        }
    }
}
