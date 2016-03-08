package org.skimens.wakeonalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver{

    boolean active;
    boolean repeat;
    boolean current_day;
    String IP;
    String MAC;



    @Override
    public void onReceive(Context context, Intent intent)
    {
        String DID = intent.getExtras().getString("DID");

        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getReadableDatabase();

        String query = "SELECT " + DBHelper.DEVICE_IP + " , " + DBHelper.DEVICE_MAC + " FROM " + DBHelper.TABLE_DEVICE + " WHERE " + DBHelper._ID +"='" + DID +"'" ;
        Cursor cursor = sdb.rawQuery(query, null);
        while (cursor.moveToNext()) {
            IP = cursor.getString(cursor
                    .getColumnIndex(DBHelper.DEVICE_IP));
            MAC = cursor.getString(cursor
                    .getColumnIndex(DBHelper.DEVICE_MAC));
            Log.v("CURSOR", "SELECTED " + IP + " " + MAC);
        cursor.close();


        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        if (dayOfWeek == -1) {dayOfWeek = 6;};

        query = "SELECT * FROM " + DBHelper.TABLE_ALARM + " WHERE " + DBHelper.ALARM_DEVICE_ID + "='" + DID +"'" ;
        Log.v("QUERY",query);
        cursor = sdb.rawQuery(query, null);
        while (cursor.moveToNext()) {
            current_day = cursor.getString(cursor
                    .getColumnIndex(DBHelper.ALARM_DAYS)).toCharArray()[dayOfWeek] != '0';
            active = cursor.getInt(cursor
                    .getColumnIndex(DBHelper.ALARM_ACTIVE)) > 0;
            repeat = cursor.getInt(cursor
                    .getColumnIndex(DBHelper.ALARM_REPEAT)) > 0;
            Log.v("CURSOR", "FOR DID " + DID  + " SELECTED " + current_day + " active " + active + " repeat " + repeat);
        }
        cursor.close();
            Log.v("cur", String.valueOf(current_day));
        if(active && current_day) {
            Log.v("act",String.valueOf(active));

                new WakeOnLan(IP,MAC).execute();


            }
        }

    }

}

