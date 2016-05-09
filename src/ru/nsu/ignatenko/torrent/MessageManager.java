package ru.nsu.ignatenko.torrent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class MessageManager
{
    private final static int MAX_SIZE = 20;
    private final static int PROTOCOL_NAME_LENGTH = 19;
    private final static int HASH_LENGTH = 20;
    private final static int PEER_ID_LENGTH = 20;
    private final static int NUM_RESERVED_BYTES = 8;

    private Handshake handshake;

    public MessageManager() {}

    public void setHandshake(Handshake handshake)
    {
        this.handshake = handshake;
    }

    public void sendHandshake(OutputStream output) throws IOException
    {
        int protocolNameLength  = handshake.getProtocolNameLength();
        output.write(protocolNameLength);
        output.write(handshake.getProtocolName());

        for (int i = 0; i < NUM_RESERVED_BYTES; ++i)
        {
            output.write(0);
        }

        output.write(handshake.getInfoHash());
        output.write(handshake.getPeerID());
    }

    public Handshake receiveHandshake(InputStream input) throws IOException
    {
        int bytesRead;
        byte[] buf = new byte[MAX_SIZE];
        Handshake clientHandshake = new Handshake();

        int protocolNameLength = input.read();
        if (protocolNameLength  == -1)
        {
            throw new IOException("Unexpected EOF.");
        }

        bytesRead = input.read(buf, 0, protocolNameLength);
        if (bytesRead != protocolNameLength )
        {
            throw new IOException("Invalid clientHandshake.");
        }
        clientHandshake.setProtocolName(buf);

        bytesRead = input.read(buf, 0, NUM_RESERVED_BYTES);
        if (bytesRead != NUM_RESERVED_BYTES)
        {
            throw new IOException("Invalid clientHandshake.");
        }

        bytesRead = input.read(buf, 0, HASH_LENGTH);
        if (bytesRead != HASH_LENGTH)
        {
            throw new IOException("Invalid clientHandshake.");
        }
        clientHandshake.setInfoHash(buf);

        bytesRead = input.read(buf, 0, PEER_ID_LENGTH);
        if (bytesRead != PEER_ID_LENGTH)
        {
            throw new IOException("Invalid clientHandshake.");
        }
        clientHandshake.setPeerID(buf);

        return clientHandshake;
    }

    public boolean isValidHandshake(Handshake clientHandshake)
    {
        return (clientHandshake.getProtocolNameLength() == handshake.getProtocolNameLength() &&
                Arrays.equals(clientHandshake.getProtocolName(), handshake.getProtocolName()) &&
                Arrays.equals(clientHandshake.getInfoHash(), handshake.getInfoHash()));
    }

    public boolean isValidHandshake(Handshake clientHandshake, byte[] expectedPeerID)
    {
        if (Arrays.equals(expectedPeerID, clientHandshake.getPeerID()))
        {
            return isValidHandshake(clientHandshake);
        }
        return false;
    }
}