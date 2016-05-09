package ru.nsu.ignatenko.torrent;

import java.io.InputStream;

//arg[0] - name of Torrent file
//arg[1] и arg[2] - peer ids who owns it
//arg[3] и arg[4] - ips
//arg[5] и arg[6] - ports

public class TorrentClient
{
    ConnectionManager connectionManager = new ConnectionManager();
    Reader reader;
    Writer writer;

    public void download(String[] args)
    {
        int port[] = new int[2];
        int peerId[] = new int[2];
        String ip[] = new String[2];

        ip[0] = args[3];
        ip[1] = args[4];
        port[0] = Integer.parseInt(args[5]);
        port[1] = Integer.parseInt(args[6]);
        peerId[0] = Integer.parseInt(args[1]);
        peerId[1] = Integer.parseInt(args[2]);

        InputStream torrentFile = ClassLoader.getSystemClassLoader().getResourceAsStream(args[0]);
        TorrentInfo torrentInfo = Bencoder.parser(torrentFile);
        String path = ClassLoader.getSystemClassLoader().getResource(args[0]).getPath() - args[0] + torrentInfo.getFilename();
        torrentInfo.setPath(path);

        writer = new Writer(torrentInfo.getFilename(),
                            torrentInfo.getPath(),
                            torrentInfo.getFileLength(),
                            torrentInfo.getPieceLength(),
                            torrentInfo.getPiecesCount());

        reader = new Reader(torrentInfo.getFilename(),
                            torrentInfo.getPath(),
                            torrentInfo.getFileLength(),
                            torrentInfo.getPieceLength(),
                            torrentInfo.getPiecesCount());

        connectionManager.setConnectionsWithWriter(writer.getMustWriteQueue(), writer.getReadyWriteQueue());
        connectionManager.setConnectionsWithReader(reader.getMustReadQueue(), reader.getReadyReadQueue());

        writer.start();
        reader.start();

        connectionManager.connectTo(ip[0], port[0], torrentInfo);
        connectionManager.connectTo(ip[1], port[1], torrentInfo);


    }

    public void load(String[] args)
    {

    }

    public static void main(String[] args)
    {
        TorrentClient client = new TorrentClient();
        client.download(args);
    }
}
