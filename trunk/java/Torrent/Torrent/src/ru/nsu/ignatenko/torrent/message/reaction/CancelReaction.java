package ru.nsu.ignatenko.torrent.message.reaction;

import ru.nsu.ignatenko.torrent.Pair;
import ru.nsu.ignatenko.torrent.Peer;
import ru.nsu.ignatenko.torrent.message.Message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class CancelReaction  extends Reaction
{
    private BlockingQueue<Pair> mustReadQueue;
    private ConcurrentMap<byte[],Peer> connectedPeers;

    public CancelReaction(ConcurrentMap<byte[],Peer> connectedPeers, BlockingQueue<Pair> mustReadQueue)
    {
        this.connectedPeers = connectedPeers;
        this.mustReadQueue = mustReadQueue;
    }

    public void react(Message message, SocketChannel socket)
    {
        ByteBuffer buf = ByteBuffer.allocate(message.getLength());
        buf.put(message.getPayload());
        Integer pieceIdx = buf.getInt();
        mustReadQueue.remove(pieceIdx);
    }
}
