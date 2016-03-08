package org.skimens.wakeonalarm;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class AddPC extends AppCompatActivity {

    TableLayout deviceList;

    String localIP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpc);
        deviceList = (TableLayout) findViewById(R.id.devicelist);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbaraddpc);
        setSupportActionBar(toolbar);

        WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int ip = wifiMan.getConnectionInfo().getIpAddress();
        Log.v("ip",String.valueOf(ip));

        if(ip != 0){
            localIP = String.valueOf(ip);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabaddpc);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDialog("", localIP, "");
            }
        });

        Log.v("WiFiIP",localIP);
        if(ip != 0){
        new NetworkDiscovery(this,localIP).execute(); }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void SearchNetwork(View v){

    }

    public void addDevice(String name,String IP,String MAC) {
        Log.v("addDevice",name + " " + IP + " " + MAC);
        DBHelper mDatabaseHelper = new DBHelper(this);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.DEVICE_NAME, name);
        values.put(DBHelper.DEVICE_IP, IP);
        values.put(DBHelper.DEVICE_MAC, MAC);
        db.insert(DBHelper.TABLE_DEVICE, null, values);
    }

//    public void addManually(String name,String IP,String MAC){
//        Log.v("addManually",name + " " + IP + " " + MAC);
//        nameedit.setText(name);
//        ipedit.setText(IP);
//        macedit.setText(MAC);
//        addDialog().show();
//    }
//
//    public void addManually(){
//        if(!localIP.equals(String.valueOf('0'))){
//        ipedit.setText(localIP);};
//        addDialog().show();
//    }

    public void addDialog(String name,String IP,String MAC){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add device");

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView nametext = new TextView(this);
        nametext.setText("\n Custom name for device: ");
        layout.addView(nametext);

        final EditText nameedit = new EditText(this);
        nameedit.setGravity(Gravity.CENTER);
        nameedit.setText(name);
        layout.addView(nameedit);

        TextView iptext = new TextView(this);
        iptext.setText(" IP address: ");
        layout.addView(iptext);

        final EditText ipedit = new EditText(this);
        ipedit.setInputType(InputType.TYPE_CLASS_TEXT);
        ipedit.setGravity(Gravity.CENTER);
        ipedit.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        ipedit.setText(IP);
        layout.addView(ipedit);

        TextView mactext = new TextView(this);
        mactext.setText(" MAC address: ");
        layout.addView(mactext);

        final EditText macedit = new EditText(this);
        macedit.setInputType(InputType.TYPE_CLASS_TEXT);
        macedit.setGravity(Gravity.CENTER);
        macedit.setText(MAC);
        layout.addView(macedit);

        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String name = nameedit.getText().toString().trim();
                String IP = ipedit.getText().toString().trim();
                String MAC = macedit.getText().toString().trim();
                // Check if IP address was submitted and it's valid
                if (IP.length() == 0) {
                    Log.v("IP", " is null");
                    Toast.makeText(AddPC.this, "Please, submit IP address", Toast.LENGTH_LONG).show();
                    addDialog(name, IP, MAC);
                    return;
                } else {
                    final Pattern IPPATTERN = Pattern.compile(
                            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
                    if (!IPPATTERN.matcher(IP).matches()) {
                        Log.v("IP", " is null");
                        Toast.makeText(AddPC.this, "IP address is not valid", Toast.LENGTH_LONG).show();
                        addDialog(name, IP, MAC);
                        return;
                    }
                }
                if (MAC.length() == 0) {
                    Log.v("MAC", " is null");
                    Toast.makeText(AddPC.this, "Please, submit MAC address", Toast.LENGTH_LONG).show();
                    addDialog(name, IP, MAC);
                    return;
                } else {
                    final Pattern MACPATTERN = Pattern.compile(
                            "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
                    if (!MACPATTERN.matcher(IP).matches()) {
                        Log.v("MAC", " is null");
                        Toast.makeText(AddPC.this, "MAC address is not valid", Toast.LENGTH_LONG).show();
                        addDialog(name, IP, MAC);
                        return;}
                    }

                    Log.v("name", "name is " + name);
                    if (name.length() == 0) {
                        Log.v("name", " is null");
                        Toast.makeText(AddPC.this, "Please, write custom name", Toast.LENGTH_LONG).show();
                        addDialog(name, IP, MAC);
                        return;
                    }

                    Log.v("IP", "end " + IP);
                    addDevice(name, IP, MAC);
                }
            }

            );
            builder.setCancelable(true);
            builder.show();
        }


        class NetworkDiscovery extends AsyncTask<Void,String,Void> {
        private Context context;
        public String IP;

        public NetworkDiscovery(Context c,String IP){
            context = c;
            this.IP = IP;
        };

        @Override
        protected void onPreExecute() {
            Log.v("PRE", "Call from preExecute");
//            deviceList.removeAllViewsInLayout();
}



        @Override
        protected Void doInBackground(Void... args) {
            String Mask = this.IP.substring(0, this.IP.lastIndexOf('.') + 1);
            Log.v("MASK",Mask);
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
            Log.v("onProgressAsync",IP + " " + MAC + " " + MAC);
            StringBuilder info = new StringBuilder(name);
            info.append("\n");
            info.append(IP);
            info.append("\n");
            info.append(MAC);
            TableRow tableRow = new TableRow(context);
            tableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            TextView cell = new TextView(context);
            final String value = info.toString();
            cell.setText(value);
            cell.setPadding(10, 0, 15, 5);
            cell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addDialog(name,IP,MAC);
                }
            });
            tableRow.addView(cell, 0);
            deviceList.addView(tableRow,deviceList.getChildCount()-1);
        }

//        @Override
//        protected void onPostExecute(String result) {   }

        public boolean ping(String IP) throws UnknownHostException, SocketException,IOException,InterruptedException {
            // ping range of several ips
            String cmd = String.format("/system/bin/ping -q -n -w 1 -c 1 %s",IP);
            try {
                Process proc = Runtime.getRuntime().exec(cmd);
                proc.waitFor();
                if(proc.exitValue() == 0){
                    return true;
                }}
            catch (Exception e) {
                Log.v("PING",e.toString());
            }
            return false;
        };


        public String getMac(String IP){
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
                            Log.v("MAC","NOT FOUND");
                            return null;
                        }
                    }
                }
            } catch (Exception e) {
                Log.v("MAC",e.toString());
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.v("MAC",e.toString());
                } }
            Log.v("MAC","Failed to open  /proc/net/arp/");
            return null;
        };

    }

}