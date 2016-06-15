package ru.nsu.ignatenko.torrent;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ru.nsu.ignatenko.torrent.exceptions.PortNotFoundException;
import ru.nsu.ignatenko.torrent.exceptions.SelectorException;
import ru.nsu.ignatenko.torrent.message.reaction.*;

public class TorrentClient
{
    private final static int  MAX_NUM_PEERS = 10;

    private ConnectionManager connectionManager;
    private InteractorWithUser userInteractor;
    private MessageManager messageManager;
    private Coordinator coordinator;
    private Reader reader;
    private Writer writer;

    private Peer ourPeer;
    private BlockingQueue<Peer> peers;
    private BlockingQueue<Peer> connectedPeers;
    private BlockingQueue<Peer> newConnectedPeers;
    private TorrentInfo torrentInfo;
    private Thread mainThread;
    private  boolean stop;

    public TorrentClient()
    {
        ourPeer = new Peer();
        mainThread = Thread.currentThread();
        peers = new LinkedBlockingQueue<>();
        connectedPeers = new LinkedBlockingQueue<>();
        newConnectedPeers = new LinkedBlockingQueue<>();
        userInteractor = new InteractorWithUser(this, peers);
        torrentInfo = new TorrentInfo();
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
        messageReactions.put((byte)7, new PieceReaction(writer.getMustWriteQueue(), torrentInfo, ourPeer));
        messageReactions.put((byte)8, new CancelReaction(reader.getMustReadQueue()));
        messageManager = new MessageManager(messageReactions);
    }

    public void execute() throws PortNotFoundException, SelectorException, IOException
    {
        PeerBehaviour ourPeerBehaviour = userInteractor.getInfoAboutOurPeer();

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
//                userInteractor.waitStopCommand();
            }
            else if(ourPeerBehaviour.isLeecher())
            {
                ourPeer.setLeecher(true);
                String pathToFile = ourPeerBehaviour.getPathToFile() ;
                BitSet bitfield = new BitSet(torrentInfo.getPiecesCount());
                ourPeer.setBitfield(bitfield);
                ourPeer.setCountPieces(torrentInfo.getPiecesCount());
                start(torrentInfo, pathToFile);

                Thread thread = new Thread(userInteractor);
                thread.start();

                try
                {
                    while(!stop)
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
                }
                catch (InterruptedException e) {}
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
            writer.initiate(pathToFile, torrentInfo.getPieceLength());
        }

        reader.initiate(pathToFile,
                        torrentInfo.getFileLength(),
                        torrentInfo.getPieceLength(),
                        torrentInfo.getPiecesCount());

        coordinator = new Coordinator(connectedPeers,
                                      newConnectedPeers,
                                      messageManager,
                                      writer.getReadyWriteQueue(),
                                      reader.getReadyReadQueue(),
                                      ourPeer, writer, torrentInfo, userInteractor);

        connectionManager = new ConnectionManager(messageManager,
                                                  connectedPeers,
                                                  newConnectedPeers,
                                                  ourPeer,
                                                  torrentInfo);
        
        connectionManager.processIncomingConnectionsAndMessages();
        coordinator.start();
        reader.start();
        if(ourPeer.isLeecher())
        {
            writer.start();
        }
    }

    public void stop()
    {
        stop = true;
        connectionManager.stop();
        coordinator.stop();
        if(ourPeer.isLeecher())
        {
            writer.stop();
        }
        reader.stop();
        mainThread.interrupt();
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
