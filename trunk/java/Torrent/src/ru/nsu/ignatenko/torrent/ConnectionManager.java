package ru.nsu.ignatenko.torrent;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.io.IOException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConnectionManager implements Runnable
{
	private static Logger logger = LogManager.getLogger("default_logger");

	private static final int MIN_PORT = 6881;
	private static final int MAX_PORT = 6889;


	private ServerSocketChannel serverSocket;
	private ConcurrentMap<byte[], Peer> connectedPeers;
	private TorrentInfo torrentInfo;

	private Selector selector;
	private Map<byte[], SocketChannel> clients;
	private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(30));
	private boolean stop = false;
	private MessageManager messageManager;


	public ConnectionManager(MessageManager messageManager, ConcurrentMap<byte[], Peer> connectedPeers)
	{
		for(int port = MIN_PORT; port <= MAX_PORT; ++port)
		{
			try
			{
				serverSocket = ServerSocketChannel.open();
				serverSocket.bind(new InetSocketAddress(port));
				logger.info("Ready to listen port " + port);
			}
			catch(IOException e)
			{
				continue;
			}
			break;
		}
		if(serverSocket == null)
		{
			throw new RuntimeException("No available port to listen.");
		}

		this.messageManager = messageManager;
		this.connectedPeers = connectedPeers;
		try
		{
			selector = Selector.open();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public void startListen()
	{
		Thread thread = new Thread(this, "Acceptor");
		thread.start();
	}

	public void connectTo(Peer peer, TorrentInfo torrentInfo)
	{
		try
		{
			logger.info("Try connect to some peer");
			byte[] ip = {127, 0, 0, 1};
			SocketChannel client = SocketChannel.open(new InetSocketAddress(InetAddress.getByAddress(ip), peer.getPort()));

				logger.info("Sending handshake...");
				int bytesWrote = messageManager.sendHandshake(client);
				logger.info("Handshake sent " + bytesWrote + " bytes.");
				logger.info("Receiving handshake...");
				Handshake clientHandshake = messageManager.receiveHandshake(client);
//			if(!messageManager.isValidHandshake(clientHandshake, peer.getPeerID()))
				if(!messageManager.isValidHandshake(clientHandshake))
				{
				logger.info("Invalid handshake. Dropping connection...");
				client.close();
			}
			else
			{
				logger.info("Got valid handshake");
				peer.setSocket(client);
				client.configureBlocking(false);
				connectedPeers.put(peer.getPeerID(), peer);
				client.register(selector, SelectionKey.OP_READ, peer);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		while(!stop)
		{
			try
			{
				SocketChannel client = serverSocket.accept();
				logger.info("Some peer want to connect to me");
				logger.info("Receiving handshake...");
				Handshake clientHandshake = messageManager.receiveHandshake(client);
				if(!messageManager.isValidHandshake(clientHandshake))
				{
					logger.info("Invalid handshake. Dropping connection...");
					client.close();
					continue;
				}
				int bytesWrote = messageManager.sendHandshake(client);
				logger.info("Handshake sent " + bytesWrote + " bytes.");
				logger.info("Got valid handshake and sent my handshake to peer");


				Peer peer = new Peer();
				peer.setSocket(client);
				peer.setPeerID(clientHandshake.getPeerID());
				client.configureBlocking(false);
				connectedPeers.put(peer.getPeerID(), peer);
				client.register(selector, SelectionKey.OP_READ, peer);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void workSelector()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				SUPER_POOPER_METHOD();
			}
		}, "Input listener");
		thread.start();
	}

	public void SUPER_POOPER_METHOD()
	{
		while(!stop)
		{
			try
			{
				int numSelected = selector.select(30);
				if(numSelected == 0)
				{
					continue;
				}
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				while(iterator.hasNext())
				{
					SelectionKey key = iterator.next();
					if(key.isReadable())
					{
						try
						{
							messageManager.receiveMessage((SocketChannel) key.channel(), (Peer) key.attachment());
						}
						catch (EOFException e)
						{
							logger.info("EOF got. No data in channel. Disconnecting...");
							key.channel().close();
						}
					}
				}
				selectedKeys.clear();
			}
			catch(Exception e)
			{
				e.printStackTrace();
                throw new RuntimeException();
			}
		}
	}
}
