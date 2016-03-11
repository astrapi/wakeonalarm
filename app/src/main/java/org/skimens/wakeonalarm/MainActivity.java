package org.skimens.wakeonalarm;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    LinearLayout deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        deviceList = (LinearLayout) findViewById(R.id.devicelist);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPC(view);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();}

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


    public void onLongTap(Integer id,final String name,final String IP,final String MAC){
        final String DID = String.valueOf(id);
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("Choose action");
        ad.setMessage(name + " ( " + IP + " )");
        ad.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                editDialog(DID, name, IP, MAC);
            }
        });
        ad.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                DBHelper mDatabaseHelper = new DBHelper(MainActivity.this);
                SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
                db.delete(DBHelper.TABLE_ALARM, DBHelper.ALARM_DEVICE_ID + "=" + DID, null);
                db.delete(DBHelper.TABLE_DEVICE, DBHelper._ID + "=" + DID, null);
                db.close();
                updateList();
                Toast.makeText(MainActivity.this, "Device was deleted",
                        Toast.LENGTH_LONG).show();
            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        });
        ad.create();
        ad.show();

    }


        public void editDialog(final String DID, String name, String IP, String MAC) {
            // Shows dialog for adding device information
            // Validates parameters before call to db insert
            Log.v("editDialog",name + " " + IP + " " + MAC);
            final setDeviceDialog dial = new setDeviceDialog(this,name, IP, MAC);
            AlertDialog.Builder ad = dial.getDialog();
            ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (dial.checkSetDialog()) {
                        dial.process(DID);
                        Toast.makeText(MainActivity.this, "Device was updated",
                                Toast.LENGTH_LONG).show();
                        updateList();
                    } else {
                        editDialog(DID, dial.getName(), dial.getIP(), dial.getMAC());
                    }
                    ;
                }
            });
            ad.create();
            ad.show();
            }


        public void updateList(){

            deviceList.removeAllViews();
            deviceList.setDividerPadding(1);
            DBHelper db = new DBHelper(this);
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
                Log.v("CURSOR", "ROW " + id + " HAS NAME " + name + " " + IP + " " + MAC);
                deviceLayout dl = new deviceLayout(MainActivity.this,name,IP,MAC);
                LinearLayout mainLayout = dl.getLayout(id);
                deviceList.addView(mainLayout, deviceList.getChildCount() - 1);
                Log.v("lol",String.valueOf(deviceList.getChildCount()));
            }
            cursor.close();

    }


    }



