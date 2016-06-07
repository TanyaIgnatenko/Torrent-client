package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Piece  extends Message
{
    private int length;
    private int pieceIdx;
    private byte[] piece;

    public Piece(int length, Peer peer)
    {
        this.length = length;
        piece = new byte[length-4];
        this.peer = peer;
    }

    public void parse(SocketChannel channel) throws IOException
    {
        int count = 0;
        ByteBuffer idx = ByteBuffer.allocate(4);
        ByteBuffer pieceTmp = ByteBuffer.allocate(length - 4);
        while (idx.hasRemaining())
        {
            count+= channel.read(idx);
        }
        while (pieceTmp.hasRemaining())
        {
            count += channel.read(pieceTmp);
        }
        idx.rewind();
        pieceIdx = idx.getInt();
        pieceTmp.rewind();
        pieceTmp.get(piece);
        if (count != length)
        {
            throw new EOFException();
        }
    }

    public int getPieceIdx()
    {
        return pieceIdx;
    }

    public byte[] getPiece()
    {
        return piece;
    }
}
