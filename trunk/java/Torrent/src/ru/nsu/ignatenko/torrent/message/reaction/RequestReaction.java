package ru.nsu.ignatenko.torrent.message.reaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.Pair;
import ru.nsu.ignatenko.torrent.message.Message;
import ru.nsu.ignatenko.torrent.message.Request;

import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

public class RequestReaction extends Reaction
{
    private static Logger logger = LogManager.getLogger("default_logger");
    private BlockingQueue<Pair<Integer, SocketChannel>> mustReadQueue;

    public RequestReaction(BlockingQueue<Pair<Integer, SocketChannel>> mustReadQueue)
    {
        this.mustReadQueue = mustReadQueue;
    }

    public void react(Message message)
    {
        Request message_ = (Request) message;
        int pieceIdx = message_.getPieceIdx();
        Pair<Integer, SocketChannel> pair = new Pair<>(pieceIdx, message.getPeer().getChannel());
        try
        {
            mustReadQueue.put(pair);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
