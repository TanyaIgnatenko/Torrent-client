package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.message.Message;

public class Uninterested  extends Message
{
    public Uninterested(int length, byte[] peerID)
    {
        this.length = length;
    }
}
