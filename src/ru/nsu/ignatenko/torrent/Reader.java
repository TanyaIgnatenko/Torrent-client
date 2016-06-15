package ru.nsu.ignatenko.torrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Reader implements Runnable
{
    private static Logger logger = LogManager.getLogger("default_logger");
    private RandomAccessFile file;
    private int pieceLength;
    private int piecesCount;
    private int lastPieceLength;
    private byte piece[];
    private byte lastPiece[];
    private boolean stop;
    private Thread thread;

    private BlockingQueue<Trio<Integer, byte[], SocketChannel>> readyQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Pair<Integer, SocketChannel>> mustReadQueue = new LinkedBlockingQueue<>();


    public void initiate(String path, long fileLength,
                         int pieceLength, int piecesCount) throws FileNotFoundException
    {
        this.pieceLength = pieceLength;
        this.piecesCount = piecesCount;
        piece = new byte[pieceLength];
        if (fileLength % pieceLength != 0)
        {
            lastPieceLength = (int)(fileLength % pieceLength);
        }
        else
        {
            lastPieceLength = pieceLength;
        }
        lastPiece = new byte[lastPieceLength];
        try
        {
            File tmp = new File(path);
            file = new RandomAccessFile(tmp, "r");
        }
        catch (FileNotFoundException e)
        {
            logger.info("Error: Seeder's reader can't find file.");
            throw e;
        }
    }

    public void start()
    {
        stop = false;
        thread = new Thread(this, "Reader");
        thread.start();
    }

    public void stop()
    {
        try
        {
            stop = true;
            thread.interrupt();
            file.close();
        }
        catch (IOException e)
        {
            logger.info("Error: Can't close file.");
        }
    }

    @Override
    public void run()
    {
        while (!stop)
        {
            Pair<Integer, SocketChannel> data = null;
            try
            {
                data = mustReadQueue.take();
            }
            catch (InterruptedException e)
            {
                return;
            }
            read(data);
        }
    }

    public void read(Pair<Integer, SocketChannel> data)
    {
        Integer idx = data.first;
        SocketChannel socket = data.second;
        try
        {
            file.seek(idx * pieceLength);

            if (idx != piecesCount - 1)
            {
                file.read(piece);
                try
                {
                    readyQueue.put(new Trio<>(idx, Arrays.copyOf(piece, piece.length), socket));
                }
                catch (InterruptedException e){}
            }
            else
            {
                file.read(lastPiece);
                try
                {
                    readyQueue.put(new Trio<>(idx, Arrays.copyOf(lastPiece, lastPiece.length), socket));
                }
                catch (InterruptedException e) {}
            }
        }
        catch (IOException e)
        {
            logger.info("Error: Can't read a piece with idx {}." + idx);
        }
    }

    public BlockingQueue<Pair<Integer, SocketChannel>> getMustReadQueue()
    {
        return mustReadQueue;
    }

    public BlockingQueue<Trio<Integer, byte[], SocketChannel>> getReadyReadQueue()
    {
        return readyQueue;
    }
}
