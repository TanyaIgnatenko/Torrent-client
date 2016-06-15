package ru.nsu.ignatenko.torrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

public class Coordinator implements Runnable
{
    private static Logger logger = LogManager.getLogger("default_logger");
    private final static int MAX_NUM_REQUESTS = 4;
    private BlockingQueue<Trio<Integer, byte[], SocketChannel>> readyReadQueue;
    private BlockingQueue<Integer> readyWriteQueue;
    private BlockingQueue<Peer> newConnectedPeers;
    private BlockingQueue<Peer> connectedPeers;
    private TorrentInfo torrentInfo;
    private Thread thread;
    private Peer ourPeer;
    boolean has_all = false;
    boolean stop = false;

    private Writer writer;
    private InteractorWithUser interactorWithUser;
    private MessageManager messageManager;

    public Coordinator(BlockingQueue<Peer> connectedPeers, BlockingQueue<Peer> newConnectedPeers, MessageManager messageManager,
                       BlockingQueue<Integer>readyWriteQueue,
                       BlockingQueue<Trio<Integer, byte[], SocketChannel>> readyReadQueue,
                       Peer ourPeer, Writer writer, TorrentInfo torrentInfo, InteractorWithUser interactorWithUser)
    {
        this.readyReadQueue = readyReadQueue;
        this.readyWriteQueue = readyWriteQueue;
        this.messageManager = messageManager;
        this.connectedPeers = connectedPeers;
        this.newConnectedPeers = newConnectedPeers;
        this.interactorWithUser = interactorWithUser;
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

                ByteBuffer msg = messageManager.generateUnchoke();
                messageManager.sendMessage(newPeer.getChannel(), msg);
            }


            if (ourPeer.isLeecher())
            {
                Integer pieceIdx = readyWriteQueue.poll();
                if (pieceIdx != null)
                {
                    ourPeer.setHavePiece(pieceIdx);
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

                for (int i = 0; i < torrentInfo.getPiecesCount(); ++i)
                {
                    if (!ourPeer.havePiece(i))
                    {
                        has_all = false;

                        if(!ourPeer.isAskedPiece(i))
                        {
                            for (Peer peer : connectedPeers)
                            {
                                if(!peer.isChokedMe() && peer.getNumDoneRequestsForTime() <= MAX_NUM_REQUESTS)
                                {
                                    if (peer.havePiece(i))
                                    {
                                        ByteBuffer message = messageManager.generateRequest(i);
                                        messageManager.sendMessage(peer.getChannel(), message);
                                        peer.increaseNumDoneRequests();
                                        ourPeer.setAskedPiece(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (has_all)
                {
                    logger.info("have all pieces!");
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
