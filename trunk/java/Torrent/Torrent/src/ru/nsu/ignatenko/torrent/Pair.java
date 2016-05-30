package ru.nsu.ignatenko.torrent;

import java.nio.channels.SocketChannel;

public class Pair
{
    public int first;
    public SocketChannel second;

    public Pair(){}
    public Pair(int first, SocketChannel second)
    {
        this.first = first;
        this.second = second;
    }
}
