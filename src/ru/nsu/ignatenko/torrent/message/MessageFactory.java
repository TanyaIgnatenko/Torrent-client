package ru.nsu.ignatenko.torrent.message;

public class MessageFactory
{
    public Message create(int id, int length, byte[] peerID)
    {
        switch (id)
        {
            case -1:
                return new KeepAlive(length, peerID);
            case 0:
                return new Choke(length, peerID);
            case 1:
                return new Unchoke(length, peerID);
            case 2:
                return new Interested(length, peerID);
            case 3:
                return new Uninterested(length, peerID);
            case 4:
                return new Have(length, peerID);
            case 5:
                return new Bitfield(length, peerID);
            case 6:
                return new Request(length, peerID);
            case 7:
                return new Piece(length, peerID);
            case 8:
                return new Cancel(length, peerID);
        }
        return null;
    }
}
