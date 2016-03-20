package org.skimens.wakeonalarm;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
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

/*
Class for managing device dialog layout
Used in MainActivity and AddDeviceActivity activity
 */

public class setDeviceDialog {


    private final String TAG = "SetDeviceDialog";
    private Context context;

    public AlertDialog.Builder builder;

    public EditText nameedit;
    public EditText ipedit;
    public EditText macedit;

    private String name;
    private String IP;
    private String MAC;



    private Resources RS;

    /*
    Setting common components
     */
    public setDeviceDialog(Context c,String name,String IP,String MAC){
        RS = c.getResources();

        context = c;
        builder = new AlertDialog.Builder(context);

        builder.setTitle(RS.getString(R.string.add_device));

        LinearLayout layout = new LinearLayout(context);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView nametext = new TextView(context);
        nametext.setText(RS.getString(R.string.custom_name_for_device));
        layout.addView(nametext);

        nameedit = new EditText(context);
        nameedit.setGravity(Gravity.CENTER);
        nameedit.setText(name);
        layout.addView(nameedit);

        TextView iptext = new TextView(context);
        iptext.setText(RS.getString(R.string.ip_address));
        layout.addView(iptext);

        ipedit = new EditText(context);
        ipedit.setInputType(InputType.TYPE_CLASS_TEXT);
        ipedit.setGravity(Gravity.CENTER);
        ipedit.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        ipedit.setText(IP);
        layout.addView(ipedit);

        TextView mactext = new TextView(context);
        mactext.setText(RS.getString(R.string.mac_address));
        layout.addView(mactext);

        macedit = new EditText(context);
        macedit.setInputType(InputType.TYPE_CLASS_TEXT);
        macedit.setGravity(Gravity.CENTER);
        macedit.setText(MAC);

        layout.addView(macedit);
        builder.setView(layout);
        builder.setCancelable(true);
        builder.setNegativeButton(RS.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
    }

    public AlertDialog.Builder getDialog(){
        return builder;
    }

    public String getName(){
        return nameedit.getText().toString();
    }

    public String getIP(){
        return ipedit.getText().toString();
    }

    public String getMAC(){
        return macedit.getText().toString();
    }

    /*
    Check submitted values if exist and valid
     */
    public boolean checkSetDialog(){
        // Check if IP address was submitted and it's valid
        this.name = nameedit.getText().toString().trim();
        this.IP = ipedit.getText().toString().trim();
        this.MAC = macedit.getText().toString().trim();

        if (IP.length() == 0) {
            Toast.makeText(context,RS.getString(R.string.submit_ip), Toast.LENGTH_LONG).show();
            return false;
        } else {
            final Pattern IPPATTERN = Pattern.compile(
                    "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
            if (!IPPATTERN.matcher(IP).matches()) {
                Toast.makeText(context, RS.getString(R.string.ip_not_valid), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        if (MAC.length() == 0) {
            Toast.makeText(context, RS.getString(R.string.submit_mac), Toast.LENGTH_LONG).show();
            return false;
        } else {
            final Pattern MACPATTERN = Pattern.compile(
                    "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
            if (!MACPATTERN.matcher(MAC).matches()) {
                Toast.makeText(context, RS.getString(R.string.mac_not_valid), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        if (name.length() == 0) {
            Toast.makeText(context, RS.getString(R.string.submit_name), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    /*
    Method for AddDeviceActivity activity
     */
    public void process(){
        // Store device parameters to database
        // All checks are done before method call
        DBHelper mDatabaseHelper = new DBHelper(context);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBHelper.DEVICE_NAME, name);
        values.put(DBHelper.DEVICE_IP, IP);
        values.put(DBHelper.DEVICE_MAC, MAC);
        db.insert(DBHelper.TABLE_DEVICE, null, values);
        db.close();

        Toast.makeText(context, String.format(RS.getString(R.string.added_to_list),name), Toast.LENGTH_LONG).show();

    }
    /*
    Method for MainActivity (Updating)
     */
    public void process(String DID){
        DBHelper mDatabaseHelper = new DBHelper(context);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBHelper.DEVICE_NAME, name);
        values.put(DBHelper.DEVICE_IP, IP);
        values.put(DBHelper.DEVICE_MAC, MAC);

        db.update(DBHelper.TABLE_DEVICE, values,
                DBHelper._ID + "=" + DID,null);

    }



}
