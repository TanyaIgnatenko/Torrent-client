package ru.nsu.ignatenko.torrent.message.reaction;

import ru.nsu.ignatenko.torrent.Pair;
import ru.nsu.ignatenko.torrent.message.Message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

public class CancelReaction  extends Reaction
{
    private BlockingQueue<Pair<Integer, SocketChannel>> mustReadQueue;

    public CancelReaction(BlockingQueue<Pair<Integer, SocketChannel>> mustReadQueue)
    {
        this.mustReadQueue = mustReadQueue;
    }

    public void react(Message message)
    {
        ByteBuffer buf = ByteBuffer.allocate(message.getLength());
        buf.put(message.getPayload());
        Integer pieceIdx = buf.getInt();
        mustReadQueue.remove(new Pair<>(pieceIdx, message.getPeer().getChannel()));
    }
}
