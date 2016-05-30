package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

public class KeepAlive extends Message
{
    public KeepAlive(int length, Peer peer)
    {
        this.length = length;
        this.peer = peer;
    }
}
