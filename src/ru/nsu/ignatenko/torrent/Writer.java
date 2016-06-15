package ru.nsu.ignatenko.torrent;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Writer implements Runnable
{
    private static Logger logger = LogManager.getLogger("default_logger");
    private RandomAccessFile file;
    private int pieceLength;
    private boolean stop;
    private Thread thread;

    private BlockingQueue<Pair<Integer, byte[]>> mustWriteQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Integer> readyWriteQueue = new LinkedBlockingQueue<>();

    public Writer()
    {
    }

    public void initiate(String path, int pieceLength) throws IOException
    {
        this.pieceLength = pieceLength;
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
        thread = new Thread(this, "Writer");
        thread.start();
    }

    @Override
    public void run()
    {
        try
        {
            while (!stop)
            {
                Pair<Integer, byte[]> pieceInfo = null;
                pieceInfo = mustWriteQueue.take();
                write(pieceInfo.first, pieceInfo.second);
            }
        }
        catch(InterruptedException e) {}
    }

    public void write(int idx, byte[] piece)
    {
        try
        {
            file.seek(idx * pieceLength);
            file.write(piece);
            try
            {
                readyWriteQueue.put(idx);
            }
            catch(InterruptedException e){}
        }
        catch (IOException e)
        {
            logger.info("Error: Can't write a piece with idx {} to file." + idx);
        }
    }

    public void stop()
    {
        try
        {
            stop = true;
            thread.interrupt();
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
        return mustWriteQueue;
    }

    public BlockingQueue<Integer> getReadyWriteQueue()
    {
        return readyWriteQueue;
    }
}
