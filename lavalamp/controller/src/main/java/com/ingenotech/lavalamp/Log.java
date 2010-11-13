package com.ingenotech.lavalamp;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	private static PrintStream log = System.out;
	private static final DateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
	
	public static void log(String msg) {
		log(msg, null);
	}

	public static void log(Throwable exception) {
		log(null, exception);
	}

	public static void log(String msg, Throwable exception) {
		if (msg != null)
			synchronized (df) {
				msg = df.format(new Date())+" "+msg;
			}
			log.println(msg);
		if (exception != null)
			exception.printStackTrace(log);
		
	}
}
