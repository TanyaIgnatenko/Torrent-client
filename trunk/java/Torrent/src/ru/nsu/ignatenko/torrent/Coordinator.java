package ru.nsu.ignatenko.torrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.BitSet;
import java.util.concurrent.BlockingQueue;

public class Coordinator implements Runnable
{
    private static Logger logger = LogManager.getLogger("default_logger");
    private final static int MAX_NUM_REQUESTS = 4;
    private Writer writer;
    private Reader reader;
    private BlockingQueue<Trio<Integer, byte[], SocketChannel>> readyReadQueue;
    private BlockingQueue<Integer> readyWriteQueue;

    private ConnectionManager connectionManager;
    private MessageManager messageManager;
    private InteractorWithUser interactorWithUser;
    private TorrentClient torrentClient;


    private Thread thread;
    private BlockingQueue<Peer> connectedPeers;
    private BlockingQueue<Peer> newConnectedPeers;
    private TorrentInfo torrentInfo;
    private Peer ourPeer;
    boolean has_all = false;
    boolean stop = false;

    public Coordinator(BlockingQueue<Peer> connectedPeers, BlockingQueue<Peer> newConnectedPeers, MessageManager messageManager,
                       BlockingQueue<Integer>readyWriteQueue,
                       BlockingQueue<Trio<Integer, byte[], SocketChannel>> readyReadQueue,
                       Peer ourPeer, Writer writer, TorrentInfo torrentInfo, TorrentClient torrentClient,
                       InteractorWithUser interactorWithUser)
    {
        this.readyReadQueue = readyReadQueue;
        this.readyWriteQueue = readyWriteQueue;
        this.messageManager = messageManager;
        this.connectedPeers = connectedPeers;
        this.newConnectedPeers = newConnectedPeers;
        this.interactorWithUser = interactorWithUser;
        this.torrentClient = torrentClient;
        this.torrentInfo = torrentInfo;
        this.ourPeer = ourPeer;
        this.writer = writer;
    }

    public void start()
    {
        thread = new Thread(this, "Coordinator");
        thread.start();
    }

    @Override
    public void run()
    {
        boolean numAskedPieces[] = new boolean[torrentInfo.getPiecesCount()];
        synchronized (newConnectedPeers)
        {
                try
                {
                    while(newConnectedPeers.isEmpty())
                    {
                        newConnectedPeers.wait();
                    }
                }
                catch (InterruptedException e) {}
        }
        while (!stop)
        {
            Peer newPeer = newConnectedPeers.poll();
            if(newPeer != null)
            {
                ByteBuffer message = messageManager.generateBitfield(ourPeer.getBitfield(), torrentInfo.getPiecesCount());
                messageManager.sendMessage(newPeer.getChannel(), message);
                newPeer.setHasOurBitfield();
            }

            if (ourPeer.isLeecher())
            {
                Integer pieceIdx = readyWriteQueue.poll();
                if (pieceIdx != null)
                {
                    BitSet bitfield = ourPeer.getBitfield();
                    bitfield.set(pieceIdx);
                    ByteBuffer message = messageManager.generateHave(pieceIdx);
                    for (Peer peer : connectedPeers)
                    {
                        messageManager.sendMessage(peer.getChannel(), message);
                        message.flip();
                    }
                }
            }

            Trio<Integer, byte[], SocketChannel> data = readyReadQueue.poll();
            if (data != null)
            {
                ByteBuffer message = messageManager.generatePiece(data.second, data.first);
                messageManager.sendMessage(data.third, message);
            }

            if (!stop && ourPeer.isLeecher())
            {
                has_all = true;

                BitSet bitfieldOfPeer;
                BitSet bitfield = ourPeer.getBitfield();
                for (int i = 0; i < torrentInfo.getPiecesCount(); ++i)
                {
                    if (!bitfield.get(i))
                    {
                        has_all = false;
                        if(!numAskedPieces[i])
                        {
                            for (Peer peer : connectedPeers)
                            {
                                if(!peer.isChokedMe() && peer.getNumDoneRequestsForTime() <= MAX_NUM_REQUESTS)
                                {
                                    bitfieldOfPeer = peer.getBitfield();
                                    if (bitfieldOfPeer != null && bitfieldOfPeer.get(i))
                                    {
                                        ByteBuffer message = messageManager.generateRequest(i);
                                        messageManager.sendMessage(peer.getChannel(), message);
                                        peer.increaseNumDoneRequests();
                                        numAskedPieces[i] = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (has_all)
                {
                    logger.info("has all pieces!");
                    stop = true;
                    writer.stop();
                    interactorWithUser.printStatistics(connectedPeers);
                }
            }
        }
    }

    public void stop()
    {
        stop = true;
        thread.interrupt();
    }
}
