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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class AddPC extends AppCompatActivity {

    TableLayout deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpc);
        deviceList = (TableLayout) findViewById(R.id.devicelist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbaraddpc);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabaddpc);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addManually(view);
            }
        });

        WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        String IP = String.valueOf(wifiInf.getIpAddress());
        Log.v("WiFiIP",IP);
        if(!IP.equals(String.valueOf('0'))){
        new NetworkDiscovery(this,IP).execute(); }
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
        values.put(DBHelper.DEVICE_NAME_COLUMN, name);
        values.put(DBHelper.DEVICE_IP_COLUMN, IP);
        values.put(DBHelper.DEVICE_MAC_COLUMN, MAC);
        db.insert(DBHelper.TABLE_DEVICE, null, values);
    }


    public void addManually(View v){
        final View view = v;

        String title = "Add device manually";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);


        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView nametext = new TextView(this);
        nametext.setText("\n Custom name for device: ");
        layout.addView(nametext);

        final EditText nameedit = new EditText(this);
        nameedit.setGravity(Gravity.CENTER);
        layout.addView(nameedit);

        final TextView iptext = new TextView(this);
        iptext.setText(" IP address: ");
        layout.addView(iptext);

        final EditText ipedit = new EditText(this);
        ipedit.setInputType(InputType.TYPE_CLASS_TEXT);
        ipedit.setGravity(Gravity.CENTER);
        ipedit.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        layout.addView(ipedit);

        final TextView mactext = new TextView(this);
        mactext.setText(" MAC address: ");
        layout.addView(mactext);

        final EditText macedit = new EditText(this);
        macedit.setInputType(InputType.TYPE_CLASS_TEXT);
        macedit.setGravity(Gravity.CENTER);
        layout.addView(macedit);


        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // add check later on
                String name = nameedit.getText().toString().trim();
                String IP = ipedit.getText().toString().trim();
                String MAC = macedit.getText().toString().trim();
                Log.v("Name", name);
                Log.v("IP", IP);
                Log.v("Name", name);
                addDevice(name, IP, MAC);
            }
        });
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
                        StringBuilder info = new StringBuilder(InetAddress.getByName(IP).getHostName());
                        info.append("\n");
                        info.append(IP);
                        info.append("\n");
                        info.append(getMac(IP));
                        publishProgress(info.toString());
                    } else {Log.v("Failed IP",IP);}}
                catch (Exception e) {
                    Log.v("PING",e.toString());
                }


            }
            return null;

        };

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.v("onProgressAsync",values[0]);
            TableRow tableRow = new TableRow(context);
            tableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            TextView cell = new TextView(context);
            cell.setText(values[0]);
            cell.setPadding(10, 0, 15, 5);
            tableRow.addView(cell, 0);
            deviceList.addView(tableRow,deviceList.getChildCount()-1);
        }

//        @Override
//        protected void onPostExecute(String result) {   }

        private boolean ping(String IP) throws UnknownHostException, SocketException,IOException,InterruptedException {
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