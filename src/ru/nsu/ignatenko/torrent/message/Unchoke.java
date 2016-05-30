package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.message.Message;

public class Unchoke  extends Message
{
    public Unchoke(int length, byte[] peerID)
    {
        this.length = length;
    }
}
