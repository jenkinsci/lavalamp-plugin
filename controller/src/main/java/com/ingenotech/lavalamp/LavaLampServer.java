package com.ingenotech.lavalamp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

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

    private DeviceController controller;
    private List<Listener>	 listeners;

    public LavaLampServer(InetSocketAddress tcpAddress,
                          InetSocketAddress udpAddress,
    		              InetSocketAddress multicastAddress) throws IOException
    {
        controller = new DeviceController();
        controller.open();
        
        listeners = new LinkedList<Listener>();
        
        if (tcpAddress != null && tcpAddress.getPort() > 0) {
        	try {
            	listeners.add( new TCPListener(this, tcpAddress) );
        	} catch (IOException iox) {
        		throw new IOException("TCPListener at: "+tcpAddress, iox);
        	}
        }
        
        if (udpAddress != null && udpAddress.getPort() > 0) {
        	try {
            	listeners.add( new UDPListener(this, udpAddress) );
        	} catch (IOException iox) {
        		throw new IOException("UDPListener at: "+udpAddress, iox);
        	}
        }
        
        if (multicastAddress != null && multicastAddress.getPort() > 0) {
        	try {
            	listeners.add( new UDPListener(this, multicastAddress) );
        	} catch (IOException iox) {
        		throw new IOException("Multicast Listener at: "+multicastAddress, iox);
        	}
        }
    }


    public void updateState(BuildState bs) {
		Log.log("updateState("+bs+")");
    	try {
    		switch (bs.getStatus()) {
    			case SUCCESS:
    				controller.redLampAlert(false);
    				controller.setBeep(false);
    				controller.setLamp(false);
    				break;
    			case FAILURE:
    				controller.redLampAlert(true);
    				controller.beepAlert1();
    				break;
    			case UNSTABLE:
    				controller.redLampAlert(false);
    				controller.setLamp(true);
    				controller.beepAlert2();
    				break;
    			case NOT_BUILT:
    				controller.redLampAlert(false);
    				controller.setLamp(true);
    				controller.beepAlert2();
    				break;
    			case ABORTED:
    				controller.redLampAlert(false);
    				controller.setLamp(true);
    				controller.beepAlert2();
    				break;
    		}
    	} catch (IOException iox) {
    		Log.log("UpdateState("+bs+"): "+iox.getMessage());
    	}
    }


    public void setMute(boolean mute) {
        controller.setMute(mute);
    }


    public void close() {
    	for (Listener li : listeners) {
    		li.close();
    	}
    	listeners.clear();

        try {
            controller.close();
        } catch ( IOException fx ) {
        }
    }


    public void run() {
    	for (Listener li : listeners) {
    		li.start();
    	}
        
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


    /**
     * LavaLampServer [TCPPort] [UDPPort] [MCAddr] [MCPort]
     * TCPPort - Port to listen for TCP connections (or 0 to disaable TCP) (default:1999)
     * UDPPort - Port to listen for UDP connections (or 0 to disable UDP) (default: same as TCPPort)
     * MCAddr -  Multicast Address to listen on (default: multicast disabled)
     * MCPort -  Port to listen for Multicast (default: same as UDPPort if multicast enabled) 
     */
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
        
        InetAddress mcAddr = null;
        try {
            if (argv.length >= 3)
            	mcAddr = InetAddress.getByName(argv[2]);
        } catch (NumberFormatException nfx) {
        }

        int mcPort = (mcAddr != null ? udpPort : 0);
        try {
            if (argv.length >= 4)
            	mcPort = Integer.parseInt(argv[3]);
        } catch (NumberFormatException nfx) {
        }

        InetSocketAddress tcpAddress = new InetSocketAddress((InetAddress)null, tcpPort);
        InetSocketAddress udpAddress = new InetSocketAddress((InetAddress)null, udpPort);
        InetSocketAddress mcAddress = new InetSocketAddress(mcAddr, mcPort);
        
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
