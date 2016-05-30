package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.message.Message;

public class Choke extends Message
{
    public Choke(int length, byte[] peerID)
    {
        this.length = length;
    }
}
