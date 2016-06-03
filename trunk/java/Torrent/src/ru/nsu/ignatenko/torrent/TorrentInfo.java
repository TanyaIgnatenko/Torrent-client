package ru.nsu.ignatenko.torrent;

import java.util.Collection;

public class TorrentInfo
{
    String filename;
    String path_;
    long fileLength;
    int pieceLength;
    byte[] piecesHash;
    byte[] handshakeHash;

    public byte[] getPieceHash(int pieceIdx)
    {
        byte[] pieceHash = new byte[20];
        System.arraycopy(piecesHash, pieceIdx * 20, pieceHash, 0, 20);
        return pieceHash;
    }

    public String getFilename(){return filename;}
    public String getPath(){return path_;}
    public long getFileLength(){return  fileLength;}
    public int getPieceLength(){return  pieceLength;}
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
        this.piecesHash = piecesHash;
    }

    public void setPath(String path){path_ = path;}

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

    public void setHandshakeHash(byte[] hash){handshakeHash = hash;}
}
