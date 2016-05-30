package ru.nsu.ignatenko.torrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.message.Bitfield;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Coordinator implements Runnable
{
    private static Logger logger = LogManager.getLogger("default_logger");
    private Writer writer;
    private Reader reader;
    private ConnectionManager connectionManager;
    private BlockingQueue<Trio> readyReadQueue;

    private BlockingQueue<Integer> readyWriteQueue;

    private MessageManager messageManager;
    private BlockingQueue<Peer> connectedPeers;
    private TorrentInfo torrentInfo;
    private Peer ourPeer;
    boolean cancel = false;
    boolean has_all = false;
    boolean stop = false;

    public Coordinator(BlockingQueue<Peer> connectedPeers, MessageManager messageManager,
                       BlockingQueue<Integer>readyWriteQueue, BlockingQueue<Trio> readyReadQueue,
                       Peer ourPeer, Writer writer, TorrentInfo torrentInfo)
    {
        this.readyReadQueue = readyReadQueue;
        this.readyWriteQueue = readyWriteQueue;
        this.messageManager = messageManager;
        this.connectedPeers = connectedPeers;
        this.torrentInfo = torrentInfo;
        this.ourPeer = ourPeer;
        this.writer = writer;
    }

    public void start()
    {
        Thread thread = new Thread(this, "Coordinator");
        thread.start();
    }

    @Override
    public void run()
    {
        boolean asked[] = new boolean[torrentInfo.getPiecesCount()];
        while (true)
        {
            for (Peer peer : connectedPeers)
            {
                if (!peer.getHasOurBitfield())
                {
                    logger.info("Coordinator found some peer without our bitfield");
                    ByteBuffer message = messageManager.generateBitfield(ourPeer.getBitfield(), torrentInfo.getPiecesCount());
                    messageManager.sendMessage(peer.getSocket(), message);
                    peer.setHasOurBitfield();
                }
            }
            if (cancel)
            {
                ByteBuffer message = messageManager.generateCancel();
                for (Peer peer : connectedPeers)
                {
                    messageManager.sendMessage(peer.getSocket(), message);
                }
            }

            Integer pieceIdx = readyWriteQueue.poll();
            if(pieceIdx != null)
            {
                ourPeer.setBit(pieceIdx);
//                ByteBuffer message = messageManager.generateHave(pieceIdx);
//                for (Peer peer : connectedPeers.values())
//                {
//                    messageManager.sendMessage(peer.getSocket(), message);
//                }
            }

            Trio data = readyReadQueue.poll();
            if(data != null)
            {
//                logger.info("Coordinator read from readyReadQueue: " + new String(data.second));
                ByteBuffer message = messageManager.generatePiece(data.second, data.first);
                messageManager.sendMessage(data.third, message);
            }

            if (!stop)
            {
                has_all = true;

                BitSet bitfieldOfPeer;
                BitSet bitfield = ourPeer.getBitfield();
                for (int i = 0; i < torrentInfo.getPiecesCount(); ++i)
                {
                    if (!bitfield.get(i))
                    {
                        has_all = false;
                        if(!asked[i])
                        {
                            for (Peer peer : connectedPeers)
                            {
                                bitfieldOfPeer = peer.getBitfield();
                                if (bitfieldOfPeer != null && bitfieldOfPeer.get(i))
                                {
                                    ByteBuffer message = messageManager.generateRequest(i);
                                    messageManager.sendMessage(peer.getSocket(), message);
                                    asked[i] = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (has_all)
                {
                    logger.info("has all pieces!");
                    stop = true;
                    if(writer!=null)
                    {
                        writer.stop();
                    }
                }
            }
        }
    }

    public void setCancel()
    {
        cancel = true;
    }
}