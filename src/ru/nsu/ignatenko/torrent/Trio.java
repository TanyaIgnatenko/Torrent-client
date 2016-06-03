package ru.nsu.ignatenko.torrent;

//
//public class Trio
//{
//    public int first;
//    public byte[] second;
//    public SocketChannel third;
//
//    public Trio(){}
//    public Trio(int first, byte[] second, SocketChannel third)
//    {
//        this.first = first;
//        this.second = new byte[second.length];
//        System.arraycopy(second, 0, this.second, 0, second.length);
//        this.third = third;
//    }
//}

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