package ru.nsu.ignatenko.torrent;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Bencoder
{
    public static TorrentInfo parseTorrent(DataInputStream torrentFile, TorrentInfo torrentInfo) throws IOException
    {
        try
        {
            torrentFile.skipBytes(13);
            long length = getNextInteger(torrentFile);
            torrentInfo.setFileLength(length);

            torrentFile.skipBytes(6);
            int n = (int) getNextNumber(torrentFile);
            byte[] name = new byte[n];
            torrentFile.read(name);
            String filename = new String(name);
            torrentInfo.setFilename(filename);

            torrentFile.skipBytes(14);
            n = (int)getNextInteger(torrentFile);
            torrentInfo.setPieceLength(n);

            torrentFile.skipBytes(11);
            int piecesCount  = torrentInfo.getPiecesCount();
            byte[] hash = new byte[piecesCount * 20];
            for(int i = 0; i < piecesCount; ++i)
            {
                torrentFile.read(hash, i*20, 20);
            }
            torrentInfo.setPiecesHash(hash);
            MessageDigest md;
            try
            {
                md = MessageDigest.getInstance("SHA-1");
                md.update(hash);
                torrentInfo.setHandshakeHash(md.digest());
            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
        }
        catch(IOException e)
        {
            throw new IOException("Error: Wrong torrent file.");
        }
        return torrentInfo;
    }

    public static void generateTorrent(String pathToFile, String pathToTorrent) throws IOException
    {
        File file = new File(pathToFile);
        long fileLength = file.length();
        String filename = file.getName();
        int pieceLength = 2097152;
        try
        {
            DataOutputStream torrentFile = new DataOutputStream(new FileOutputStream(pathToTorrent));
            torrentFile.writeBytes("infod7:lengthi");
            torrentFile.writeBytes(String.valueOf(fileLength));
            torrentFile.writeBytes("e5:name");
            torrentFile.writeBytes(String.valueOf(filename.length()));
            torrentFile.writeBytes(":"+ filename);
            torrentFile.writeBytes("13:piecelengthi");
            torrentFile.writeBytes(String.valueOf(pieceLength));
            torrentFile.writeBytes("e7:pieces20:");

            RandomAccessFile inFile = new RandomAccessFile(file, "r");
            byte[] piece = new byte[pieceLength];
            byte[] lastPiece;
            int piecesCount;
            if(fileLength%pieceLength == 0)
            {
                lastPiece = new byte[pieceLength];
                piecesCount = (int)fileLength/pieceLength;
            }
            else
            {
                lastPiece = new byte[(int)fileLength%pieceLength];
                piecesCount = (int)fileLength/pieceLength + 1;
            }
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[][] hash = new byte[piecesCount][20];
            for(int i = 0; i < piecesCount-1; ++i)
            {
                inFile.read(piece);
                md.update(piece);
                hash[i] = md.digest();
            }
            inFile.read(lastPiece);
            md.update(lastPiece);
            hash[piecesCount-1] = md.digest();

            for(int i = 0; i < piecesCount; ++i)
            {
                torrentFile.write(hash[i]);
            }
            inFile.close();
            torrentFile.close();
        }
        catch (Exception e)
        {
            throw new IOException("Can't create torrent file");
        }
    }

    private static long getNextInteger(InputStream is) throws IOException
    {
        int leadingByte = is.read();
        if (leadingByte != 'i')
        {
            throw new IOException("Error: Wrong torrent file.");
        }
        int c;
        long number = 0;
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

    private static boolean isDigit(int c)
    {
        return c >= '0' && c <= '9';
    }

}