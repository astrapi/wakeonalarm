package org.skimens.wakeonalarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Calendar;

/*
Class receive event from Android alarm service
 */

public class AlarmReceiver extends BroadcastReceiver{

    private final String TAG = "AlarmReceiver";

    private Context context;
    private Intent intent;

    String DID;
    String IP;
    String MAC;
    String name;

    boolean active=false;
    boolean repeat;
    boolean current_day;

    @Override
    public void onReceive(Context c, Intent i)
    {
        context = c;
        intent = i;

        DID = intent.getExtras().getString("DID");
        Log.i(TAG,DID);

        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getReadableDatabase();

        String query = "SELECT " + DBHelper.DEVICE_NAME + " , " + DBHelper.DEVICE_IP +
                " , " + DBHelper.DEVICE_MAC + " FROM " + DBHelper.TABLE_DEVICE +
                " WHERE " + DBHelper._ID +"='" + DID +"'" ;

        Cursor cursor = sdb.rawQuery(query, null);
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor
                    .getColumnIndex(DBHelper.DEVICE_NAME));
            IP = cursor.getString(cursor
                    .getColumnIndex(DBHelper.DEVICE_IP));
            MAC = cursor.getString(cursor
                    .getColumnIndex(DBHelper.DEVICE_MAC));
            Log.v(TAG, "Selected: " + IP + " " + MAC);
        cursor.close();

        //Managing week starts from monday
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        if (dayOfWeek == -1) {dayOfWeek = 6;};

        query = "SELECT * FROM " + DBHelper.TABLE_ALARM + " WHERE " + DBHelper.ALARM_DEVICE_ID + "='" + DID +"'" ;
        cursor = sdb.rawQuery(query, null);
        while (cursor.moveToNext()) {
            current_day = cursor.getString(cursor
                    .getColumnIndex(DBHelper.ALARM_DAYS)).toCharArray()[dayOfWeek] != '0';
            active = cursor.getInt(cursor
                    .getColumnIndex(DBHelper.ALARM_ACTIVE)) > 0;
            repeat = cursor.getInt(cursor
                    .getColumnIndex(DBHelper.ALARM_REPEAT)) > 0;
            Log.v(TAG, "for Device ID " + DID  + " selected: days=" + current_day + " active=" + active + " repeat=" + repeat);
        }
            sdb.close();

        if(active && current_day) {
            Log.i(TAG,"Sent Wake on Lan packet");
                new WakeOnLan(IP,MAC).execute();
                sendNotification();
            }

            // Will delete any existing alarms if active was set to false or device was deleted from database
            if(!active){
                Log.i(TAG,"Alarm broadcast for device id" + DID + " was removed");
                PendingIntent sender = PendingIntent.getBroadcast(context,Integer.valueOf(DID),intent,PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(sender);
            }
        }

    }
    /*
    Send notification if packet was sent to device
     */
    public void sendNotification(){
                PendingIntent contentIntent = PendingIntent.getActivity(context,
                        0, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

                Resources res = context.getResources();
                Notification.Builder builder = new Notification.Builder(context);

                String message = String.format(res.getString(R.string.wake_packet_sent_to), name, IP);

                builder.setContentIntent(contentIntent)
                        .setSmallIcon(R.drawable.ic_alarm_on_black)
                        .setTicker(message)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setContentTitle(res.getString(R.string.app_name))
                                .setContentText(message);

                Notification notification = builder.build();

                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(Integer.valueOf(DID), notification);
    }

}

