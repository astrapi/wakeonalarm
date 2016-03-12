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

public class AlarmReceiver extends BroadcastReceiver{

    Context context;
    Intent intent;

    String DID;
    String IP;
    String MAC;
    String name;

    boolean active;
    boolean repeat;
    boolean current_day;

    @Override
    public void onReceive(Context c, Intent i)
    {
        context = c;
        intent = i;

        DID = intent.getExtras().getString("DID");
        Log.v("OnR",DID);

        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getReadableDatabase();

        String query = "SELECT " + DBHelper.DEVICE_NAME + " , " + DBHelper.DEVICE_IP + " , " + DBHelper.DEVICE_MAC + " FROM " + DBHelper.TABLE_DEVICE + " WHERE " + DBHelper._ID +"='" + DID +"'" ;
        Cursor cursor = sdb.rawQuery(query, null);
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor
                    .getColumnIndex(DBHelper.DEVICE_NAME));
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

            Log.v("cur", String.valueOf(current_day));
        if(active && current_day) {
            Log.v("act",String.valueOf(active));
                new WakeOnLan(IP,MAC).execute();
                sendNotification();
            }

            if(!active){
                PendingIntent sender = PendingIntent.getBroadcast(context,Integer.valueOf(DID),intent,PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(sender);
            }
        }

    }

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

