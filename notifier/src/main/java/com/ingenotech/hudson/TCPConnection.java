package com.ingenotech.hudson;

import hudson.model.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPConnection {
	private InetAddress 		server;
	private int 				port;
	private Socket				socket;
	private BufferedReader		reader;
	private PrintWriter			writer;
	private String				serverIdent;
	
	private static final String	PING = "PING";
	
	public TCPConnection(String host, int port) throws UnknownHostException {
		this(InetAddress.getByName(host), port);
	}
	
	
	public TCPConnection(InetAddress server, int port) {
		this.server = server;
		this.port = port;
	}
	
	public synchronized void open() throws IOException {
		if (this.socket == null) {
			this.socket = new Socket(server, port);
			this.reader = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
			this.writer = new PrintWriter( socket.getOutputStream(), true );
			this.serverIdent = this.reader.readLine();
		}
	}
	
	public synchronized void close() throws IOException {
		if (this.socket != null) {
			this.socket.close();
			this.socket = null;
		}
	}
	
	public String getServerIdent() {
		return this.serverIdent;
	}
	
	public boolean ping() throws IOException {
		writer.println(PING+":");
		String resp = reader.readLine();
		return (resp.toUpperCase().startsWith(PING));
	}
	
	public void sendResult(String buildName, Result result) {
		writer.println("NAME "+buildName);
		writer.println("RESULT "+result.toString());
		writer.println("QUIT");
	}
	
}
