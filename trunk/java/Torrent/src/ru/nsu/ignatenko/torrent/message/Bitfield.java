package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.message.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Bitfield  extends Message
{
    byte[] bitfield;

    public Bitfield(int length, byte[] peerID)
    {
        this.length = length;
        bitfield = new byte[this.length];
        this.peerID = peerID;
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
