package ru.nsu.ignatenko.torrent;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Writer implements Runnable
{
    private static Logger logger = LogManager.getLogger("default_logger");
    RandomAccessFile file;
    long fileLength;
    int pieceLength;
    int piecesCount;
    byte piece[][];
    private boolean stop;

    BlockingQueue<Pair<Integer, byte[]>> piecesQueue = new LinkedBlockingQueue<>();
    BlockingQueue<Integer> changesQueue = new LinkedBlockingQueue<>();

    public Writer()
    {
    }

    public void initiate(String filename, String path, long fileLength, int pieceLength, int piecesCount) throws IOException
    {
        this.fileLength = fileLength;
        this.pieceLength = pieceLength;
        this.piecesCount = piecesCount;
        piece = new byte[piecesCount][pieceLength];
        try
        {
            File tmp = new File(path);
            tmp.createNewFile();
            file = new RandomAccessFile(tmp, "rw");
        }
        catch (IOException e)
        {
            logger.info("Error: Leecher's writer can't open a file.");
            throw e;
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
            Pair<Integer, byte[]> pieceInfo = null;
            try
            {
                pieceInfo = piecesQueue.take();
            }
            catch (InterruptedException e)
            {
                logger.info("It never happens");
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
            catch(InterruptedException e)
            {
                logger.info("It never happens");
            }
        }
        catch (IOException e)
        {
            logger.info("Error: Can't write a piece with id {} to file." + idx);
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
            logger.info("Error: Can't close file.");
        }
    }

    public BlockingQueue<Pair<Integer, byte[]>> getMustWriteQueue()
    {
        return piecesQueue;
    }

    public BlockingQueue<Integer> getReadyWriteQueue()
    {
        return changesQueue;
    }
}
