package org.skimens.wakeonalarm;

import java.io.*;
import java.net.*;
import android.util.Log;

public class WakeOnLan {

    public static final int PORT = 9;

    public static void main(String ipStr,String macStr) {

//        if (args.length != 2) {
//            System.out.println("Usage: java WakeOnLan <broadcast-ip> <mac-address>");
//            System.out.println("Example: java WakeOnLan 192.168.0.255 00:0D:61:08:22:4A");
//            System.out.println("Example: java WakeOnLan 192.168.0.255 00-0D-61-08-22-4A");
//            System.exit(1);
//        }

        try {
            byte[] macBytes = getMacBytes(macStr);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipStr);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

            Log.v("WOL","Wake-on-LAN packet sent.");
        }
        catch (Exception e) {
            Log.e("WOL", "Failed to send Wake-on-LAN packet: + e");
        }

    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            Log.e("WOL","Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            Log.e("WOL","Invalid hex digit in MAC address.");
        }
        return bytes;
    }


}
