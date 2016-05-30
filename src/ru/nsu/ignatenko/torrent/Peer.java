package ru.nsu.ignatenko.torrent;

import java.nio.channels.SocketChannel;
import java.util.BitSet;

public class Peer
{
    private BitSet bitfield;
    private SocketChannel socket;
    private byte[] peerID;
    private String ip;
    private int port;
    private boolean hasOurBitfield;

    public void setBitfield(BitSet bitfield)
    {
        this.bitfield = bitfield;
    }

    public void setBit(int pieceIdx)
    {
       bitfield.set(pieceIdx);
    }

    public void setHasOurBitfield()
    {
        hasOurBitfield = true;
    }

    public void setSocket(SocketChannel socket)
    {
        this.socket = socket;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setPeerID(byte[] peerID)
    {
        this.peerID = peerID;
    }

    public boolean getHasOurBitfield()
    {
        return hasOurBitfield;
    }

    public boolean getBit(int pieceIdx)
    {
        return bitfield.get(pieceIdx);
    }

    public BitSet getBitfield()
    {
        return bitfield;
    }

    public byte[] getPeerID()
    {
        return peerID;
    }

    public int getPort()
    {
        return port;
    }

    public String getIp()
    {
        return ip;
    }

    public SocketChannel getSocket()
    {
        return socket;
    }
}
