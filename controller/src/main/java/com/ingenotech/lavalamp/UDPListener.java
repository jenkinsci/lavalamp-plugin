package com.ingenotech.lavalamp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class UDPListener extends Thread {
	
	private ConnectionHandler handler;
	private DatagramSocket server;
	private InetAddress multicastGroup;
	private volatile boolean run;
	
	public UDPListener(LavaLampServer controller,
			           InetSocketAddress listenAddress,
			           InetAddress multicastGroup) throws IOException {
		this.handler = new ConnectionHandler(controller);
		if (multicastGroup != null && multicastGroup.isMulticastAddress()) {
			MulticastSocket ms = new MulticastSocket(listenAddress);
		    ms.joinGroup(multicastGroup);
		    this.multicastGroup = multicastGroup;
		    this.server = ms;
		} else { 
			this.server = new DatagramSocket(listenAddress);
		}
		this.run = true;
	}
	
	
	public void close() {
		this.run = false;
		this.server.close();
	}
	
	
	public void run() {
		Log.log("UDPListener started on: "+server.getLocalSocketAddress() +(multicastGroup != null ? " (multicast group:"+multicastGroup+")" : ""));
		try {
			while (run) {
				byte[] buffer = new byte[500];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				server.receive(packet);
				this.handler.handleConnection(packet);
			}
		} catch (IOException iox) {
			Log.log("UDPListener.run() socket exception:", iox);
		}
		Log.log("UDPListener exited.");
	}
	
}
