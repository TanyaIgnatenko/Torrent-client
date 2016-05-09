package ru.nsu.ignatenko.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionManager
{
    MessageManager messageManager = new MessageManager();;
    ServerSocket server;

    public ConnectionManager(int port, byte[] peerID)
    {
        Handshake handshake = new Handshake(19, "BitTorrent protocol".getBytes(), peerID);
        messageManager.setHandshake(handshake);
        try
        {
            server = new ServerSocket(port);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        if(server == null)
        {
            throw new RuntimeException("Port " + port + " isn't available to listen.");
        }
    }

    public void connectTo(int clientPort, byte[] clientPeerID)
    {
        try
        {
            Socket client = new Socket("localhost", clientPort);
            InputStream input = client.getInputStream();
            OutputStream output = client.getOutputStream();

            messageManager.sendHandshake(output);
            Handshake clientHandshake = messageManager.receiveHandshake(input);
            if(!messageManager.isValidHandshake(clientHandshake, clientPeerID))
            {
                client.close();
                System.out.println("Can't connect to this socket. Ip: localhost Port: "  + clientPort);
            }
            else
            {
                System.out.println("Handshake is valid. Success.");
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void listen()
    {
        try
        {
            Socket client = server.accept();
            InputStream input = client.getInputStream();
            OutputStream output = client.getOutputStream();

            Handshake clientHandshake = messageManager.receiveHandshake(input);
            if(!messageManager.isValidHandshake(clientHandshake))
            {
                client.close();
                System.out.println("Wrong Handshake.");
            }
            else
            {
                messageManager.sendHandshake(output);
                System.out.println("Handshake is valid. Success.");
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
