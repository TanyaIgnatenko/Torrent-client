package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.message.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Request  extends Message
{
    int length;
    int pieceIdx;
    byte[] peerID;
    private SocketChannel socket;

    public Request(int length, byte[] peerID)
    {
        this.length = length;
        this.peerID = peerID;
    }

    public void parse(SocketChannel socket)
    {
        ByteBuffer idx = ByteBuffer.allocate(4);
        try
        {
            socket.read(idx);
            idx.rewind();
            pieceIdx = idx.getInt();
            this.socket = socket;
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

    public SocketChannel getSocket(){return socket;}
}
