package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.message.Message;

public class Interested  extends Message
{
    public Interested(int length, byte[] peerID)
    {
        this.length = length;
    }
}
