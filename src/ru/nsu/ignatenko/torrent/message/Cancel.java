package ru.nsu.ignatenko.torrent.message;

public class Cancel  extends Message
{
    public Cancel(int length, byte[] peerID)
    {
        this.length = length;
    }
}
