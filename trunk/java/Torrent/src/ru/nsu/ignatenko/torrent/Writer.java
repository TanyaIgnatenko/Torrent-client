package ru.nsu.ignatenko.torrent;


import java.io.*;
import java.util.ArrayDeque;

public class Writer implements Runnable
{
    RandomAccessFile file;
    int fileLength_;
    int pieceLength_;
    int piecesCount_;
    int bitPieceMask[];
    byte piece [][];

    ArrayDeque<Pair> piecesQueue;
    ArrayDeque<Integer> changesQueue;

    public Writer(String filename, String path, int fileLength, int pieceLength, int piecesCount)
    {
        fileLength_ = fileLength;
        pieceLength_ = pieceLength;
        piecesCount_ = piecesCount;
        bitPieceMask = new int[piecesCount/32 + 1];
        piece = new byte[piecesCount][pieceLength];
        piecesQueue = new ArrayDeque<>[piecesCount];
        changesQueue = new ArrayDeque<>[piecesCount];

        try
        {
            file = new RandomAccessFile(new File(path), "w");
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
            while (piecesQueue.isEmpty())
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
            Pair pieceInfo = piecesQueue.pollFirst();
            write(pieceInfo.first(), pieceInfo.second());
        }
    }

    public void write(int idx, byte[] piece)
    {
        try
        {
            file.seek(idx*pieceLength_);
            file.write(piece);
            changesQueue.addLast((Integer)idx);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public ArrayDeque<Pair> getMustWriteQueue(){return piecesQueue;}
    public ArrayDeque<Integer> getReadyWriteQueue(){return changesQueue;}
}
