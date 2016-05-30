package ru.nsu.ignatenko.torrent;

import java.nio.channels.SocketChannel;

public class Pair2
{
    public int first;
    public byte[] second;

    public Pair2(){}
    public Pair2(int first, byte[] second)
    {
        this.first = first;
        this.second = second;
    }
}
