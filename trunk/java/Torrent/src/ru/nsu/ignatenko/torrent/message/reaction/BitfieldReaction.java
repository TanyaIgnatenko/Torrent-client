package ru.nsu.ignatenko.torrent.message.reaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.message.Message;

import java.util.BitSet;

public class BitfieldReaction extends Reaction
{
    private static Logger logger = LogManager.getLogger("default_logger");

    public void react(Message message)
    {
        logger.info("In bitfield react");
        BitSet bitfield = BitSet.valueOf(message.getPayload());
        message.getPeer().setBitfield(bitfield);
        logger.info("We reacted on bitfield");
    }
}
