package com.ingenotech.lavalamp.ftdi;

import java.io.IOException;

import com.ftdichip.ftd2xx.BitBangMode;
import com.ftdichip.ftd2xx.Device;
import com.ftdichip.ftd2xx.DeviceDescriptor;
import com.ftdichip.ftd2xx.FTD2xxException;
import com.ftdichip.ftd2xx.Service;
import com.ingenotech.lavalamp.Log;


public class DeviceController implements DeviceWriter {

	private Device		device;
	private int			currentData;
	private RedLamp		redLamp;
	private Beeper 		beeper;
	private boolean		mute = false;
	
	
	public DeviceController() {
	}

	
	private Device connect() throws IOException {
		try {
			Device[] devices = Service.listDevices();
			if (devices.length < 1)
				throw new IOException("FTDI device not found!");
			Device ftdi = devices[0];
			DeviceDescriptor desc = ftdi.getDeviceDescriptor();
			Log.log("Manufacturer: " +desc.getManufacturer());
			Log.log("Serial#: " +desc.getSerialNumber());
			Log.log("Product descr: " +desc.getProductDescription());
			return ftdi;
			
		} catch (FTD2xxException ftx) {
			throw new IOException("Unable to connect to FTDI device", ftx);
		}
	}
	
	
	public synchronized void open() throws IOException {
		try {
			if (device == null) {
				device = connect();
			}
			device.open();
			device.setBitBangMode(0x0f, BitBangMode.ASYNCHRONOUS);
			write(0x00);
		} catch (FTD2xxException ftx) {
			this.device = null;
			throw new IOException("open(): FTDI device disconnected", ftx);
		}
	}
	
	
	public synchronized void close() throws IOException {
		try {
			if (device != null) { 
				write(0x00);
				device.close();
			}

		} finally {
			device = null;
		}
	}

	
	private synchronized void write(int data) throws IOException {
		 try {
			 if (device == null)
				 open();
			 
			 data &= 0x0f;
			 //Log.log("Set  D0..D3 = 0x"+Integer.toHexString(data));
			 device.write(data);
			 this.currentData = data;
			 
		 } catch (FTD2xxException ftx) {
			 // I/O error - USB device unplugged?
			 device = null;
			 throw new IOException("write("+Integer.toHexString(data)+"): FTDI device disconnected", ftx);
		 }
	}
	
	private void set(DeviceBits bit) throws IOException {
		int data = DeviceBits.set(currentData, bit);
		write(data);
	}
	
	private void clear(DeviceBits bit) throws IOException {
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
	
	
	public synchronized void setLamp(boolean on) throws IOException {
		if (on) {
			set(DeviceBits.LAMP);
		} else {
			clear(DeviceBits.LAMP);
		}
	}
	
	
	public synchronized void setBeep(boolean on) throws IOException {
		if (on && !mute) {
			set(DeviceBits.BEEP);
		} else {
			clear(DeviceBits.BEEP);
		}
	}
	
	
	public void setMute(boolean mute) {
		Log.log("setMute("+mute+")");
		this.mute = mute;
	}
	
}
