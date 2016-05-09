package ru.nsu.ignatenko.test;

public class TorrentClient
{
    public void run(String[] args)
    {
        int port = Integer.parseInt(args[0]);
        byte[] peerID = args[1].getBytes();
        ConnectionManager connectionManager = new ConnectionManager(port, peerID);

        if (args[2].equals("connectWith"))
        {
            int clientPort = Integer.parseInt(args[3]);
            byte[] clientPeerID = args[4].getBytes();
            connectionManager.connectTo(clientPort, clientPeerID);
        }
        else
        {
            connectionManager.listen();
        }
    }

    public static void main(String [] args)
    {
        TorrentClient client = new TorrentClient();
        client.run(args);
    }

}
