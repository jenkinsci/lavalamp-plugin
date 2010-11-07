/*
 * $Id$
 * Created on 16 Oct 2010
 */

package com.ingenotech.lavalamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Handle an incoming network connection.
 * Parse the request message, send a response.
 */
public class ConnectionHandler {
	
	private static final String PING = "PING";
	private static final String QUIT = "QUIT";
	private static final String MUTE = "MUTE";
	private static final String BUILDNAME = "NAME";
	private static final String BUILDRESULT = "RESULT";

	
	private LavaLampServer	controller;

	
	public ConnectionHandler(LavaLampServer controller) {
		this.controller = controller;
	}

	
	public String handleConnection(DatagramPacket packet) {
		String data = new String(packet.getData(), packet.getOffset(), packet.getLength());
		Reader in = new StringReader( data );
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter( sw );
		handleConnection( in, out );
		out.flush();
		return sw.toString();
	}
	
	public void handleConnection(Socket socket) throws IOException {
		Reader in = new InputStreamReader( socket.getInputStream() );
		PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
		handleConnection( in, out );
	}
	
	
	/** Process request, write response */
	private void handleConnection(Reader in, PrintWriter out) {
		try {
			out.println(LavaLampServer.VERSION);
			
			String buildName = null;
			BuildStatus buildStatus = null;
			BufferedReader br = new BufferedReader(in);
			while (true) {
				String line = br.readLine();
				if (line == null) {
					// end-of-stream
					break;
				}
				
				String cmd = line.trim().toUpperCase();
				if (cmd.startsWith(QUIT)) {
					break;
					
				} else if (cmd.startsWith(PING)) {
					out.println(PING+" "+line.substring(PING.length()));
					
				} else if (cmd.startsWith(BUILDNAME)) {
					buildName = line.substring(BUILDNAME.length()).trim();
					if (buildName.length() < 1) {
						buildName = null;
						out.println(BUILDNAME+" requires a build name");
					}

				} else if (cmd.startsWith(BUILDRESULT)) {
					String result = cmd.substring(BUILDRESULT.length()).trim();
					try {
						buildStatus = BuildStatus.valueOf(result);
					} catch (IllegalArgumentException iax) {
						out.println(BUILDRESULT +" expects one of the following values:"+Arrays.asList(BuildStatus.values()));
					}

				} else if (cmd.startsWith(MUTE)) {
					boolean mute = Boolean.valueOf( cmd.substring(MUTE.length()).trim() );
					controller.setMute( mute );
					out.println(MUTE+" "+mute);
					
				} else if (cmd.length() > 0) {
					out.println("Unrecognised command: "+cmd);
					out.println("Expecting one of: "+PING+","+QUIT+","+BUILDNAME+","+BUILDRESULT+","+MUTE);
				}
			}

			if (buildName != null && buildStatus != null) {
				BuildState bs = new BuildState(buildName, buildStatus);
				controller.updateState(bs);
			}
			
		} catch (IOException iox) {
			Log.log("handleConnection", iox);
			
		} finally {
			Log.log("handleConnection done.");
		}
	}

}
