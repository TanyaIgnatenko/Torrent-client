package ru.nsu.ignatenko.torrent.message.reaction;

import ru.nsu.ignatenko.torrent.Peer;
import ru.nsu.ignatenko.torrent.message.Message;

import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class BitfieldReaction extends Reaction
{
    ConcurrentMap<byte[],Peer> connectedPeers;

    public BitfieldReaction(ConcurrentMap<byte[],Peer> connectedPeers)
    {
        this.connectedPeers = connectedPeers;
    }

    public void react(Message message, SocketChannel socket)
    {
        BitSet bitfield = BitSet.valueOf(message.getPayload());
        connectedPeers.get(message.getPeerID()).setBitfield(bitfield);
    }
}
