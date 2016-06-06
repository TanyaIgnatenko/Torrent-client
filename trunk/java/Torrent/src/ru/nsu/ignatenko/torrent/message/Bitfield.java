package ru.nsu.ignatenko.torrent.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.Peer;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Bitfield extends Message
{
    private static Logger logger = LogManager.getLogger("default_logger");
    byte[] bitfield;

    public Bitfield(int length, Peer peer)
    {
        this.length = length;
        bitfield = new byte[this.length];
        this.peer = peer;
    }

    public void parse(SocketChannel channel) throws IOException
    {
        int count = 0;
        ByteBuffer data = ByteBuffer.allocate(length);
        while (data.hasRemaining())
        {
            count += channel.read(data);
        }
        if (count == -1 || count != length)
        {
            throw new EOFException();
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
