package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Piece  extends Message
{
    private final static int PIECE_IDX_SIZE = 4;
    private int length;
    private int pieceIdx;
    private byte[] piece;

    public Piece(int length, Peer peer)
    {
        this.length = length;
        piece = new byte[length - PIECE_IDX_SIZE];
        this.peer = peer;
    }

    public void parse(SocketChannel channel) throws IOException
    {
        int count = 0;
        ByteBuffer idx = ByteBuffer.allocate(PIECE_IDX_SIZE);
        ByteBuffer pieceTmp = ByteBuffer.allocate(length - PIECE_IDX_SIZE);
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
