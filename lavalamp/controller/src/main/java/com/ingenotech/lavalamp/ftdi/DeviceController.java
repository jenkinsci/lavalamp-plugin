package com.ingenotech.lavalamp.ftdi;

import com.ftdichip.ftd2xx.BitBangMode;
import com.ftdichip.ftd2xx.Device;
import com.ftdichip.ftd2xx.DeviceDescriptor;
import com.ftdichip.ftd2xx.FTD2xxException;
import com.ftdichip.ftd2xx.Service;
import com.ingenotech.lavalamp.Log;


public class DeviceController implements DeviceWriter {

	private Device	device;
	private int		currentData;
	private RedLamp	redLamp;
	private Beeper 	beeper;
	private boolean	mute = false;
	
	public DeviceController() throws FTD2xxException {

		 Device[] devices = Service.listDevices();
		 if (devices.length < 1)
			 throw new IllegalStateException("FTDI device not found!");
		 this.device = devices[0];
		 
		 DeviceDescriptor desc = device.getDeviceDescriptor();
		 Log.log("Manufacturer: " +desc.getManufacturer());
		 Log.log("Serial#: " +desc.getSerialNumber());
		 Log.log("Product descr: " +desc.getProductDescription());
	}

	
	public void open() throws FTD2xxException {
		device.open();
		device.setBitBangMode(0x0f, BitBangMode.ASYNCHRONOUS);
		write(0x00);
	}
	
	
	public void close() throws FTD2xxException {
		write(0x00);
		device.close();
	}

	
	private void write(int data) throws FTD2xxException {
		 data &= 0x0f;
		 //Log.log("Set  D0..D3 = 0x"+Integer.toHexString(data));
		 device.write(data);
		 this.currentData = data;
	}
	
	private void set(DeviceBits bit) throws FTD2xxException {
		int data = DeviceBits.set(currentData, bit);
		write(data);
	}
	
	private void clear(DeviceBits bit) throws FTD2xxException {
		int data = DeviceBits.clear(currentData, bit);
		write(data);
	}
	
	
	public synchronized void beepAlert1() {
		if (this.beeper == null || !this.beeper.isAlive()) {
			this.beeper = new Beeper1(this);
			this.beeper.start();
		}
	}
	
	public synchronized void beepAlert2() {
		if (this.beeper == null || !this.beeper.isAlive()) {
			this.beeper = new Beeper2(this);
			this.beeper.start();
		}
	}
	
	public synchronized void redLampAlert(boolean on) {
		if (on) {
			if (this.redLamp == null || !this.redLamp.isAlive()) {
				this.redLamp = new RedLamp(this);
				this.redLamp.start();
			}
		} else {
			if (this.redLamp != null) {
				this.redLamp.cancel();
			}
		}
	}
	
	
	public synchronized void setLamp(boolean on) {
		try {
			if (on) {
				set(DeviceBits.LAMP);
			} else {
				clear(DeviceBits.LAMP);
			}
		} catch (FTD2xxException fx) {
			Log.log("setLamp("+on+")", fx);
		}
	}
	
	
	public synchronized void setBeep(boolean on) {
		try {
			if (on && !mute) {
				set(DeviceBits.BEEP);
			} else {
				clear(DeviceBits.BEEP);
			}
		} catch (FTD2xxException fx) {
			Log.log("setBeep("+on+")", fx);
		}
	}
	
	
	public void setMute(boolean mute) {
		Log.log("setMute("+mute+")");
		this.mute = mute;
	}
	
}
