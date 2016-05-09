package ru.nsu.ignatenko.torrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConnectionManager implements Runnable
{
    private static final int MIN_PORT = 6881;
    private static final int MAX_PORT = 6889;

    ServerSocket serverSocket;
    MessageManager messageManager = new MessageManager();

    TorrentInfo torrentInfo_;
    int[] peerId_;
    String[] ip_;
    int[] port_;

    ArrayDeque<Pair> readyReadQueue_;
    ArrayDeque<Integer> mustReadQueue_;

    ArrayDeque<Pair> mustWriteQueue_;
    ArrayDeque<Integer> readyWriteQueue_;
    Map<byte[], Socket> clients;

    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS, new BlockingQueue<Runnable>);

    boolean stop = false;

    public ConnectionManager()
    {
        for(int port = MIN_PORT; port <= MAX_PORT; ++port)
        {
            try
            {
                serverSocket = new ServerSocket(port);
            }
            catch(IOException e)
            {
                continue;
            }
            break;
        }
        if(serverSocket == null)
        {
            throw new RuntimeException("No available port to listen.");
        }
    }

    public void startListen()
    {
        Thread thread = new Thread(this);
        thread.run();
    }

    public void connectTo(String ip, int port, TorrentInfo torrentInfo)
    {
        try
        {
            Socket client = new Socket(ip, port);
            InputStream input = client.getInputStream();
            OutputStream output = client.getOutputStream();

            messageManager.sendHandshake(output);
            Handshake clientHandshake = messageManager.receiveHandshake(input);
            if(!messageManager.isValidHandshake(clientHandshake))
            {
                client.close();
                System.out.println("Can't connect to this socket. Ip: " + ip + " Port: " + port);
            }
            addConnectedPeer(clientHandshake.getPeerID(), client);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        while(!stop)
        {
            try
            {
                Socket client = serverSocket.accept();
                InputStream input = client.getInputStream();
                OutputStream output = client.getOutputStream();
                Handshake clientHandshake = messageManager.receiveHandshake(input);
                if(!messageManager.isValidHandshake(clientHandshake))
                {
                    client.close();
                    continue;
                }
                messageManager.sendHandshake(output);
                addConnectedPeer(clientHandshake.getPeerID(), client);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void addConnectedPeer(byte[] peerID, Socket client)
    {

        clients.put(peerID, client);

    }

    class Downloader implements Runnable
    {
        Socket client;
        int pieceIdx;

        public Downloader(Socket client, int pieceIdx)
        {
            this.client = client;
            this.pieceIdx = pieceIdx;
        }

        @Override
        public void run()
        {
            try
            {
                InputStream input = client.getInputStream();
                OutputStream output = client.getOutputStream();

                messageManager.sendRequest(output, idx);
                byte[] piece = messageManager.recievePiece(input);
                makeRequestToWrite(pieceIdx, piece);

            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void download(TorrentInfo torrentInfo)
    {
        int idx;
        int piecesCount = torrentInfo.getPiecesCount();
        BitSet downloadPieces = new BitSet(piecesCount);
        boolean isDownloaded = false;

        while(!isDownloaded)
        {
            for (Map.Entry<byte[], Socket> entry : clients.entrySet())
            {
                try
                {
                    Socket client = entry.getValue();
                    InputStream input = client.getInputStream();
                    OutputStream output = client.getOutputStream();
                    byte[] clientPieceMask = messageManager.getBitFieldMessage(input);

                    for (idx = 0; idx < piecesCount; ++idx)
                    {
                        if (!downloadPieces.get(idx) && clientPieceMask[idx] == 1)
                        {
                            Downloader download = new Downloader(client, idx);
                            threadPool.execute(download);
                            downloadPieces.set(idx);

                            break;
                        }
                    }
                    if (idx == piecesCount)
                    {
                        // say to this client that we aren't interested in him. ??
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
            checkResponseToWriteRequest();
            for (idx = 0; idx < piecesCount; ++idx)
            {
                if(!downloadPieces.get(idx))
                    break;
            }
            if(idx == piecesCount)
                isDownloaded = true;
        }
    }

    public void sentMessage()
    {

    }

    public void checkResponseToWriteRequest()
    {
        if(!readyWriteQueue_.isEmpty())
        {
            sentMessage();
        }
    }

    public void checkResponseToReadRequest()
    {
        if(!readyReadQueue_.isEmpty())
        {
            sendPiece();
        }
    }

    public void makeRequestToWrite(int idx, byte[] piece)
    {
        mustWriteQueue_.addLast(new Pair(idx, piece));
    }

    public void makeRequestToRead(int idx)
    {
        mustReadQueue_.addLast((Integer)idx);
    }

    public void setConnectionsWithWriter(ArrayDeque<Pair> mustWriteQueue, ArrayDeque<Integer> readyWriteQueue)
    {
        mustWriteQueue_ = mustWriteQueue;
        readyWriteQueue_ = readyWriteQueue;
    }

    public void setConnectionsWithReader(ArrayDeque<Integer> mustReadQueue, ArrayDeque<Pair> readyReadQueue)
    {
        mustReadQueue_ = mustReadQueue;
        readyReadQueue_ = readyReadQueue;
    }
}
