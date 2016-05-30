package ru.nsu.ignatenko.torrent.message;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;

public abstract class Message
{
    protected byte[] payload;
    protected int id;
    protected int length;
    protected byte[] peerID;

    public byte[] getPeerID()
    {
        return peerID;
    }
    public byte[] getPayload(){return payload;}
    public void parse(SocketChannel socket){}
    public void setLength(int length){this.length = length;}
    public int getLength(){return length;}
    public int getId()
    {
        return id;
    }
}
