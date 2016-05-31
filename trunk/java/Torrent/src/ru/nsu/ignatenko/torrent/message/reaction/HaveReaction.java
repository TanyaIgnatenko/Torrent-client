package ru.nsu.ignatenko.torrent.message.reaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.message.Have;
import ru.nsu.ignatenko.torrent.message.Message;

import java.util.BitSet;

public class HaveReaction  extends Reaction
{
    private static Logger logger = LogManager.getLogger("default_logger");

    @Override
    public void react(Message message)
    {
        Have message_ = (Have) message;
        int pieceIdx = message_.getPieceIdx();
        BitSet bitfield = message.getPeer().getBitfield();
        bitfield.set(pieceIdx);
    }
}
