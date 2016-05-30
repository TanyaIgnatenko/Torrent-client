package ru.nsu.ignatenko.torrent.message.reaction;

import ru.nsu.ignatenko.torrent.Pair;
import ru.nsu.ignatenko.torrent.Peer;
import ru.nsu.ignatenko.torrent.message.Message;
import ru.nsu.ignatenko.torrent.message.Piece;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class PieceReaction extends Reaction
{
    private BlockingQueue<Peer> connectedPeers;
    private BlockingQueue<Pair<Integer, byte[]>> mustWriteQueue;

    public PieceReaction(BlockingQueue<Pair<Integer, byte[]>> mustWriteQueue)
    {
        this.mustWriteQueue = mustWriteQueue;
    }

    public void react(Message msg)
    {
        Piece message = (Piece) msg;
        int pieceIdx = message.getPieceIdx();
        byte[] piece = message.getPiece();
        try
        {
            mustWriteQueue.put(new Pair<>(pieceIdx, Arrays.copyOf(piece, piece.length)));
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}

