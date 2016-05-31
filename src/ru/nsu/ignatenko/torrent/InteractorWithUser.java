package ru.nsu.ignatenko.torrent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

class InteractorWithUser
{
    private TorrentClient torrentClient;
    private BlockingQueue<Peer> peers;
    private boolean stop;

    InteractorWithUser(TorrentClient torrentClient, BlockingQueue<Peer> peers)
    {
        this.torrentClient = torrentClient;
        this.peers = peers;
    }

    public PeerBehaviour getInfoAboutOurPeer()
    {
        System.out.println("Input format:\n" +
                "To create torrent from file/s use: -c [path to file]\n" +
                "To distribute file from torrent use: -s [path to Torrent] [path to File]\n" +
                "To download file/s from torrent use: -l [path to Torrent] [path to directory for download]");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        PeerBehaviour ourPeerBehaviour = new PeerBehaviour();
        if (input.equals("-c"))
        {
            ourPeerBehaviour.setCreator(true);
            ourPeerBehaviour.setPathToFile(scanner.next());
        }
        else if (input.equals("-s"))
        {
            ourPeerBehaviour.setSeeder(true);
            ourPeerBehaviour.setPathToTorrent(scanner.next());
            ourPeerBehaviour.setPathToFile(scanner.next());
        }
        else if (input.equals("-l"))
        {
            ourPeerBehaviour.setLeecher(true);
            ourPeerBehaviour.setPathToTorrent(scanner.next());
            ourPeerBehaviour.setPathToDownloadDir(scanner.next());
        }
        else
        {
            System.out.println("Error: wrong input.");
            throw new RuntimeException();
        }
        return ourPeerBehaviour;
    }


    public void run()
    {
//        System.out.println("Input format:\n" +
//                "To tell about peer that has this torrent use: [peerID] [ip] [port]\n" +
//                "To stop work of torrent client use: stop");
//
//            Scanner scanner = new Scanner(System.in);
//            String input = scanner.next();
//            if (input.equals("stop"))
//            {
//                stop = true;
//            }
//            else
//            {
//                try
//                {
//                    Peer peer = new Peer();
//                    peer.setPeerID(input.getBytes(Charset.forName("ASCII")));
//                    peer.setIp(InetAddress.getByName(scanner.next()));
//                    peer.setPort(scanner.nextInt());
//                    peers.put(peer);
//                }
//                catch (UnknownHostException e)
//                {
//                    System.out.println("Error: wrong Ip.");
//                    e.printStackTrace();
//                }
//                catch (InterruptedException e)
//                {
//                    e.printStackTrace();
//                }
//            }

        try
        {
            Peer peer = new Peer();
            peer.setPeerID("12345678901234566881".getBytes(Charset.forName("ASCII")));
            peer.setIp(InetAddress.getByName("127.0.0.1"));
            peer.setPort(6881);
            peers.put(peer);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        try
        {
            if(torrentClient.getOurPeer().getPort() != 6882)
            {
                Peer peer = new Peer();
                peer.setPeerID("12345678901234566882".getBytes(Charset.forName("ASCII")));
                peer.setIp(InetAddress.getByName("127.0.0.1"));
                peer.setPort(6882);
                peers.put(peer);
            }
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void printStatistics(BlockingQueue<Peer> connectedPeers)
    {
        System.out.println("Statistics:");
        for (Peer peer : connectedPeers)
        {
            System.out.println("Peer with Id: "+new String(peer.getPeerID())+" sent "+peer.getNumDoneRequests()+" pieces.");
        }
    }
}
