package com.ingenotech.lavalamp.ftdi;

import java.io.IOException;


public interface DeviceWriter {
	
	public void setLamp(boolean on) throws IOException;
	
	public void setBeep(boolean on) throws IOException;
}

