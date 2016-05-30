package ru.nsu.ignatenko.torrent;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Writer implements Runnable
{
    private static Logger logger = LogManager.getLogger("default_logger");
    RandomAccessFile file;
    int fileLength;
    int pieceLength;
    int piecesCount;
    byte piece[][];
    private boolean stop;

    BlockingQueue<Pair2> piecesQueue = new LinkedBlockingQueue<>();
    BlockingQueue<Integer> changesQueue = new LinkedBlockingQueue<>();

    public Writer()
    {
    }

    public void initiate(String filename, String path, int fileLength, int pieceLength, int piecesCount)
    {
        this.fileLength = fileLength;
        this.pieceLength = pieceLength;
        this.piecesCount = piecesCount;
        piece = new byte[piecesCount][pieceLength];
        try
        {
            file = new RandomAccessFile(new File(path), "rw");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        Thread thread = new Thread(this, "Writer");
        thread.start();
    }

    @Override
    public void run()
    {
        while (!stop)
        {
            Pair2 pieceInfo = null;
            synchronized (piecesQueue)
            {
                while (piecesQueue.isEmpty())
                {
                    try
                    {
                        piecesQueue.wait();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                try
                {
                    pieceInfo = piecesQueue.take();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            write(pieceInfo.first, pieceInfo.second);
        }
    }

    public void write(int idx, byte[] piece)
    {
        try
        {
            file.seek(idx * pieceLength);
            file.write(piece);
            try
            {
                changesQueue.put(idx);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            logger.info("Wrote a piece");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void stop()
    {
        try
        {
            stop = true;
            if(file != null)
            {
                file.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public BlockingQueue<Pair2> getMustWriteQueue()
    {
        return piecesQueue;
    }

    public BlockingQueue<Integer> getReadyWriteQueue()
    {
        return changesQueue;
    }
}
