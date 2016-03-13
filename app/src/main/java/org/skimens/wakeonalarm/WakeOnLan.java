package org.skimens.wakeonalarm;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WakeOnLan extends AsyncTask<Void,String,Void> {


    private final int PORT = 9;
    private final String TAG = "WOL";
    private String IP;
    private String MAC;

    public WakeOnLan(String IP, String MAC){
        this.IP = IP;
        this.MAC = MAC;
    }


    @Override
    protected void onPreExecute() {}

    @Override
    protected Void doInBackground(Void... args) {
        Wake(IP,MAC);
        return null;
    };

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    public void Wake(String ipStr,String macStr) {

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

            Log.i(TAG,"Wake on LAN packet sent");
        }
        catch (Exception e) {
            Log.e(TAG, "Failed to send Wake on LAN packet: " + e.toString());
        }

    }

    private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            Log.e(TAG,"Invalid MAC address");
            throw new IllegalArgumentException("Invalid MAC address");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            Log.e(TAG,"Invalid hex digit in MAC address");
            throw new IllegalArgumentException("Invalid hex digit in MAC address");
        }
        return bytes;
    }


}
