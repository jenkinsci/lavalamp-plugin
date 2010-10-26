package com.ingenotech.lavalamp.ftdi;



public class Beeper1 extends Thread implements Beeper {
	
	private DeviceWriter device;
	private long runFor = 6000;
	private volatile boolean run = true;

	public Beeper1(DeviceWriter device) {
		this.device = device;
	}
	
	public void setRunFor(long msDuration) {
		this.runFor = msDuration;
	}
	
	public void cancel() {
		this.run = false;
		this.interrupt();
	}
	
	public void run() {
		long start = System.currentTimeMillis();
		long delay = 100;
		long minDelay = 30;
		int delta = 3;
		long runFor2 = (runFor >> 2);
		long runFor1 = runFor - runFor2;

		boolean beep = true;
		do {
			device.setBeep(beep);
			beep = !beep;
			delay -= delta;
			if (delay <= minDelay)
				delay = minDelay;
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ix) {
			}
		} while (run && (System.currentTimeMillis()-start) < runFor1);
		
		if (run) {
			device.setBeep(true);
			try {
				Thread.sleep(runFor2);
			} catch (InterruptedException ix) {
			}
		}
		
		device.setBeep(false);
	}
}
