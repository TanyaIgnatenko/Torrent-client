package ru.nsu.ignatenko.torrent.message.reaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.Pair;
import ru.nsu.ignatenko.torrent.Peer;
import ru.nsu.ignatenko.torrent.TorrentInfo;
import ru.nsu.ignatenko.torrent.message.Message;
import ru.nsu.ignatenko.torrent.message.Piece;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class PieceReaction extends Reaction
{
    private static Logger logger = LogManager.getLogger("default_logger");
    private BlockingQueue<Pair<Integer, byte[]>> mustWriteQueue;
    private TorrentInfo torrentInfo;
    private Peer ourPeer;

    public PieceReaction(BlockingQueue<Pair<Integer, byte[]>> mustWriteQueue, TorrentInfo torrentInfo, Peer ourPeer)
    {
        this.mustWriteQueue = mustWriteQueue;
        this.torrentInfo = torrentInfo;
        this.ourPeer = ourPeer;
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
                logger.info("Piece with idx {} is right", pieceIdx);
                mustWriteQueue.put(new Pair<>(pieceIdx, Arrays.copyOf(piece, piece.length)));
            }
            else
            {
                logger.info("Piece with idx {} is bad", pieceIdx);
                ourPeer.clearAskedPiece(pieceIdx);
            }
            message.getPeer().decreaseNumDoneRequestsForTime();
        }
        catch (Exception e)
        {
            System.out.println("Error: Can't react on a piece.");
        }
    }
}

