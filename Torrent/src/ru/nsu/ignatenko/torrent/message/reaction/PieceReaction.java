package ru.nsu.ignatenko.torrent.message.reaction;

import ru.nsu.ignatenko.torrent.MessageManager;
import ru.nsu.ignatenko.torrent.Pair;
import ru.nsu.ignatenko.torrent.Pair2;
import ru.nsu.ignatenko.torrent.Peer;
import ru.nsu.ignatenko.torrent.message.Message;
import ru.nsu.ignatenko.torrent.message.Piece;

import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.BitSet;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class PieceReaction extends Reaction
{
    private ConcurrentMap<byte[],Peer> connectedPeers;
    private BlockingQueue<Pair2> mustWriteQueue;

    public PieceReaction(ConcurrentMap<byte[],Peer> connectedPeers, BlockingQueue<Pair2> mustWriteQueue)
    {
        this.connectedPeers = connectedPeers;
        this.mustWriteQueue = mustWriteQueue;
    }

    public void react(Message msg, SocketChannel socket)
    {
        Piece message = (Piece) msg;
        synchronized (mustWriteQueue)
        {
            int pieceIdx = message.getPieceIdx();
            byte[] piece = message.getPiece();
            try
            {
                mustWriteQueue.put(new Pair2(pieceIdx, piece));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            mustWriteQueue.notify();
        }
    }
}
