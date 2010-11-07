package com.ingenotech.lavalamp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;

public class UDPListener extends Thread implements Listener {
	
	private final String            name;
	private final ConnectionHandler	handler;
	private final InetAddress 		multicastGroup;
	private DatagramSocket 			server;
	private volatile boolean 		run;
	
	public UDPListener(LavaLampServer controller,
			           InetSocketAddress listenAddress) throws IOException {
		
		this.handler = new ConnectionHandler(controller);
		
		if (listenAddress.getAddress().isMulticastAddress()) {
		    this.multicastGroup = listenAddress.getAddress();
			MulticastSocket ms = new MulticastSocket(listenAddress.getPort());
		    ms.joinGroup(multicastGroup);
		    this.server = ms;
		    this.name = "UDPListener@Multicast:"+listenAddress.getAddress()+":"+listenAddress.getPort();
		    
		} else { 
			this.multicastGroup = null;
			this.server = new DatagramSocket(listenAddress);
		    this.name = "UDPListener@UDP:"+listenAddress.getAddress()+":"+listenAddress.getPort();
		}
		this.run = true;
	}
	
	
	public void close() {
		this.run = false;
		if (this.multicastGroup != null) {
			try {
				((MulticastSocket)server).leaveGroup(this.multicastGroup);
			} catch (IOException ex) {
			}
		}
		this.server.close();
	}
	
	
	public void run() {
		Log.log(this.name+" started.");
		try {
			while (run) {
				byte[] reqBuf = new byte[500];
				DatagramPacket reqPacket = new DatagramPacket(reqBuf, reqBuf.length);
				server.receive(reqPacket);
				SocketAddress senderAddr = reqPacket.getSocketAddress();
				Log.log(this.name+" packet from: "+senderAddr);
				String response = this.handler.handleConnection(reqPacket);
				if (response.length() > 0) {
					if (this.multicastGroup == null) {
						sendResponse(response, senderAddr);
					}
				}
			}
		} catch (IOException iox) {
			Log.log(this.name+" run(): socket exception:", iox);
		}
		
		Log.log(this.name+" exited.");
	}
	
	
	private void sendResponse(String response, SocketAddress address) throws IOException {
		byte[] respBuf = response.getBytes();
		DatagramPacket respPacket = new DatagramPacket(respBuf, respBuf.length, address);
		this.server.send(respPacket);
	}
	
}
