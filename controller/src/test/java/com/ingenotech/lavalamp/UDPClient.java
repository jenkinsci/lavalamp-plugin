package com.ingenotech.lavalamp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;


/**
 * Simple UDP client for testing LavaLampServer
 * @author Ed
 */
public class UDPClient {

    public static void main(String[] argv) throws Exception {

        InetAddress address = InetAddress.getLocalHost();
        try {
            if (argv.length >= 1)
                address = InetAddress.getByName(argv[0]);
        } catch (NumberFormatException nfx) {
        }
        
        int port = 1999;
        try {
            if (argv.length >= 2)
                port = Integer.parseInt(argv[1]);
        } catch (NumberFormatException nfx) {
        }
        
        Log.log("Connecting to LavaLampServer at UDP: "+address+":"+port);

        final String NL = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        sb.append("NAME testbuild"+NL);
        //sb.append("RESULT FAILURE"+NL);
        //sb.append("RESULT UNSTABLE"+NL);
        sb.append("RESULT SUCCESS"+NL);
        //sb.append("RESULT UNSTABLE"+NL);
        //sb.append("QUIT"+NL);
        byte[] buffer = sb.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        
        DatagramSocket socket = null;
        try {
            socket = new MulticastSocket();
            socket.send(packet);

        } finally {
            if (socket != null)
                socket.close();
            Log.log("exit.");
        }
    }
}
