/*
 * $Id$
 * Created on 28 Oct 2010
 */

package com.ingenotech.hudson;

import hudson.model.Result;

import java.io.IOException;

/**
 * Socket connection to a LavaLampController
 */
public interface Connection {
	
	public static final String	PING 	= "PING";
	public static final String	NAME 	= "NAME";
	public static final String	RESULT 	= "RESULT";
	public static final String	QUIT 	= "QUIT";

	public void open() throws IOException;
	
	public void close() throws IOException;
	
	public boolean ping() throws IOException;

	public String getServerIdent();

	public void sendResult(String buildName, Result result);

}
