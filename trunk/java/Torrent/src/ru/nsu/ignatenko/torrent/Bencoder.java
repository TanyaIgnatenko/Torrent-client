package ru.nsu.ignatenko.torrent;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Bencoder
{

    public static TorrentInfo parse(DataInputStream torrentFile)
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
        torrentInfo.setPieceLength(255);
        torrentInfo.setInfoHash("infohashinfohashinfohash1234".getBytes((Charset.forName("ASCII"))));
        torrentInfo.setFileLength(155269);
        return torrentInfo;
    }
}

//d4:name8:file.txt12:piece length1:16:pieces40:hash6:length1:2e
