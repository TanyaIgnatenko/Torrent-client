package ru.nsu.ignatenko.torrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.nsu.ignatenko.torrent.exceptions.PortNotFoundException;
import ru.nsu.ignatenko.torrent.exceptions.SelectorException;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionManager
{
	private static Logger logger = LogManager.getLogger("default_logger");

	private static final int MIN_PORT = 6881;
	private static final int MAX_PORT = 6889;


	private ServerSocketChannel serverSocket;
	private BlockingQueue<Peer> connectedPeers;
	private BlockingQueue<Peer> newConnectedPeers;
	private TorrentInfo torrentInfo;
	private Peer ourPeer;

    private Lock selectorRegisterLock = new ReentrantLock();
    private MessageManager messageManager;
    private Selector selector;
	private boolean stop;

	public ConnectionManager(MessageManager messageManager, BlockingQueue<Peer> connectedPeers,
							 BlockingQueue<Peer> newConnectedPeers, Peer ourPeer, TorrentInfo torrentInfo)
							 throws PortNotFoundException, SelectorException
	{
		for (int port = MIN_PORT; port <= MAX_PORT; ++port)
		{
			try
			{
				serverSocket = ServerSocketChannel.open();
				serverSocket.bind(new InetSocketAddress(port));
				ourPeer.setPort(port);
				String peerID = "1234567890123456" + port;
				ourPeer.setPeerID(peerID.getBytes(Charset.forName("ASCII")));
				System.out.println("Ready to listen port " + port);
				System.out.println("Set peerID: " + peerID);
				logger.info("Ready to listen port " + port);
			}
			catch (IOException e)
			{
				continue;
			}
			break;
		}
		if (serverSocket == null)
		{
			logger.info("No available port to listen.");
			throw new PortNotFoundException();
		}

		this.messageManager = messageManager;
		this.connectedPeers = connectedPeers;
		this.newConnectedPeers = newConnectedPeers;
		this.torrentInfo = torrentInfo;
		this.ourPeer = ourPeer;
		try
		{
			selector = Selector.open();
		}
		catch (IOException e)
		{
			logger.info("Error: Cant't open a selector.");
			throw new SelectorException();
		}
	}

	public void connectTo(Peer peer)
	{
		try
		{
			SocketChannel client = SocketChannel.open(new InetSocketAddress(peer.getIp(), peer.getPort()));
			messageManager.createHandshake(ourPeer.getPeerID(), torrentInfo.getHandshakeHash());
			messageManager.sendHandshake(client);
			Handshake clientHandshake = messageManager.receiveHandshake(client);
			if (!messageManager.isValidHandshake(clientHandshake, peer.getPeerID()))
			{
				logger.info("Invalid handshake. Dropping connection...");
				dropConnection(client);
			}
			else
			{
				logger.info("Got valid handshake");
				peer.setChannel(client);
				try
				{
					client.setOption(StandardSocketOptions.TCP_NODELAY, true);
					client.configureBlocking(false);
					selectorRegisterLock.lock();
					try
					{
						client.register(selector, SelectionKey.OP_READ, peer);
					}
					finally
					{
						selectorRegisterLock.unlock();
					}
					connectedPeers.put(peer);
					synchronized (newConnectedPeers)
					{
						newConnectedPeers.put(peer);
						newConnectedPeers.notify();
					}
				}
				catch (IOException e)
				{
					logger.info("Error: Can't register  selector to SocketChannel. Dropping connection...");
					dropConnection(client);
				}
				catch (InterruptedException e)
				{
					logger.info("Never happens.");
				}
			}
		}
		catch (IOException e)
		{
			logger.info("Error: Can't connect to peer.");
		}
	}

	public void proccessConnection(SelectionKey connectionKey)
	{
			SocketChannel client = null;
			try
			{
				ServerSocketChannel acceptor = (ServerSocketChannel) connectionKey.channel();
				client = acceptor.accept();
				logger.info("Some peer want to connect to me");
//				logger.info("Receiving handshake...");
				messageManager.createHandshake(ourPeer.getPeerID(), torrentInfo.getHandshakeHash());
				Handshake clientHandshake = messageManager.receiveHandshake(client);
				if (!messageManager.isValidHandshake(clientHandshake))
				{
					logger.info("Invalid handshake. Dropping connection...");
					dropConnection(client);
				}
				logger.info("Got valid handshake. Port:{}", ourPeer.getPort());
				int bytesWrote = messageManager.sendHandshake(client);
				logger.info("Handshake sent " + bytesWrote + " bytes.");
//				logger.info("Got valid handshake and sent my handshake to peer");
				System.out.println("Got valid handshake and sent my handshake to peer");

				Peer peer = new Peer();
				peer.setChannel(client);
				peer.setPeerID(clientHandshake.getPeerID());

				try
				{
					client.setOption(StandardSocketOptions.TCP_NODELAY, true);
					client.configureBlocking(false);
					selectorRegisterLock.lock();
					try
					{
						client.register(selector, SelectionKey.OP_READ, peer);
					}
					finally
					{
						selectorRegisterLock.unlock();
					}
					connectedPeers.put(peer);
					synchronized (newConnectedPeers)
					{
						newConnectedPeers.put(peer);
						newConnectedPeers.notify();
					}
				}
				catch (IOException e)
				{
					logger.info("Error: Can't register  selector to SocketChannel. Dropping connection...");
					dropConnection(client);
				}
				catch (InterruptedException e)
				{
					logger.info("Never happens.");
				}
			}
			catch (IOException e)
			{
				logger.info("Error: Can't connect to peer.");
			}
	}

	public void dropConnection(SocketChannel channel)
	{
		try
		{
			channel.close();
		}
		catch (IOException e)
		{
			logger.info("Can't close channel");
		}
	}

	public void processIncomingConnectionsAndMessages()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					serverSocket.configureBlocking(false);
					serverSocket.register(selector, SelectionKey.OP_ACCEPT);
				}
				catch (IOException e)
				{
					logger.info("Error: Can't use a selector.");
					System.out.println("Some problem happened. For more information look in a log file. Torrent client is terminated.");
					System.exit(1);
				}
				while (!stop)
				{
					try
					{
						int numSelected = 0;
						try
						{
							numSelected = selector.select(30);
						}
						catch(IOException e)
						{
							System.out.println("selector obgadilsyyy!");
							e.printStackTrace();
						}

						if (numSelected == 0)
						{
							selectorRegisterLock.lock();
							//	Gives other thread chance to register channel in selector.
							selectorRegisterLock.unlock();
//							Thread.yield();
							continue;
						}
						Set<SelectionKey> selectedKeys = selector.selectedKeys();
						Iterator<SelectionKey> iterator = selectedKeys.iterator();
						while (iterator.hasNext())
						{
							SelectionKey key = iterator.next();

							if (key.isAcceptable())
							{
								proccessConnection(key);
							}
							if (key.isReadable())
							{
								try
								{
									messageManager.receiveMessage((SocketChannel) key.channel(), (Peer) key.attachment());
								}
								catch (IOException e)
								{
									logger.info("EOF got. No data in channel. Disconnecting...");
									synchronized (connectedPeers)
									{
										connectedPeers.remove(key.attachment());
										connectedPeers.notify();
									}
									key.channel().close();
									key.cancel();
								}
							}
							if(!key.isValid())
							{
								System.out.println("key is not valid");
								logger.info("Key is not valid.");
								key.channel().close();
								key.cancel();
							}
							iterator.remove();
						}
					}
					catch (Exception e)
					{
						logger.info("Error: Can't process an incoming connection and message.");
					}
				}
			}
		}, "Input listener");

		thread.start();
	}

	public void stop()
	{
		stop = true;
		for(Peer peer: connectedPeers)
		{
			try
			{
				peer.getChannel().close();
			}
			catch (IOException e)
			{
				logger.info("Can't close channel");
			}
		}
	}
}
