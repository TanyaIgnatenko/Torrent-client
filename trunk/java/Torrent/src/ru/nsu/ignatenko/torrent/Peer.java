package ru.nsu.ignatenko.torrent;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.BitSet;

public class Peer
{
    private BitSet bitfield;
    private SocketChannel socket;
    private byte[] peerID;
    private InetAddress ip;
    private int port;
    private boolean hasOurBitfield;
    private boolean isInteresting;
    private boolean chokedMe;

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

    public synchronized void setSocket(SocketChannel socket)
    {
        this.socket = socket;
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

    public synchronized SocketChannel getSocket()
    {
        return socket;
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
}
