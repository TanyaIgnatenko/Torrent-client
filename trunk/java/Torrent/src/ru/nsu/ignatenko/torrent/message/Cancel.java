package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

public class Cancel  extends Message
{
    public Cancel(int length, Peer peer)
    {
        this.length = length;
        this.peer = peer;
    }
}
