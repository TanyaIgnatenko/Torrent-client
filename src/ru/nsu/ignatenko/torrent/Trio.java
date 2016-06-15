package ru.nsu.ignatenko.torrent;

public class Trio<F,S,T>
{
    public F first;
    public S second;
    public T third;

    public Trio(F first, S second, T third)
    {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}