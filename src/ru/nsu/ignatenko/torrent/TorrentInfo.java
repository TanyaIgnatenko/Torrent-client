package ru.nsu.ignatenko.torrent;


import java.util.Arrays;

public class TorrentInfo
{
    private final static int HASH_LENGTH = 20;

    String filename;
    long fileLength;
    int pieceLength;
    byte[] piecesHash;
    byte[] handshakeHash;

    public byte[] getPieceHash(int pieceIdx)
    {
        byte[] pieceHash = new byte[HASH_LENGTH];
        System.arraycopy(piecesHash, pieceIdx * HASH_LENGTH, pieceHash, 0, HASH_LENGTH);
        return pieceHash;
    }

    public String getFilename() {return filename;}

    public long getFileLength() {return  fileLength;}

    public int getPieceLength() {return  pieceLength;}

    public int getPiecesCount()
    {
        int piecesCount;
        if(fileLength%pieceLength == 0)
        {
            piecesCount = (int)fileLength/pieceLength;
        }
        else
        {
            piecesCount = (int)fileLength/pieceLength +1;
        }
        return  piecesCount;
    }

    public byte[] getHandshakeHash(){return handshakeHash;}

    public void setPiecesHash(byte[] piecesHash)
    {

        this.piecesHash = Arrays.copyOf(piecesHash, piecesHash.length);
    }

    public void setHandshakeHash(byte[] handshakeHash)
    {
        this.handshakeHash = Arrays.copyOf(handshakeHash, handshakeHash.length);
    }

    public void setFileLength(long fileLength)
    {
        this.fileLength = fileLength;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public void setPieceLength(int pieceLength)
    {
        this.pieceLength = pieceLength;
    }
}
