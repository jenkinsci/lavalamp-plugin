package com.ingenotech.lavalamp.ftdi;

public enum DeviceBits {
	BEEP (0x01),
	LAMP (0x08);
	
	private int bit;
	
	private DeviceBits(int bit) {
		this.bit = bit;
	}
	
	public static int set(int data, DeviceBits state) {
		return (data | state.bit);
	}

	public static int clear(int data, DeviceBits state) {
		return (data & (~state.bit));
	}

	public static int invert(int data, DeviceBits state) {
		if ((data & state.bit) != 0)
			return (data & (~state.bit));
		else
			return data | state.bit;
	}
}

