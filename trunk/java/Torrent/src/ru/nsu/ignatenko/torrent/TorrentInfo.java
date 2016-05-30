package ru.nsu.ignatenko.torrent;

public class TorrentInfo
{
    String filename;
    String path_;
    int fileLength;
    int pieceLength;
    Byte[] sha1;

    public String getFilename(){return filename;}
    public String getPath(){return path_;}
    public int getFileLength(){return  fileLength;}
    public int getPieceLength(){return  pieceLength;}
    public int getPiecesCount()
    {
        int piecesCount;
        if(fileLength%pieceLength == 0)
        {
            piecesCount = fileLength/pieceLength;
        }
        else
        {
            piecesCount = fileLength/pieceLength +1;
        }
        return  piecesCount;
    }
    public Byte[] getSha1(){return sha1;}

    public void setPath(String path){path_ = path;}

    public void setFileLength(int fileLength)
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

    public void setSha1(Byte[] sha1)
    {
        this.sha1 = sha1;
    }
}
