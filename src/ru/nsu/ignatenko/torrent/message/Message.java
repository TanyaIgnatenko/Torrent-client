package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class Message
{
    protected byte[] payload;
    protected int id;
    protected int length;
    protected Peer peer;

    public Peer getPeer()
    {
        return peer;
    }
    public byte[] getPayload(){return payload;}
    public void parse(SocketChannel channel) throws IOException {}
    public void setLength(int length){this.length = length;}
    public int getLength(){return length;}
    public int getId()
    {
        return id;
    }
}
