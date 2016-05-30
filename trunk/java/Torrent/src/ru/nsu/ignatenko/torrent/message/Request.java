package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Request  extends Message
{
    private int length;
    private int pieceIdx;

    public Request(int length, Peer peer)
    {
        this.length = length;
        this.peer = peer;
    }

    public void parse(SocketChannel socket)
    {
        ByteBuffer idx = ByteBuffer.allocate(4);
        try
        {
            socket.read(idx);
            idx.rewind();
            pieceIdx = idx.getInt();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public int getPieceIdx()
    {
        return pieceIdx;
    }
}
