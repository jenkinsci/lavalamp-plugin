package com.ingenotech.lavalamp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPListener extends Thread implements Listener {
	
	private final String		name;
	private ConnectionHandler 	handler;
	private ServerSocket 		server;
	private volatile boolean 	run;
	
	public TCPListener(LavaLampServer controller,
	                   InetSocketAddress listenAddress) throws IOException {
		this.handler = new ConnectionHandler(controller);
		this.server = new ServerSocket();
		this.server.bind(listenAddress);
	    this.name = "TCPListener@"+listenAddress.getAddress()+":"+listenAddress.getPort();
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
		Log.log(this.name+" started.");
		try {
			while (run) {
				Socket s = server.accept();
				Log.log(this.name+" conection from: "+s.getRemoteSocketAddress());
				this.handler.handleConnection(s);
				try {
					s.close();
				} catch (IOException iox) {
					Log.log(this.name+" run()", iox);
				}
				
			}
		} catch (IOException iox) {
			Log.log(this.name+" run() socket exception:", iox);
		}
		Log.log(this.name+" exited.");
	}
	
}
