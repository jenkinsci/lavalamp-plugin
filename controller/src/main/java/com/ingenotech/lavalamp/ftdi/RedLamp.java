package com.ingenotech.lavalamp.ftdi;

import java.io.IOException;

import com.ingenotech.lavalamp.Log;



public class RedLamp extends Thread {
	
	private static final long RED_DELAY = 2200;

	private DeviceWriter device;
	private volatile boolean run;
	
	public RedLamp(DeviceWriter device) {
		this.device = device;
		this.run = true;
	}
	
	public void cancel() {
		this.run = false;
		this.interrupt();
	}
	
	public void run() {
		try {
			while (run) {
				device.setLamp(false);
				device.setLamp(true);
				try {
					sleep(RED_DELAY);
				} catch (InterruptedException ix) {
				}
			}

		} catch (IOException ex) {
			Log.log("RedLamp: device error: "+ex.getMessage());
		}

	}
}
