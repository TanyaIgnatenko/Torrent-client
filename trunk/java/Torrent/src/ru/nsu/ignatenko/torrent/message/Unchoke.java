package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

public class Unchoke  extends Message
{
    public Unchoke(int length, Peer peer)
    {
        this.length = length;
        this.peer = peer;
    }
}
