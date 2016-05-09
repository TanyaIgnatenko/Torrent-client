package ru.nsu.ignatenko.torrent;

public class Pair
{
    int first_;
    byte[] second_;

    public Pair(){}
    public Pair(int first, byte[] second)
    {
        first_ = first;
        second_ = second;
    }
    public int first(){return first_;}
    public byte[] second(){return second_;}
}
