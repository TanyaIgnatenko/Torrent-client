package ru.nsu.ignatenko.torrent.message.reaction;

import ru.nsu.ignatenko.torrent.Pair;
import ru.nsu.ignatenko.torrent.TorrentInfo;
import ru.nsu.ignatenko.torrent.message.Message;
import ru.nsu.ignatenko.torrent.message.Piece;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class PieceReaction extends Reaction
{
    private BlockingQueue<Pair<Integer, byte[]>> mustWriteQueue;
    private TorrentInfo torrentInfo;

    public PieceReaction(BlockingQueue<Pair<Integer, byte[]>> mustWriteQueue, TorrentInfo torrentInfo)
    {
        this.mustWriteQueue = mustWriteQueue;
        this.torrentInfo = torrentInfo;
    }
    public void react(Message msg)
    {
        Piece message = (Piece) msg;
        int pieceIdx = message.getPieceIdx();
        byte[] piece = message.getPiece();
        try
        {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            md.update(piece);
            if(Arrays.equals(md.digest(), torrentInfo.getPieceHash(pieceIdx)))
            {
                mustWriteQueue.put(new Pair<>(pieceIdx, Arrays.copyOf(piece, piece.length)));
            }
            message.getPeer().decreaseNumDoneRequests();
        }
        catch (Exception e)
        {
            System.out.println("Error: Can't react on a piece.");
        }
    }
}

