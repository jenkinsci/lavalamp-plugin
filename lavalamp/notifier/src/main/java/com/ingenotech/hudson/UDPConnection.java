package com.ingenotech.hudson;

import hudson.model.Result;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPConnection implements Connection {
	
	private static final Logger LOG = Logger.getLogger(UDPConnection.class.getName());

	private InetAddress 		server;
	private int 				port;
	private DatagramSocket	    socket;
	private String				serverIdent;
	
	private static final int TIMEOUT = 20000;
	
	
	public UDPConnection(String host, int port) throws UnknownHostException {
		this(InetAddress.getByName(host), port);
	}
	
	
	public UDPConnection(InetAddress server, int port) {
		this.server = server;
		this.port = port;
		this.serverIdent = "LavaLamp@"+server+":UDP:"+port;
	}
	
	public synchronized void open() throws IOException {
		if (this.socket == null) {
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT);
            socket.connect(server, port);
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
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(PING+" "+new java.util.Date().toString());
		pw.flush();
		send(sw.toString());
		String rec = receive();
		int px = rec.indexOf(PING);
		if (px > 0) {
			this.serverIdent = rec.substring(0, px).trim();
			return true;
		}
		return false;
	}
	
	public void sendResult(String buildName, Result result) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(NAME+" "+buildName);
		pw.println(RESULT+" "+result.toString());
		pw.println(QUIT);
		pw.flush();
		try {
			send(sw.toString());
		} catch (IOException iox) {
			LOG.log(Level.SEVERE, "Unable to sendResult to "+serverIdent, iox);
		}
	}
	
	private void send(String msg) throws IOException {
        byte[] buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.send(packet);
	}
	
	private String receive() throws IOException {
        byte[] buffer = new byte[500];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		return new String(packet.getData(), packet.getOffset(), packet.getLength());
	}
	
}
