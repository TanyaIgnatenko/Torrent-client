package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

public class Interested  extends Message
{
    public Interested(int length, Peer peer)
    {
        this.length = length;
        this.peer = peer;
    }
}
