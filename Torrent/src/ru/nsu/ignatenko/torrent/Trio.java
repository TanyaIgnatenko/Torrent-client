package ru.nsu.ignatenko.torrent;

import java.nio.channels.SocketChannel;

public class Trio
{
    public int first;
    public byte[] second;
    public SocketChannel third;

    public Trio(){}
    public Trio(int first, byte[] second, SocketChannel third)
    {
        this.first = first;
        this.second = new byte[second.length];
        System.arraycopy(second, 0, this.second, 0, second.length);
        this.third = third;
    }
}
