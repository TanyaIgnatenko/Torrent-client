package ru.nsu.ignatenko.torrent.message.reaction;

import ru.nsu.ignatenko.torrent.Pair;
import ru.nsu.ignatenko.torrent.message.Cancel;
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
        Cancel msg = (Cancel)message;
        Integer pieceIdx = msg.getPieceIdx();
        mustReadQueue.remove(new Pair<>(pieceIdx, message.getPeer().getChannel()));
    }
}
