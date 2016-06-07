package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Unchoke  extends Message
{
    public Unchoke(int length, Peer peer)
    {
        this.length = length;
        this.peer = peer;
    }
}
