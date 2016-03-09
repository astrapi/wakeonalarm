package org.skimens.wakeonalarm;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

public class setDeviceDialog {

    public static Context context;

    public static AlertDialog.Builder builder;

    public static EditText nameedit;
    public static EditText ipedit;
    public static EditText macedit;

    public setDeviceDialog(Context c,String name,String IP,String MAC){
        Log.v("Call","init");
        context = c;
        builder = new AlertDialog.Builder(context);

        builder.setTitle("Add device");

        LinearLayout layout = new LinearLayout(context);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView nametext = new TextView(context);
        nametext.setText("\n Custom name for device: ");
        layout.addView(nametext);

        nameedit = new EditText(context);
        nameedit.setGravity(Gravity.CENTER);
        nameedit.setText(name);
        layout.addView(nameedit);

        TextView iptext = new TextView(context);
        iptext.setText(" IP address: ");
        layout.addView(iptext);

        ipedit = new EditText(context);
        ipedit.setInputType(InputType.TYPE_CLASS_TEXT);
        ipedit.setGravity(Gravity.CENTER);
        ipedit.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        ipedit.setText(IP);
        layout.addView(ipedit);

        TextView mactext = new TextView(context);
        mactext.setText(" MAC address: ");
        layout.addView(mactext);

        macedit = new EditText(context);
        macedit.setInputType(InputType.TYPE_CLASS_TEXT);
        macedit.setGravity(Gravity.CENTER);
        macedit.setText(MAC);

        layout.addView(macedit);
        builder.setView(layout);
        builder.setCancelable(true);

    }



    public static boolean checkSetDialog(){
        // Check if IP address was submitted and it's valid
        String name = nameedit.getText().toString().trim();
        String IP = ipedit.getText().toString().trim();
        String MAC = macedit.getText().toString().trim();

        if (IP.length() == 0) {
            Log.v("IP", " is null");
//            Toast.makeText(context, "Please, submit IP address", Toast.LENGTH_LONG).show();
            return false;
        } else {
            final Pattern IPPATTERN = Pattern.compile(
                    "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
            if (!IPPATTERN.matcher(IP).matches()) {
                Log.v("IP", " is null");
                Toast.makeText(context, "IP address is not valid", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        if (MAC.length() == 0) {
            Log.v("MAC", " is null");
            Toast.makeText(context, "Please, submit MAC address", Toast.LENGTH_LONG).show();
            return false;
        } else {
            final Pattern MACPATTERN = Pattern.compile(
                    "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
            if (!MACPATTERN.matcher(MAC).matches()) {
                Log.v("MAC", " is null");
                Toast.makeText(context, "MAC address is not valid", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        Log.v("name", "name is " + name);
        if (name.length() == 0) {
            Log.v("name", " is null");
            Toast.makeText(context, "Please, write custom name", Toast.LENGTH_LONG).show();
            return false;
        }

        Log.v("IP", "end " + IP);
        return true;

    }


    public AlertDialog.Builder create(final String DID){
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (checkSetDialog()) {
                    process(DID);
                } else {
                    builder.show();
                }
                ;
            }
        });
        builder.create();
        return builder;
    }

    public void show(){
        builder.show();
    }

    public AlertDialog.Builder create(){
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (checkSetDialog()) {
                    process();
                } else {
                    Log.v("Show","show");
                    builder.show();
                }
                ;
            }
        });
        builder.create();
        return builder;
    }


    public void process(){
        // Store device parameters to database
        // All checks are done before method call
        DBHelper mDatabaseHelper = new DBHelper(context);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        String name = nameedit.getText().toString().trim();
        String IP = ipedit.getText().toString().trim();
        String MAC = macedit.getText().toString().trim();

        Log.v("Insert",name + " " + IP + " " + MAC);

        values.put(DBHelper.DEVICE_NAME, name);
        values.put(DBHelper.DEVICE_IP, IP);
        values.put(DBHelper.DEVICE_MAC, MAC);
        db.insert(DBHelper.TABLE_DEVICE, null, values);
        db.close();

    }

    public void process(String DID){
        DBHelper mDatabaseHelper = new DBHelper(context);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Check if IP address was submitted and it's valid
        String name = nameedit.getText().toString().trim();
        String IP = ipedit.getText().toString().trim();
        String MAC = macedit.getText().toString().trim();

        Log.v("Insert","ID " + DID + " params: "+ name + " " + IP + " " + MAC);

        values.put(DBHelper.DEVICE_NAME, name);
        values.put(DBHelper.DEVICE_IP, IP);
        values.put(DBHelper.DEVICE_MAC, MAC);

        db.update(DBHelper.TABLE_DEVICE, values,
                DBHelper._ID + "=" + DID,null);

    }



}
