package ru.nsu.ignatenko.torrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class Reader implements Runnable
{
    private static Logger logger = LogManager.getLogger("default_logger");
    private RandomAccessFile file;
    private int fileLength;
    private int pieceLength;
    private int piecesCount;
    private int lastPieceLength;
    private byte piece[];
    private byte lastPiece[];
    private boolean stop;

    private BlockingQueue<Trio<Integer, byte[], SocketChannel>> readyQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Pair<Integer, SocketChannel>> mustReadQueue = new LinkedBlockingQueue<>();

    public Reader()
    {
    }

    public void initiate(String filename, String path, int fileLength, int pieceLength, int piecesCount)
    {
        this.fileLength = fileLength;
        this.pieceLength = pieceLength;
        this.piecesCount = piecesCount;
        piece = new byte[pieceLength];
        if(fileLength % pieceLength != 0)
        {
            lastPieceLength = fileLength % pieceLength;
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
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            logger.info("Error: source file is missing.");
        }
    }

    public void start()
    {
        stop = false;
        Thread thread = new Thread(this, "Reader");
        thread.start();
    }

    public void stop()
    {
        stop = true;
        try
        {
            file.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        while (true)
        {
            Pair<Integer, SocketChannel> data = null;
            try
            {
                data = mustReadQueue.take();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
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
//                logger.info("Piece is read : "+ new String(piece)+" "+piece.toString());
                try
                {
                    readyQueue.put(new Trio<>(idx, Arrays.copyOf(piece, piece.length), socket));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                file.read(lastPiece);
//                logger.info("LastPiece is read : "+new String(lastPiece));
                try
                {
                    readyQueue.put(new Trio<>(idx, Arrays.copyOf(lastPiece, lastPiece.length), socket));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                logger.info("Read a last piece");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
