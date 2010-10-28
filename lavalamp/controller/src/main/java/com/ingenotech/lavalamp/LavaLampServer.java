package com.ingenotech.lavalamp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.ftdichip.ftd2xx.FTD2xxException;
import com.ingenotech.annotations.Version;
import com.ingenotech.lavalamp.ftdi.DeviceController;
import com.ingenotech.tools.VersionReader;

@Version(version="1.3")
public class LavaLampServer {

    public static final String VERSION;
    
    static {
    	String v = new VersionReader(LavaLampServer.class).getVersion();
    	String hostname = null;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
			if (hostname.startsWith("localhost"))
				hostname = null;
		} catch (UnknownHostException ex) {
		}
    	VERSION = "LavaLamp server"+(v == null ? "" : " V"+v)+(hostname == null ? "" : " @"+hostname);
    }

    private DeviceController device;
    private TCPListener      tcpListener;
    private UDPListener      udpListener;

    public LavaLampServer(InetSocketAddress tcpAddress,
                          InetSocketAddress udpAddress,
    		              InetAddress multicastAddress) throws FTD2xxException, IOException
    {
        device = new DeviceController();
        device.open();
        if (tcpAddress != null && tcpAddress.getPort() > 0) {
        	try {
            	tcpListener = new TCPListener(this, tcpAddress);
        	} catch (IOException iox) {
        		throw new IOException("TCPListener at: "+tcpAddress, iox);
        	}
        }
        
        if (udpAddress != null && udpAddress.getPort() > 0) {
        	try {
            	udpListener = new UDPListener(this, udpAddress, multicastAddress);
        	} catch (IOException iox) {
        		throw new IOException("UDPListener at: "+udpAddress+" (mc:"+multicastAddress+")", iox);
        	}
        }
    }


    public void updateState(BuildState bs) {
        Log.log("updateState("+bs+")");
        switch (bs.getStatus()) {
            case SUCCESS:
                device.redLampAlert(false);
                device.setBeep(false);
                device.setLamp(false);
                break;
            case FAILURE:
                device.redLampAlert(true);
                device.beepAlert1();
                break;
            case UNSTABLE:
                device.redLampAlert(false);
                device.setLamp(true);
                device.beepAlert2();
                break;
            case NOT_BUILT:
                device.redLampAlert(false);
                device.setLamp(true);
                device.beepAlert2();
                break;
            case ABORTED:
                device.redLampAlert(false);
                device.setLamp(true);
                device.beepAlert2();
                break;
        }
    }


    public void setMute(boolean mute) {
        device.setMute(mute);
    }


    public void close() {
        if (tcpListener != null)
            tcpListener.close();
        if (udpListener != null)
            udpListener.close();

        try {
            device.close();
        } catch ( FTD2xxException fx ) {
        }
    }


    public void run() {
        if (tcpListener != null)
            tcpListener.start();
        if (udpListener != null)
            udpListener.start();
        wait(new Object());
    }


    /** Block until interrupted */
    private void wait(Object lock) {
        synchronized(lock) {
            try {
                lock.wait();
            } catch (InterruptedException ix) {
            }
        }
    }


    public static void main(String[] argv) throws Exception {

        int tcpPort = 1999;
        try {
            if (argv.length >= 1)
            	tcpPort = Integer.parseInt(argv[0]);
        } catch (NumberFormatException nfx) {
        }

        int udpPort = tcpPort;
        try {
            if (argv.length >= 2)
            	udpPort = Integer.parseInt(argv[1]);
        } catch (NumberFormatException nfx) {
        }
        
        InetAddress mcAddress = null;
        try {
            if (argv.length >= 3)
            	mcAddress = InetAddress.getByName(argv[2]);
        } catch (NumberFormatException nfx) {
        }

        InetSocketAddress tcpAddress = new InetSocketAddress((InetAddress)null, tcpPort);
        InetSocketAddress udpAddress = new InetSocketAddress((InetAddress)null, udpPort);
        
        LavaLampServer bpa = null;
        try {
            Log.log(VERSION+" starting...");
            bpa = new LavaLampServer(tcpAddress, udpAddress, mcAddress);
            bpa.run();

        } finally {
            if (bpa != null)
                bpa.close();
            Log.log("exit.");
        }
    }
}
