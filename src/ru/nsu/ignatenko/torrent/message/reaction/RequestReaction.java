package ru.nsu.ignatenko.torrent.message.reaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.Pair;
import ru.nsu.ignatenko.torrent.Peer;
import ru.nsu.ignatenko.torrent.message.Message;
import ru.nsu.ignatenko.torrent.message.Piece;
import ru.nsu.ignatenko.torrent.message.Request;

import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class RequestReaction  extends Reaction
{
    private static Logger logger = LogManager.getLogger("default_logger");
    private ConcurrentMap<byte[],Peer> connectedPeers;
    private BlockingQueue<Pair> mustReadQueue;

    public RequestReaction(ConcurrentMap<byte[],Peer> connectedPeers, BlockingQueue<Pair> mustReadQueue)
    {
        this.connectedPeers = connectedPeers;
        this.mustReadQueue = mustReadQueue;
    }

    public void react(Message message, SocketChannel socket)
    {
        synchronized (mustReadQueue)
        {
            Request message_ = (Request) message;
            int pieceIdx = message_.getPieceIdx();
            Pair pair = new Pair(pieceIdx, message_.getSocket());
            try
            {
                mustReadQueue.put(pair);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            mustReadQueue.notify();
        }
    }
}
