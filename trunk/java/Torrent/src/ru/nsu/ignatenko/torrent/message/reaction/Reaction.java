package ru.nsu.ignatenko.torrent.message.reaction;

import ru.nsu.ignatenko.torrent.message.Message;

public abstract class Reaction
{
    public abstract void react(Message message);
}
