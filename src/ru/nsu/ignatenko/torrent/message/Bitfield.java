package ru.nsu.ignatenko.torrent.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.Peer;
import ru.nsu.ignatenko.torrent.message.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Bitfield  extends Message
{
    private static Logger logger = LogManager.getLogger("default_logger");
    byte[] bitfield;

    public Bitfield(int length, Peer peer)
    {
        this.length = length;
        bitfield = new byte[this.length];
        this.peer = peer;
    }

    public void parse(SocketChannel socket)
    {
       ByteBuffer data = ByteBuffer.allocate(length);
       try
       {
           socket.read(data);
       }
       catch(IOException e)
       {
           e.printStackTrace();
       }
       data.rewind();
       data.get(bitfield);
    }

    @Override
    public byte[] getPayload()
    {
        return bitfield;
    }
}
