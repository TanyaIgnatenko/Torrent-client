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
    boolean stop;

    InteractorWithUser(TorrentClient torrentClient, BlockingQueue<Peer> peers)
    {
        this.torrentClient = torrentClient;
        this.peers = peers;
    }

    public PeerBehaviour getInfoAboutOurPeer()
    {
        System.out.println("\nInput format:\n" +
                "To create torrent from file use: -c [path to Torrent] [path to file]\n" +
                "To distribute file from torrent use: -s [path to Torrent] [path to File]\n" +
                "To download file from torrent use: -l [path to Torrent] [path to download file]");

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
        return ourPeerBehaviour;
    }

    @Override
    public void run()
    {
        System.out.println("Input format:\n" +
                "To tell about peer that has this torrent use: [peerID] [ip] [port]\n" +
                "To stop work of torrent client use: stop");

        while (!stop)
        {
            Scanner scanner = new Scanner(System.in);
            String input = scanner.next();
            if (input.equals("stop"))
            {
                stop = true;
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

    public void printStatistics(BlockingQueue<Peer> connectedPeers)
    {
        System.out.println("Statistics:");
        for (Peer peer : connectedPeers)
        {
            System.out.println("Peer with Id: " + new String(peer.getPeerID()) + " sent " + peer.getNumDoneRequests() + " pieces.");
        }
    }

    public void waitStopCommand()
    {
        System.out.println("\nInput format:\n" + "To stop work of torrent client use: stop");

        while (!stop)
        {
            Scanner scanner = new Scanner(System.in);
            String input = scanner.next();
            if (input.equals("stop"))
            {
                stop = true;
                torrentClient.stop();
            }
        }
    }
}
