package ru.nsu.ignatenko.torrent.message;

import ru.nsu.ignatenko.torrent.Peer;

public class MessageFactory
{
    public Message create(int id, int length, Peer peer)
    {
        switch (id)
        {
            case 0:
                return new Choke(length, peer);
            case 1:
                return new Unchoke(length, peer);
            case 2:
                return new Interested(length, peer);
            case 3:
                return new Uninterested(length, peer);
            case 4:
                return new Have(length, peer);
            case 5:
                return new Bitfield(length, peer);
            case 6:
                return new Request(length, peer);
            case 7:
                return new Piece(length, peer);
            case 8:
                return new Cancel(length, peer);
        }
        return null;
    }
}
