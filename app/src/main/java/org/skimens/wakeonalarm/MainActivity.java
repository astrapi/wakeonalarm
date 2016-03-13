package org.skimens.wakeonalarm;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Main";

    private LinearLayout deviceList;
    private Resources RS;
    private final DBHelper db = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        deviceList = (LinearLayout) findViewById(R.id.devicelist);
        deviceList.setDividerPadding(1);
        RS = getResources();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPC(view);
            }
        });
    }

    /*
    OnResume executes each time when activity become on top
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateDeviceList();}

    public void addPC(View view) {
        Intent addPC = new Intent(MainActivity.this,AddDeviceActivity.class);
        startActivity(addPC);
    }

    /*
    Call for dialog frame with option to edit or delete device
     */
    public void onDeviceClick(Integer id,final String name,final String IP,final String MAC){
        final String DID = String.valueOf(id);
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(RS.getString(R.string.select_action));
        ad.setMessage(name + " ( " + IP + " )");
        ad.setPositiveButton(RS.getString(R.string.edit), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                editDialog(DID, name, IP, MAC);
            }
        });
        // deletes device from device and alarm table
        // system alarm which exist in android broadcast will be handle in AlarmReceiver class
        ad.setNegativeButton(RS.getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                SQLiteDatabase sdb = db.getWritableDatabase();
                sdb.delete(DBHelper.TABLE_ALARM, DBHelper.ALARM_DEVICE_ID + "=" + DID, null);
                sdb.delete(DBHelper.TABLE_DEVICE, DBHelper._ID + "=" + DID, null);
                sdb.close();
                updateDeviceList();
                Toast.makeText(MainActivity.this, RS.getString(R.string.device_was_deleted),
                        Toast.LENGTH_LONG).show();
            }
        });
        ad.setCancelable(true);
        ad.create();
        ad.show();}


        public void editDialog(final String DID, String name, String IP, String MAC) {
            // Shows dialog for editing device information
            // Validates parameters before call to db insert
            final setDeviceDialog dial = new setDeviceDialog(this,name, IP, MAC);
            AlertDialog.Builder ad = dial.getDialog();
            ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (dial.checkSetDialog()) {
                        dial.process(DID);
                        Toast.makeText(MainActivity.this, RS.getString(R.string.device_was_updated),
                                Toast.LENGTH_LONG).show();
                        updateDeviceList();
                    } else {
                        editDialog(DID, dial.getName(), dial.getIP(), dial.getMAC());
                    }
                }
            });
            ad.create();
            ad.show();}

        /*
        Updates information in deviceList layout
         */
        public void updateDeviceList(){

            deviceList.removeAllViews();

            SQLiteDatabase sdb = db.getReadableDatabase();
            String query = "SELECT * FROM " + DBHelper.TABLE_DEVICE;
            Cursor cursor = sdb.rawQuery(query, null);
            while (cursor.moveToNext()) {
                final int id = cursor.getInt(cursor
                        .getColumnIndex(DBHelper._ID));
                final String name = cursor.getString(cursor
                        .getColumnIndex(DBHelper.DEVICE_NAME));
                final String IP = cursor.getString(cursor
                        .getColumnIndex(DBHelper.DEVICE_IP));
                final String MAC = cursor.getString(cursor
                        .getColumnIndex(DBHelper.DEVICE_MAC));
                Log.i(TAG, "Devices list item: id=" + id + " name=" + name + " ip=" + IP + " mac=" + MAC);
                deviceLayout dl = new deviceLayout(MainActivity.this,name,IP,MAC);
                LinearLayout mainLayout = dl.getLayout(id);
                deviceList.addView(mainLayout, deviceList.getChildCount() - 1);
            }
            sdb.close();

    }


    }



