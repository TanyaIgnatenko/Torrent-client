package ru.nsu.ignatenko.torrent.message.reaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.Peer;
import ru.nsu.ignatenko.torrent.message.Message;

import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

public class HaveReaction  extends Reaction
{
    private static Logger logger = LogManager.getLogger("default_logger");
    public HaveReaction(ConcurrentMap<byte[],Peer> connectedPeers)
    {

    }

    public void react(Message message, SocketChannel socket)
    {
        logger.info("We did some react on have message.");
    }
}
