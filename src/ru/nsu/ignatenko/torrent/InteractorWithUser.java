package ru.nsu.ignatenko.torrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

class InteractorWithUser implements Runnable
{
    private static Logger logger = LogManager.getLogger("default_logger");

    private TorrentClient torrentClient;
    private BlockingQueue<Peer> peers;
    boolean stop ;

    InteractorWithUser(TorrentClient torrentClient, BlockingQueue<Peer> peers)
    {
        this.torrentClient = torrentClient;
        this.peers = peers;
    }

    public PeerBehaviour getInfoAboutOurPeer()
    {
        System.out.println("Input format:\n" +
                "To create torrent from file use: -c [path to Torrent] [path to file]\n" +
                "To distribute file from torrent use: -s [path to Torrent] [path to File]\n" +
                "To download file from torrent use: -l [path to Torrent] [path to download file]");
//
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        PeerBehaviour ourPeerBehaviour = new PeerBehaviour();
        ourPeerBehaviour.setPathToTorrent(scanner.next());
        if (input.equals("-c"))
        {
            ourPeerBehaviour.setCreator(true);
            ourPeerBehaviour.setPathToFile(scanner.next());
        }
        else if (input.equals("-s"))
        {
            ourPeerBehaviour.setSeeder(true);
            ourPeerBehaviour.setPathToFile(scanner.next());
        }
        else if (input.equals("-l"))
        {
            ourPeerBehaviour.setLeecher(true);
            ourPeerBehaviour.setPathToFile(scanner.next());
        }
        else
        {
            System.out.println("Error: wrong input.");
            System.out.println("Some problem happened. For more information look in a log file. Torrent client is terminated.");
            System.exit(1);
        }
//        PeerBehaviour ourPeerBehaviour = new PeerBehaviour();
//        Scanner scanner = new Scanner(System.in);
//        int input = scanner.nextInt();
//        if (input == 1)
//        {
//            ourPeerBehaviour.setSeeder(true);
//            ourPeerBehaviour.setLeecher(false);
//            ourPeerBehaviour.setPathToTorrent("C:/pass/java/trunk/Torrent2/host1/movie.torrent");
//            ourPeerBehaviour.setPathToFile("C:/pass/java/trunk/Torrent2/host1/movie.MOV");
//        }
//        else if (input == 2)
//        {
//            ourPeerBehaviour.setSeeder(true);
//            ourPeerBehaviour.setLeecher(false);
//            ourPeerBehaviour.setPathToTorrent("C:/pass/java/trunk/Torrent2/host2/movie.torrent");
//            ourPeerBehaviour.setPathToFile("C:/pass/java/trunk/Torrent2/host2/movie.MOV");
//        }
//        else if (input == 3)
//        {
//            ourPeerBehaviour.setLeecher(true);
//            ourPeerBehaviour.setSeeder(false);
//            ourPeerBehaviour.setPathToTorrent("C:/pass/java/trunk/Torrent2/downloader1/movie.torrent");
//            ourPeerBehaviour.setPathToFile("C:/pass/java/trunk/Torrent2/downloader1/movie.MOV");
//        }
//        else if (input == 4)
//        {
//            ourPeerBehaviour.setLeecher(true);
//            ourPeerBehaviour.setSeeder(false);
//            ourPeerBehaviour.setPathToTorrent("C:/pass/java/trunk/Torrent2/downloader2/movie.torrent");
//            ourPeerBehaviour.setPathToFile("C:/pass/java/trunk/Torrent2/downloader2/movie.MOV");
//        }
        return ourPeerBehaviour;
    }

    @Override
    public void run()
    {
        System.out.println("Input format:\n" +
                "To tell about peer that has this torrent use: [peerID] [ip] [port]\n" +
                "To stop work of torrent client use: stop");

        while (true)
        {
            Scanner scanner = new Scanner(System.in);
            String input = scanner.next();
            if (input.equals("stop"))
            {
                torrentClient.stop();
            }
            else
            {
                try
                {
                    Peer peer = new Peer();
                    peer.setPeerID(input.getBytes(Charset.forName("ASCII")));
                    peer.setIp(InetAddress.getByName(scanner.next()));
                    peer.setPort(scanner.nextInt());
                    peers.put(peer);
                }
                catch (UnknownHostException e)
                {
                    System.out.println("Error: Wrong Ip. Try again and do it right.");
                    continue;
                }
                catch (InterruptedException e)
                {
                    logger.info("Never happens");
                }
            }
        }
    }
//        System.out.println("Print 1 or 2");
//        Scanner scanner = new Scanner(System.in);
//        int input = scanner.nextInt();
//        int i = 0;
//        while (!stop)
//        {
//            try
//            {
//                Peer peer = generatePeer(i, input);
//                if (peer != null)
//                {
//                    peers.put(peer);
//                }
//            }
//            catch (InterruptedException e)
//            {
//                logger.info("Never happens");
//            }
//            ++i;
//        }
//    }

//        try
//        {
//            Peer peer = new Peer();
//            peer.setPeerID("12345678901234566881".getBytes(Charset.forName("ASCII")));
//            peer.setIp(InetAddress.getByName("127.0.0.1"));
//            peer.setPort(6881);
//            peers.put(peer);
//        }
//        catch (UnknownHostException e)
//        {
//            e.printStackTrace();
//        }
//        catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }
//        try
//        {
//            if(torrentClient.getOurPeer().getPort() != 6882)
//            {
//                Peer peer = new Peer();
//                peer.setPeerID("12345678901234566882".getBytes(Charset.forName("ASCII")));
//                peer.setIp(InetAddress.getByName("127.0.0.1"));
//                peer.setPort(6882);
//                peers.put(peer);
//            }
//        }
//        catch (UnknownHostException e)
//        {
//            e.printStackTrace();
//        }
//        catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }

//    public Peer generatePeer(int i, int input)
//    {
//
//        Peer peer = new Peer();
//        if (i == 1)
//        {
//            try
//            {
//                peer.setPeerID("12345678901234566881".getBytes(Charset.forName("ASCII")));
//                peer.setIp(InetAddress.getByName("127.0.0.1"));
//                peer.setPort(6881);
//                System.out.println("hello1");
//
//                return peer;
//            }
//            catch (UnknownHostException e)
//            {
//                e.printStackTrace();
//            }
//        }
//        if (i == 2)
//        {
//            try
//            {
//                peer.setPeerID("12345678901234566882".getBytes(Charset.forName("ASCII")));
//                peer.setIp(InetAddress.getByName("127.0.0.1"));
//                peer.setPort(6882);
//                System.out.println("hello2");
//                return peer;
//            }
//            catch (UnknownHostException e)
//            {
//                e.printStackTrace();
//            }
//        }
//        if (i == 3 && input == 2)
//        {
//            try
//            {
//                peer.setPeerID("12345678901234566883".getBytes(Charset.forName("ASCII")));
//                peer.setIp(InetAddress.getByName("127.0.0.1"));
//                peer.setPort(6883);
//                System.out.println("hello3");
//            }
//            catch (UnknownHostException e)
//            {
//                e.printStackTrace();
//            }
//            return peer;
//        }
//        if(i == 4)
//        {
//            stop = true;
//        }
//        return null;
//    }

    public void printStatistics(BlockingQueue<Peer> connectedPeers)
    {
        System.out.println("Statistics:");
        for (Peer peer : connectedPeers)
        {
            System.out.println("Peer with Id: " + new String(peer.getPeerID()) + " sent " + peer.getNumDoneRequests() + " pieces.");
        }
    }
}
