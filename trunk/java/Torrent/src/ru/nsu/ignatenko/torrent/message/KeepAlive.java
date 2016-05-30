package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.message.Message;

public class KeepAlive extends Message
{
    public KeepAlive(int length, byte[] peerID)
    {
        this.length = length;
    }
}
