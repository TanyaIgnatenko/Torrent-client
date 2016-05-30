package ru.nsu.ignatenko.torrent.message;

import com.sun.media.sound.InvalidFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Have  extends Message
{
    private int idx;

    public Have(int length, byte[] peerID)
    {
        this.length = length;
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
}
