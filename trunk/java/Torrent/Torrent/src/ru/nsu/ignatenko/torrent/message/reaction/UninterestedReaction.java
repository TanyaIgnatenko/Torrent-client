package ru.nsu.ignatenko.torrent.message.reaction;

import ru.nsu.ignatenko.torrent.Peer;
import ru.nsu.ignatenko.torrent.message.Message;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

public class UninterestedReaction  extends Reaction
{
    public UninterestedReaction(ConcurrentMap<byte[],Peer> connectedPeers)
    {

    }
}
