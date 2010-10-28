package com.ingenotech.lavalamp.ftdi;



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
		while (run) {
			device.setLamp(false);
			device.setLamp(true);
			try {
				sleep(RED_DELAY);
			} catch (InterruptedException ix) {
			}
		}
	}
}
