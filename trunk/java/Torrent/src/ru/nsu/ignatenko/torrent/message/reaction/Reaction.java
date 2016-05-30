package ru.nsu.ignatenko.torrent.message.reaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.message.Message;

import java.nio.channels.SocketChannel;

public abstract class Reaction
{
    public void react(Message message, SocketChannel socket){}
}
