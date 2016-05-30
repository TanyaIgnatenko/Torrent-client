package ru.nsu.ignatenko.torrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.message.Bitfield;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class Coordinator implements Runnable
{
    private static Logger logger = LogManager.getLogger("default_logger");
    Writer writer;
    private BlockingQueue<Trio> readyReadQueue;

    private BlockingQueue<Integer> readyWriteQueue;

    private MessageManager messageManager;
    ConcurrentMap<byte[], Peer> connectedPeers;
    TorrentInfo torrentInfo;
    Peer ourPeer;
    boolean cancel = false;
    boolean has_all = false;
    boolean stop = false;

    public Coordinator(ConcurrentMap<byte[], Peer> connectedPeers, MessageManager messageManager,
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
            for (Peer peer : connectedPeers.values())
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
                for (Peer peer : connectedPeers.values())
                {
                    messageManager.sendMessage(peer.getSocket(), message);
                }
            }
            if (!readyWriteQueue.isEmpty())
            {
                try
                {
                    int pieceIdx = readyWriteQueue.take();
                    ourPeer.setBit(pieceIdx);
//                    ByteBuffer message = messageManager.generateHave(pieceIdx);
//                    for (Peer peer : connectedPeers.values())
//                    {
//                        messageManager.sendMessage(peer.getSocket(), message);
//                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            if (!readyReadQueue.isEmpty())
            {   try
                {
                    Trio data = readyReadQueue.take();
//                    logger.info("Coordinator read from readyReadQueue: " + new String(data.second));
                    ByteBuffer message = messageManager.generatePiece(data.second, data.first);
                    messageManager.sendMessage(data.third, message);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
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
                            for (Peer peer : connectedPeers.values())
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
