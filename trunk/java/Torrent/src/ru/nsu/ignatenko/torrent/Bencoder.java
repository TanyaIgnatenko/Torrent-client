package ru.nsu.ignatenko.torrent;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Bencoder
{
    private static boolean isDigit(int c)
    {
        return c >= '0' && c <= '9';
    }

    private static long getNextInteger(InputStream is) throws IOException
    {
        int leadingByte = is.read();
        if (leadingByte == -1 || leadingByte != 'i')
        {
            throw new IOException("Error: Wrong torrent file.");
        }
        long number = 0;
        int c;
        while (isDigit(c = is.read()))
        {
            number = number * 10 + (c - '0');
        }
        if(c != 'e')
        {
            throw new IOException("Error: Wrong torrent file.");
        }
        return number;
    }

    private static long getNextNumber(InputStream is) throws IOException
    {
        boolean isNegative = false;
        long number = 0;
        int c = is.read();
        if(c == '-')
        {
            isNegative = true;
        }
        else if(isDigit(c))
        {
            number = number * 10 + (c - '0');
        }
        else
        {
            throw new IOException("Error: Wrong torrent file.");
        }

        while (isDigit(c = is.read()))
        {
            number = number * 10 + (c - '0');
        }
        if(c != ':')
        {
            throw new IOException("Error: Wrong torrent file.");
        }
        return (isNegative ? -number : number);
    }

    public static TorrentInfo parseTorrent(DataInputStream torrentFile)
    {
        TorrentInfo torrentInfo = new TorrentInfo();
        try
        {
            torrentFile.skipBytes(13);
            long length = getNextInteger(torrentFile);
            torrentInfo.setFileLength(length);

            torrentFile.skipBytes(6);
            int n = (int) getNextNumber(torrentFile);
            byte[] name = new byte[n];
            System.out.println(n);
            torrentFile.read(name);
            String filename = new String(name);
            torrentInfo.setFilename(filename);

            torrentFile.skipBytes(14);
            n = (int)getNextInteger(torrentFile);
            System.out.println(n);
            torrentInfo.setPieceLength(n);

            torrentFile.skipBytes(11);
            byte[][] hash = new byte[(int)torrentInfo.getPiecesCount()][20];
            for(byte[] pieceHash : hash)
            {
                torrentFile.read(pieceHash);
            }
            torrentInfo.setPiecesHash(hash);
        }
        catch(IOException e)
        {
            System.out.println("Error: Wrong torrent file.");
        }
        return torrentInfo;
    }

    public static void generateTorrent(String pathToFile)
    {


    }
}

//d4:name8:file.txt12:piece length1:16:pieces40:hash6:length1:2e
