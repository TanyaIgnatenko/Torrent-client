package ru.nsu.ignatenko.torrent.message.reaction;

import ru.nsu.ignatenko.torrent.message.*;

import java.io.OutputStream;

public class MessageReactionFactory
{
    public Reaction create(Message message, OutputStream output)
    {
        int id = message.getId();
//        switch (id)
//        {
//            case -1:
//                return new KeepAliveReaction(message);
//            case 0:
//                return new ChokeReaction(message, output);
//            case 1:
//                return new UnchokeReaction(message, output);
//            case 2:
//                return new InterestedReaction(message, output);
//            case 3:
//                return new UninterestedReaction(message, output);
//            case 4:
//                return new HaveReaction(message, output);
//            case 5:
//                return new BitfieldReaction(message, output);
//            case 6:
//                return new RequestReaction(message, output);
//            case 7:
//                return new PieceReaction(message, output);
//            case 8:
//                return new CancelReaction(message, output);
//        }
        return null;
    }
}
