package com.ingenotech.lavalamp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPListener extends Thread {
	
	private ConnectionHandler handler;
	private ServerSocket server;
	private volatile boolean run;
	
	public TCPListener(LavaLampServer controller,
	                   InetSocketAddress listenAddress) throws IOException {
		this.handler = new ConnectionHandler(controller);
		this.server = new ServerSocket();
		this.server.bind(listenAddress);
		this.run = true;
	}
	
	
	public void close() {
		try {
			this.run = false;
			this.server.close();
		} catch (IOException iox) {
		}
	}
	
	
	public void run() {
		Log.log("TCPListener started on: "+server.getLocalSocketAddress());
		try {
			while (run) {
				Socket s = server.accept();
				this.handler.handleConnection(s);
				try {
					s.close();
				} catch (IOException iox) {
					Log.log("TCPListener.run()", iox);
				}
				
			}
		} catch (IOException iox) {
			Log.log("TCPListener.run() socket exception:", iox);
		}
		Log.log("TCPListener exited.");
	}
	
}
