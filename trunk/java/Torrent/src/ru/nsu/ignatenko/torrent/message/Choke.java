package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

public class Choke extends Message
{
    public Choke(int length, Peer peer)
    {
        this.length = length;
        this.peer = peer;
    }
}
