package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

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
        idx = data.getInt();
    }

    public int getPieceIdx(){return idx;}
}
