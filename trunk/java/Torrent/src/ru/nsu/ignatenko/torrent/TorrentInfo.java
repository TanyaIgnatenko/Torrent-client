package ru.nsu.ignatenko.torrent;

public class TorrentInfo
{
    String filename;
    String path_;
    int fileLength;
    int pieceLength;
    int piecesCount = fileLength/pieceLength +1;
    Byte[] sha1;

    public String getFilename(){return filename;}
    public String getPath(){return path_;}
    public int getFileLength(){return  fileLength;}
    public int getPieceLength(){return  pieceLength;}
    public int getPiecesCount(){return  piecesCount;}
    public Byte[] getSha1(){return sha1;}

    public void setPath(String path){path_ = path;}
}
