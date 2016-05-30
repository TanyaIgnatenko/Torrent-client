package ru.nsu.ignatenko.torrent.message;


import ru.nsu.ignatenko.torrent.Peer;

public class Uninterested  extends Message
{

    public Uninterested(int length, Peer peer)
    {
        this.length = length;
        this.peer = peer;
    }
}
