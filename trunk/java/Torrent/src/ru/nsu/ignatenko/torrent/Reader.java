package ru.nsu.ignatenko.torrent;

import java.io.*;
import java.util.ArrayDeque;

public class Reader implements Runnable
{
    RandomAccessFile file;
    int fileLength_;
    int pieceLength_;
    int piecesCount_;
    int lastPieceLength;
    int bitPieceMask[];
    byte piece [];
    byte lastPiece [];

    ArrayDeque<Pair> readyQueue;
    ArrayDeque<Integer> mustReadQueue;

    public Reader(String filename, String path, int fileLength, int pieceLength, int piecesCount)
    {
        fileLength_ = fileLength;
        pieceLength_ = pieceLength;
        piecesCount_ = piecesCount;
        piece = new byte[pieceLength_];
        lastPieceLength = fileLength%pieceLength;
        lastPiece = new byte[lastPieceLength];
        bitPieceMask = new int[piecesCount/32 + 1];
        readyQueue = new ArrayDeque<>[piecesCount];
        mustReadQueue = new ArrayDeque<>[piecesCount];

        try
        {
            file = new RandomAccessFile(new File(path), "r");
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run()
    {
        while(true)
        {
            while (mustReadQueue.isEmpty())
            {
                try
                {
                    wait();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            Integer pieceIdx = mustReadQueue.pollFirst();
            read(pieceIdx);
        }
    }

    public void read(Integer idx)
    {
        try
        {
            file.seek(idx * pieceLength_);

            if(idx != piecesCount_ - 1)
            {
                file.read(piece);
                readyQueue.addLast(new Pair(idx, piece));
            }
            else
            {
                file.read(lastPiece);
                readyQueue.addLast(new Pair(idx, lastPiece));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public ArrayDeque<Integer> getMustReadQueue(){return mustReadQueue;}
    public ArrayDeque<Pair> getReadyReadQueue(){return readyQueue;}
}
