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

    public synchronized  byte[] getPieceHash(int pieceIdx)
    {
        byte[] pieceHash = new byte[HASH_LENGTH];
        System.arraycopy(piecesHash, pieceIdx * HASH_LENGTH, pieceHash, 0, HASH_LENGTH);
        return pieceHash;
    }

    public synchronized  long getFileLength() {return  fileLength;}

    public synchronized  int getPieceLength() {return  pieceLength;}

    public synchronized  String getFilename() {return filename;}

    public synchronized  int getPiecesCount()
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

    public synchronized  byte[] getHandshakeHash(){return handshakeHash;}

    public synchronized  void setPiecesHash(byte[] piecesHash)
    {

        this.piecesHash = Arrays.copyOf(piecesHash, piecesHash.length);
    }

    public synchronized  void setHandshakeHash(byte[] handshakeHash)
    {
        this.handshakeHash = Arrays.copyOf(handshakeHash, handshakeHash.length);
    }

    public synchronized  void setFileLength(long fileLength)
    {
        this.fileLength = fileLength;
    }

    public synchronized  void setFilename(String filename)
    {
        this.filename = filename;
    }

    public synchronized  void setPieceLength(int pieceLength)
    {
        this.pieceLength = pieceLength;
    }
}
