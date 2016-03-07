package org.skimens.wakeonalarm;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.net.wifi.WifiInfo;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;


public class MainActivity extends AppCompatActivity {

    TableLayout deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        deviceList = (TableLayout) findViewById(R.id.devicelist);

        DBHelper db = new DBHelper(this);
        SQLiteDatabase sdb = db.getReadableDatabase();

        String query = "SELECT * FROM " + DBHelper.TABLE_DEVICE;
        Cursor cursor2 = sdb.rawQuery(query, null);
        while (cursor2.moveToNext()) {
            int id = cursor2.getInt(cursor2
                    .getColumnIndex(DBHelper._ID));
            final String name = cursor2.getString(cursor2
                    .getColumnIndex(DBHelper.DEVICE_NAME_COLUMN));
            final String IP = cursor2.getString(cursor2
                    .getColumnIndex(DBHelper.DEVICE_IP_COLUMN));
            final String MAC = cursor2.getString(cursor2
                    .getColumnIndex(DBHelper.DEVICE_MAC_COLUMN));
            Log.v("CURSOR", "ROW " + id + " HAS NAME " + name + " " + IP + " " + MAC);

            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView cell = new TextView(this);
            cell.setText(name + "\n" + IP + "\n" + MAC);
            cell.setPadding(10, 0, 15, 5);
            Button alarmbt = new Button(this);
            alarmbt.setText("Set Alarm");
            alarmbt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent TimePickerActivity = new Intent(MainActivity.this,TimePickerActivity.class);
                    TimePickerActivity.putExtra("IP", IP);
                    TimePickerActivity.putExtra("name", name);
                    TimePickerActivity.putExtra("MAC", MAC);
                    startActivity(TimePickerActivity);
                }
            });
            Button wakebt = new Button(this);
            wakebt.setText("Wake");
            wakebt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new WakeOnLan(IP, MAC).execute();
                    Toast.makeText(getBaseContext(), "Wake signal send to " + name, Toast.LENGTH_LONG).show();
                }
            });
            tableRow.addView(cell, 0);
            tableRow.addView(alarmbt, 1);
            tableRow.addView(wakebt, 1);
            deviceList.addView(tableRow, deviceList.getChildCount() - 1);
        }
        cursor2.close();



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPC(view);
            }
        });



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


    public void addPC(View view) {
        Intent addPC = new Intent(MainActivity.this,AddPC.class);
        startActivity(addPC);

  }


}
