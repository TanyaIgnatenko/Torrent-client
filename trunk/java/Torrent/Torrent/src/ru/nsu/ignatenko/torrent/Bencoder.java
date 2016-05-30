package ru.nsu.ignatenko.torrent;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Bencoder
{

    public static TorrentInfo parser()
    {
        TorrentInfo torrentInfo = new TorrentInfo();
//        try
//        {
////            input.skipBytes(7);
////            int n = input.readByte();
////            byte[] name = new byte[n];
////            input.read(name, 0, n);
////            filename = new String(name);
////            input.skipBytes(15);
////            n = input.readByte();
//        }
//        catch(IOException e)
//        {
//            e.printStackTrace();
//        }

        torrentInfo.setFilename("image2.jpg");
        torrentInfo.setPieceLength(20000);
        torrentInfo.setSha1(new Byte[5]);
        torrentInfo.setFileLength(155269);
        return torrentInfo;
    }
}

//d4:name8:file.txt12:piece length1:16:pieces40:hash6:length1:2e
