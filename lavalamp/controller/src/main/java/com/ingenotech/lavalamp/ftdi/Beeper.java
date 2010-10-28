package com.ingenotech.lavalamp.ftdi;

public interface Beeper {

	public void start();
	
	public void cancel();

	public boolean isAlive();
	
	public void setRunFor(long msDuration);
}
