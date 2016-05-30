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

    public synchronized void setBitfield(BitSet bitfield)
    {
        this.bitfield = bitfield;
    }

    public synchronized void setBit(int pieceIdx)
    {
       bitfield.set(pieceIdx);
    }

    public synchronized void setHasOurBitfield()
    {
        hasOurBitfield = true;
    }

    public synchronized void setSocket(SocketChannel socket)
    {
        this.socket = socket;
    }

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

    public synchronized boolean getHasOurBitfield()
    {
        return hasOurBitfield;
    }

    public synchronized boolean getBit(int pieceIdx)
    {
        return bitfield.get(pieceIdx);
    }

    public synchronized BitSet getBitfield()
    {
        return bitfield;
    }

    public synchronized byte[] getPeerID()
    {
        return peerID;
    }

    public synchronized int getPort()
    {
        return port;
    }

    public synchronized InetAddress getIp()
    {
        return ip;
    }

    public synchronized SocketChannel getSocket()
    {
        return socket;
    }
}
