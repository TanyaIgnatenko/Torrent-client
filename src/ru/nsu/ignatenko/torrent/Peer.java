package ru.nsu.ignatenko.torrent;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.BitSet;

public class Peer
{
    private BitSet bitfield;
    private SocketChannel channel;
    private byte[] peerID;
    private InetAddress ip;
    private int port;
    private boolean hasOurBitfield;
    private boolean isInteresting;
    private boolean chokedMe;
    private int numDoneRequests;
    private int numDoneRequestsForTime;
    private boolean isLeecher;
    private boolean isSeeder;

    public synchronized void setIp(InetAddress ip)
    {
        this.ip = ip;
    }

    public synchronized void setPort(int port)
    {
        this.port = port;
    }

    public synchronized void setPeerID(byte[] peerID)
    {
        this.peerID = peerID;
    }

    public synchronized void setChannel(SocketChannel channel)
    {
        this.channel = channel;
    }

    public synchronized void setBitfield(BitSet bitfield)
    {
        this.bitfield = bitfield;
    }

    public synchronized void setHasOurBitfield()
    {
        hasOurBitfield = true;
    }

    public void setInteresting(boolean interesting)
    {
        isInteresting = interesting;
    }

    public void setChokedMe(boolean chokedMe)
    {
        this.chokedMe = chokedMe;
    }

    public void setLeecher(boolean leecher)
    {
        isLeecher = leecher;
    }

    public void setSeeder(boolean seeder)
    {
        isSeeder = seeder;
    }

    public synchronized InetAddress getIp()
    {
        return ip;
    }

    public synchronized int getPort()
    {
        return port;
    }

    public synchronized byte[] getPeerID()
    {
        return peerID;
    }

    public synchronized SocketChannel getChannel()
    {
        return channel;
    }

    public boolean isChokedMe()
    {
        return chokedMe;
    }

    public boolean isInteresting()
    {
        return isInteresting;
    }

    public synchronized boolean hasOurBitfield()
    {
        return hasOurBitfield;
    }

    public synchronized BitSet getBitfield()
    {
        return bitfield;
    }

    public synchronized void increaseNumDoneRequests(){++numDoneRequests; ++numDoneRequestsForTime;}

    public synchronized void decreaseNumDoneRequests(){--numDoneRequestsForTime;}

    public int getNumDoneRequestsForTime()
    {
        return numDoneRequestsForTime;
    }

    public int getNumDoneRequests()
    {
        return numDoneRequests;
    }

    public boolean isLeecher()
    {
        return isLeecher;
    }

    public boolean isSeeder()
    {
        return isSeeder;
    }


}
