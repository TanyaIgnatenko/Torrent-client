package ru.nsu.ignatenko.torrent;

public class TorrentInfo
{
    String filename;
    String path_;
    long fileLength;
    int pieceLength;
    byte[][] pieceHash;

    public byte[] getPieceHash(int pieceIdx)
    {
        return pieceHash[pieceIdx];
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

    public byte[] getHandshakeHash(){return new byte[20];}

    public void setPiecesHash(byte[][] infoHash)
    {
        this.pieceHash = infoHash;
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
}
