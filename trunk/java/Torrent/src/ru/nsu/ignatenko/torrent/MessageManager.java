package ru.nsu.ignatenko.torrent;

import com.sun.media.sound.InvalidFormatException;
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
import java.util.Collection;
import java.util.HashMap;

public class MessageManager
{
    private static Logger logger = LogManager.getLogger("default_logger");
    private final static int MAX_SIZE = 20;
    private final static int PROTOCOL_NAME_LENGTH = 19;
    private final static int HASH_LENGTH = 20;
    private final static int PEER_ID_LENGTH = 20;
    private final static int NUM_RESERVED_BYTES = 8;
    private final static int HANDSHAKE_SIZE = 68;
    private Handshake handshake = new Handshake();
    private MessageFactory messageFactory = new MessageFactory();
    private HashMap<Byte, Reaction> messageReactions;


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
        int bytesRead;
        int totalBytesRead = 0;
        int protocolNameLength;
        ByteBuffer protocolNameBuf;
        ByteBuffer lengthBuf = ByteBuffer.allocate(1);
        ByteBuffer reservedBytesBuf = ByteBuffer.allocate(NUM_RESERVED_BYTES);
        ByteBuffer hashBuf = ByteBuffer.allocate(HASH_LENGTH);
        ByteBuffer peerIdBuf = ByteBuffer.allocate(PEER_ID_LENGTH);

        if ((bytesRead = socket.read(lengthBuf)) != 1)
        {
            throw new InvalidFormatException("Unexpected EOF.");
        }
        totalBytesRead += bytesRead;
//        logger.info("Receiving handshake: length read: {} bytes.", bytesRead);

        lengthBuf.rewind();
        protocolNameLength = lengthBuf.get();

        if (protocolNameLength != PROTOCOL_NAME_LENGTH)
        {
            throw new InvalidFormatException("Invalid clientHandshake.");
        }
        protocolNameBuf = ByteBuffer.allocate(protocolNameLength);

        if ((bytesRead = socket.read(protocolNameBuf)) != protocolNameLength)
        {
            throw new InvalidFormatException("Invalid clientHandshake.");
        }
//        logger.info("Receiving handshake: protocol name read: {} bytes.", bytesRead);
        totalBytesRead += bytesRead;
        if ((bytesRead = socket.read(reservedBytesBuf)) != NUM_RESERVED_BYTES)
        {
            throw new InvalidFormatException("Invalid clientHandshake.");
        }
//        logger.info("Receiving handshake: reserved bytes read: {} bytes.", bytesRead);
        totalBytesRead += bytesRead;
        if ((bytesRead = socket.read(hashBuf)) != HASH_LENGTH)
        {
            throw new InvalidFormatException("Invalid clientHandshake.");
        }
//        logger.info("Receiving handshake: hash read: {} bytes.", bytesRead);
        totalBytesRead += bytesRead;
        if ((bytesRead = socket.read(peerIdBuf)) != PEER_ID_LENGTH)
        {
            throw new InvalidFormatException("Invalid clientHandshake.");
        }
//        logger.info("Receiving handshake: peerID read: {} bytes.", bytesRead);
        totalBytesRead += bytesRead;

        logger.info("Successfully received {} bytes of handshake.", totalBytesRead);
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
        return false;
    }

    public void sendMessage(SocketChannel socket, ByteBuffer message)
    {
        try
        {
//            logger.info("In sendMessage size of message : "+message.remaining());
//            int bytesWrote = socket.write(message);
//            byte id = message.get(4);
//            logger.info("Sended message {} into socket in size {} bytes.", id, bytesWrote);
            int bytesWrote;
            logger.info("In sendMessage size of message : " + message.remaining());
            while ((bytesWrote = socket.write(message)) == 0);
            byte id = message.get(4);
            logger.info("Sended message {} into socket in size {} bytes.", id, bytesWrote);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public ByteBuffer generateCancel()
    {
        ByteBuffer message = ByteBuffer.allocate(4 + 1);
        int length = 13;
        byte id = 8;
        message.putInt(length);
        message.put(id);
        logger.info("Generated cancel message");
        message.flip();

        return message;
    }

    public ByteBuffer generateHave(int pieceIdx)
    {
        ByteBuffer message = ByteBuffer.allocate(4 + 4 + 1);
        int length = 5;
        byte id = 4;
        message.putInt(length);
        message.put(id);
        message.putInt(pieceIdx);
        logger.info("Generated have message");
        message.flip();
        return message;
    }

    public ByteBuffer generateRequest(int pieceIdx)
    {
        ByteBuffer message = ByteBuffer.allocate(4 + 4 + 1);
        int length = 5;
        byte id = 6;
        message.putInt(length);
        message.put(id);
        message.putInt(pieceIdx);
        logger.info("Generated request message");
        message.flip();
        return message;
    }

    public ByteBuffer generatePiece(byte[] piece, int pieceIdx)
    {
//        logger.info("In generatePiece length of piece: " + piece.length+ "\n piece: "+new String(piece));
        int length = piece.length + 4 +  1;
        ByteBuffer message = ByteBuffer.allocate(length + 4);
        byte id = 7;
        message.putInt(length);
        message.put(id);
        message.putInt(pieceIdx);
        message.put(piece);
        logger.info("Generated piece message");
        message.flip();
        return message;
    }

    public ByteBuffer generateBitfield(BitSet bitfield, int pieceCount)
    {
        byte[] bitset = bitfield.toByteArray();
        byte[] payload = new byte[pieceCount/8 + 1];
        System.arraycopy(bitset, 0, payload, 0, bitset.length);
        for(int i = bitfield.length(); i < pieceCount/8 + 1; ++i)
        {
            payload[i] = (byte)0;
        }
        System.out.println("payload length: "+payload.length);
        int length = payload.length+1;
        ByteBuffer message = ByteBuffer.allocate(length + 4);
        byte id = 5;
        message.putInt(length);
        message.put(id);
        message.put(payload);
        logger.info("Generated bitfield message");
        message.flip();
        System.out.println("Buffer limit: "+message.limit());
        return message;
    }

    public void receiveMessage(SocketChannel socket, Peer peer) throws IOException
    {
        ByteBuffer data1 = ByteBuffer.allocate(4);

        int count = socket.read(data1);
        if (count == -1)
        {
            throw new EOFException();
        }
        logger.info("Received some message");
        data1.rewind();
        if (count != 4)
        {
            System.out.println(count);
            throw new InvalidFormatException();
        }
        int length = data1.getInt();
        ByteBuffer data2 = ByteBuffer.allocate(1);
        socket.read(data2);
        data2.rewind();
        byte id = data2.get();
        logger.info("With id " + id);
        Message message = messageFactory.create(id, length-1, peer);
        message.parse(socket);
        if (messageReactions == null)
        {
            System.out.println("mesReact == null");
        }
        messageReactions.get(id).react(message);
    }
}