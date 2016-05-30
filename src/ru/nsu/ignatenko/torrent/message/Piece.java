package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.message.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Piece  extends Message
{
    int length;
    int pieceIdx;
    byte[] piece;
    byte[] peerID;

    public Piece(int length, byte[] peerID)
    {
        this.length = length;
        piece = new byte[length-4];
        this.peerID = peerID;
    }

    public void parse(SocketChannel socket)
    {
        ByteBuffer idx =  ByteBuffer.allocate(4);
        ByteBuffer pieceTmp =  ByteBuffer.allocate(length - 4);
        try
        {
            socket.read(idx);
            socket.read(pieceTmp);
            idx.rewind();
            pieceIdx = idx.getInt();
            pieceTmp.rewind();
            pieceTmp.get(piece);
        }
        catch(IOException e)
        {
            e.printStackTrace();
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
