package org.skimens.wakeonalarm;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

public class AddDeviceActivity extends AppCompatActivity {

    private final String TAG = "AddDevice";

    LinearLayout deviceList;

    String localIP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpc);
        deviceList = (LinearLayout) findViewById(R.id.devicelist);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbaraddpc);
        setSupportActionBar(toolbar);

        Menu renew = (Menu) findViewById(R.id.action_settings);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabaddpc);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDialog("", localIP, "");
            }
        });

        scanNetwork();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
    Get IP of android phone from WIFI to search LAN for available devices
     */
    public void scanNetwork(){
        WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        Log.v(TAG, "WiFi IP is" + String.valueOf(ipAddress));
        if(ipAddress != 0){
            localIP = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
            new NetworkDiscovery(this,localIP).execute();
        } else {
        Toast.makeText(this, getResources().getString(R.string.wifi_not_found), Toast.LENGTH_LONG).show();}
    }

    /*
    Call network again
     */
    public void renew(MenuItem item){
        deviceList.removeAllViewsInLayout();
        scanNetwork();
    }

    /*
       Shows dialog for adding device information
       Validates parameters before call to db insert
     */
    public void addDialog(String name,String IP,String MAC){

        final setDeviceDialog dial = new setDeviceDialog(this,name, IP, MAC);
        AlertDialog.Builder ad = dial.getDialog();
        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dial.checkSetDialog()) {
                    dial.process();
                } else {
                    addDialog(dial.getName(),dial.getIP(),dial.getMAC());
                }
            }
        });
        ad.create();
        ad.show();
        }


    /*
          inner class for processing async operation
          class search for device in LAN
          and return name, ip, mac to addPC activity
     */

        class NetworkDiscovery extends AsyncTask<Void,String,Void> {
        private Context context;
        public String IP;

        public NetworkDiscovery(Context c,String ip){
            context = c;
            IP = ip;
        };

        @Override
        protected Void doInBackground(Void... args) {
            String Mask = this.IP.substring(0, this.IP.lastIndexOf('.') + 1);
            Log.v("MASK", Mask);
            for (int i=0; i < 255;i++){
                String IP = Mask + String.valueOf(i);
                try {
                    if(ping(IP)){
                        publishProgress(IP,InetAddress.getByName(IP).getHostName());
                    } else {Log.v("Ping failed",IP);}}
                catch (Exception e) {
                    Log.v("PING",e.toString());
                }


            }
            return null;

        };

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            final String IP = values[0];
            final String name = values[1];
            final String MAC = getMac(IP);
            if(MAC == null){return;};
            String info = name + "\n" + IP + " (" + MAC + ")";
            Log.v("onProgressUpdate","Got result " + name);
            deviceLayout dl = new deviceLayout(context,name,IP,MAC);
            LinearLayout mainLayout = dl.getLayout();
            deviceList.addView(mainLayout, deviceList.getChildCount() - 1);
        }

        public boolean ping(String IP) throws IOException,InterruptedException {
            // because InetAddresses.isReachable not work in some type of LAN (routers)
            // have to use call to unix's ping
            String cmd = String.format("/system/bin/ping -q -n -w 1 -c 1 %s", IP);
            try {
                Process proc = Runtime.getRuntime().exec(cmd);
                proc.waitFor();
                if(proc.exitValue() == 0){
                    return true;
                }}
            catch (Exception e) {
                Log.i(TAG,"Ping " + e.toString());
            }
            return false;
        };


        public String getMac(String IP){
            // We can get information about device MAC & etc from file /proc/net/arp
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader("/proc/net/arp"));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] splitted = line.split(" +");
                    if (splitted != null && splitted.length >= 4 && IP.equals(splitted[0])) {
                        // Basic sanity check
                        String mac = splitted[3];
                        if (mac.matches("..:..:..:..:..:..")) {
                            return mac;
                        } else {
                            Log.e(TAG, "For IP - MAC not found in /proc/net/arp");
                            return null;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG,"getMac " + e.toString());

            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.e(TAG, "getMac " + e.toString());
                } }
            Log.v(TAG,"Failed to open  /proc/net/arp/");
            return null;
        };

    }

}