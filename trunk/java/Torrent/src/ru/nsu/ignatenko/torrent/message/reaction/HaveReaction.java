package ru.nsu.ignatenko.torrent.message.reaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.message.Message;

public class HaveReaction  extends Reaction
{
    private static Logger logger = LogManager.getLogger("default_logger");

    @Override
    public void react(Message message)
    {
        logger.info("We did some react on have message.");
    }
}
