package ru.nsu.ignatenko.torrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.message.Message;

import ru.nsu.ignatenko.torrent.message.MessageFactory;
import ru.nsu.ignatenko.torrent.message.reaction.Reaction;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;

public class MessageManager
{
    private Handshake handshake = new Handshake();
    private HashMap<Byte, Reaction> messageReactions;
    private MessageFactory messageFactory = new MessageFactory();
    private static Logger logger = LogManager.getLogger("default_logger");

    private final static int LENGTH_SIZE = 4;
    private final static int ID_SIZE = 1;
    private final static int PIECE_IDX_SIZE = 4;
    private final static int PROTOCOL_NAME_LENGTH = 19;
    private final static int HASH_LENGTH = 20;
    private final static int PEER_ID_LENGTH = 20;
    private final static int NUM_BITS_IN_BYTE = 8;
    private final static int NUM_RESERVED_BYTES = 8;
    private final static int HANDSHAKE_SIZE = 68;

    public MessageManager(HashMap<Byte, Reaction> messageReactions)
    {
        this.messageReactions = messageReactions;
        handshake.setProtocolName("Bittorrent Protocol".getBytes(Charset.forName("ASCII")));
    }

    public void createHandshake(byte[] peerID, byte[] infoHash)
    {
        handshake.setPeerID(peerID);
        handshake.setInfoHash(infoHash);
    }

    public int sendHandshake(SocketChannel socket) throws IOException
    {
        ByteBuffer message = ByteBuffer.allocate(HANDSHAKE_SIZE);
        message.put((byte) handshake.getProtocolNameLength());
        message.put(handshake.getProtocolName());
        byte zero = 0;
        for (int i = 0; i < NUM_RESERVED_BYTES; ++i)
        {
            message.put(zero);
        }
        message.put(handshake.getInfoHash());
        message.put(handshake.getPeerID());
        message.flip();
        return socket.write(message);
    }

    public Handshake receiveHandshake(SocketChannel socket) throws IOException
    {
        int protocolNameLength;
        ByteBuffer protocolNameBuf;
        ByteBuffer lengthBuf = ByteBuffer.allocate(1);
        ByteBuffer reservedBytesBuf = ByteBuffer.allocate(NUM_RESERVED_BYTES);
        ByteBuffer hashBuf = ByteBuffer.allocate(HASH_LENGTH);
        ByteBuffer peerIdBuf = ByteBuffer.allocate(PEER_ID_LENGTH);

        if ((socket.read(lengthBuf)) != 1)
        {
            throw new IOException("Unexpected EOF.");
        }
        lengthBuf.rewind();
        protocolNameLength = lengthBuf.get();

        if (protocolNameLength != PROTOCOL_NAME_LENGTH)
        {
            throw new IOException("Invalid clientHandshake.");
        }
        protocolNameBuf = ByteBuffer.allocate(protocolNameLength);

        if (socket.read(protocolNameBuf) != protocolNameLength)
        {
            throw new IOException("Invalid clientHandshake.");
        }

        if (socket.read(reservedBytesBuf) != NUM_RESERVED_BYTES)
        {
            throw new IOException("Invalid clientHandshake.");
        }

        if (socket.read(hashBuf) != HASH_LENGTH)
        {
            throw new IOException("Invalid clientHandshake.");
        }

        if (socket.read(peerIdBuf) != PEER_ID_LENGTH)
        {
            throw new IOException("Invalid clientHandshake.");
        }

        protocolNameBuf.rewind();
        hashBuf.rewind();
        peerIdBuf.rewind();

        Handshake clientHandshake = new Handshake();
        clientHandshake.setProtocolName(protocolNameBuf.array());
        clientHandshake.setInfoHash(hashBuf.array());
        clientHandshake.setPeerID(peerIdBuf.array());
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
        logger.info("Expected peerID: {} actual clien't peerID: {}", expectedPeerID, clientHandshake.getPeerID());
        return false;
    }

    public void sendMessage(SocketChannel channel, ByteBuffer message)
    {
        try
        {
            while (message.hasRemaining())
            {
                channel.write(message);
            }
        }
        catch (IOException e)
        {
            logger.info("Error: Can't send message.");
        }
    }

    public ByteBuffer generateCancel(int pieceIdx)
    {
        int length = ID_SIZE + PIECE_IDX_SIZE;
        ByteBuffer message = ByteBuffer.allocate(LENGTH_SIZE + length);
        byte id = 8;
        message.putInt(length);
        message.put(id);
        message.putInt(pieceIdx);
        message.flip();
        return message;
    }

    public ByteBuffer generateUnchoke()
    {
        int length = ID_SIZE;
        ByteBuffer message = ByteBuffer.allocate(LENGTH_SIZE + length);
        byte id = 1;
        message.putInt(length);
        message.put(id);
        message.flip();
        return message;
    }

    public ByteBuffer generateChoke()
    {
        int length = ID_SIZE;
        ByteBuffer message = ByteBuffer.allocate(LENGTH_SIZE + length);
        byte id = 0;
        message.putInt(length);
        message.put(id);
        message.flip();
        return message;
    }

    public ByteBuffer generateInterested()
    {
        int length = ID_SIZE;
        ByteBuffer message = ByteBuffer.allocate(LENGTH_SIZE + length);
        byte id = 2;
        message.putInt(length);
        message.put(id);
        message.flip();
        return message;
    }

    public ByteBuffer generateUninterested()
    {
        int length = ID_SIZE;
        ByteBuffer message = ByteBuffer.allocate(LENGTH_SIZE + length);
        byte id = 3;
        message.putInt(length);
        message.put(id);
        message.flip();
        return message;
    }

    public ByteBuffer generateHave(int pieceIdx)
    {
        int length = ID_SIZE + PIECE_IDX_SIZE;
        ByteBuffer message = ByteBuffer.allocate(LENGTH_SIZE + length);
        byte id = 4;
        message.putInt(length);
        message.put(id);
        message.putInt(pieceIdx);
        message.flip();
        return message;
    }

    public ByteBuffer generateRequest(int pieceIdx)
    {
        int length = ID_SIZE + PIECE_IDX_SIZE;
        ByteBuffer message = ByteBuffer.allocate(LENGTH_SIZE + length);
        byte id = 6;
        message.putInt(length);
        message.put(id);
        message.putInt(pieceIdx);
        message.flip();
        return message;
    }

    public ByteBuffer generatePiece(byte[] piece, int pieceIdx)
    {
        int length = piece.length + ID_SIZE + PIECE_IDX_SIZE;
        ByteBuffer message = ByteBuffer.allocate(LENGTH_SIZE + length);
        byte id = 7;
        message.putInt(length);
        message.put(id);
        message.putInt(pieceIdx);
        message.put(piece);
        message.flip();
        return message;
    }

    public ByteBuffer generateBitfield(BitSet bitfield, int pieceCount)
    {
    int n = pieceCount/NUM_BITS_IN_BYTE + Integer.signum(pieceCount%NUM_BITS_IN_BYTE);
    byte[] bitset = bitfield.toByteArray();
    byte[] payload = new byte[n];
    System.arraycopy(bitset, 0, payload, 0, bitset.length);
    for(int i = bitfield.length(); i < n; ++i)
    {
        payload[i] = (byte)0;
    }
    int length = payload.length + ID_SIZE;
    ByteBuffer message = ByteBuffer.allocate(length + LENGTH_SIZE);
    byte id = 5;
    message.putInt(length);
    message.put(id);
    message.put(payload);
    message.flip();
    return message;
}


    public void receiveMessage(SocketChannel socket, Peer peer) throws IOException
    {
        int count = 0;
        ByteBuffer data1 = ByteBuffer.allocate(LENGTH_SIZE);
        while(data1.hasRemaining())
        {
            count+= socket.read(data1);
        }
        if (count == -1 || count != LENGTH_SIZE)
        {
            throw new EOFException();
        }

        data1.rewind();
        int length = data1.getInt();

        count = 0;
        ByteBuffer data2 = ByteBuffer.allocate(ID_SIZE);
        while(data2.hasRemaining())
        {
            count+= socket.read(data2);
        }
        if (count == -1 || count != 1)
        {
            throw new EOFException();
        }

        data2.rewind();
        byte id = data2.get();
        Message message = messageFactory.create(id, length-1, peer);
        message.parse(socket);
        messageReactions.get(id).react(message);
    }
}