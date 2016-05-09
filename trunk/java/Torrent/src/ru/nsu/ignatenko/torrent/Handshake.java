package ru.nsu.ignatenko.torrent;

public class Handshake
{
    private final static int PROTOCOL_NAME_LENGTH = 19;
    private final static int HASH_LENGTH = 20;
    private final static int PEER_ID_LENGTH = 20;

    private byte[] peerID;
    private byte[] infoHash;
    private byte[] protocolName;
    private int protocolNameLength;

    public Handshake()
    {
        this.protocolNameLength = PROTOCOL_NAME_LENGTH;
        this.infoHash = new byte[HASH_LENGTH];
        this.protocolName = new byte[PROTOCOL_NAME_LENGTH];
        this.peerID = new byte[20];
    }

    public Handshake(int protocolNameLength, byte[] protocolName, byte[] peerID)
    {
        this.protocolNameLength = protocolNameLength;
        this.infoHash = new byte[HASH_LENGTH];
        this.protocolName = new byte[protocolNameLength];
        this.peerID = new byte[PEER_ID_LENGTH];
        System.arraycopy(peerID, 0, this.peerID, 0, PEER_ID_LENGTH);
        System.arraycopy(protocolName, 0, this.protocolName, 0, protocolNameLength);
    }

    public void setPeerID(byte[] peerID)
    {
        System.arraycopy(peerID, 0, this.peerID, 0, PEER_ID_LENGTH);
    }

    public void setInfoHash(byte[] infoHash)
    {
        System.arraycopy(infoHash, 0, this.infoHash, 0, HASH_LENGTH);
    }

    public void setProtocolName(byte[] protocolName)
    {
        System.arraycopy(protocolName, 0, this.protocolName, 0, protocolNameLength);
    }

    public byte[] getPeerID()
    {
        return peerID;
    }

    public byte[] getInfoHash()
    {
        return infoHash;
    }

    public byte[] getProtocolName()
    {
        return protocolName;
    }

    public int getProtocolNameLength()
    {
        return protocolNameLength;
    }
}
