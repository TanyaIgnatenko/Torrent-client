package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Have  extends Message
{
    private int idx;

    public Have(int length, Peer peer)
    {
        this.length = length;
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
        idx = data.getInt();
    }

    public int getPieceIdx(){return idx;}
}
