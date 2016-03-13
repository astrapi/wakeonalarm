package org.skimens.wakeonalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Calendar;

public class TimePickerActivity extends AppCompatActivity {

    private final String TAG = "TimePicker";

    private TimePicker timePicker;
    private LinearLayout weekdays;
    private ToggleButton[] weekdayArray;
    private CheckBox repeat;
    private CheckBox active;

    private String DIP;
    private String DNAME;
    private String DID;

    private boolean existed = false;

    private final DBHelper db = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timepicker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        repeat = (CheckBox) findViewById(R.id.repeat);
        active = (CheckBox) findViewById(R.id.active);

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        DIP = getIntent().getExtras().getString("IP");
        DNAME = getIntent().getExtras().getString("name");
        DID = getIntent().getExtras().getString("ID");
        Log.i(TAG,"Information from intent: " + DNAME + " ( " + DIP + " ) " + DID);

        TextView deviceInfo = (TextView) findViewById(R.id.deviceInfo);
        deviceInfo.setText(DNAME + " ( " + DIP + " )\n");
        deviceInfo.setTextSize(20);

        manageWeekdays();
        setData();
    }

    /*
    Managing weekdays togglebuttons
     */
    public void manageWeekdays(){
        weekdays = (LinearLayout) findViewById(R.id.weekdays);

        ToggleButton monday = (ToggleButton) findViewById(R.id.monday);
        ToggleButton tuesday = (ToggleButton) findViewById(R.id.tuesday);
        ToggleButton wednesday = (ToggleButton) findViewById(R.id.wednesday);
        ToggleButton thursday = (ToggleButton) findViewById(R.id.thursday);
        ToggleButton friday = (ToggleButton) findViewById(R.id.friday);
        ToggleButton saturday = (ToggleButton) findViewById(R.id.saturday);
        ToggleButton sunday = (ToggleButton) findViewById(R.id.sunday);

        weekdayArray = new ToggleButton[]{monday,tuesday,wednesday,thursday,friday,saturday,sunday};

        //Setting width of weekdays button to fit and fill into layout width
        weekdays.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Layout width : " + weekdays.getWidth());
                int width = weekdays.getWidth() / 7;
                for (ToggleButton button : weekdayArray) {
                    button.setLayoutParams(new LinearLayout.LayoutParams(width, width));
                }
            }
        });

    }
    /*
    Setting up user alarm data for device, if existed in db
     */
    public void setData(){
        SQLiteDatabase sdb = db.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.TABLE_ALARM + " WHERE " + DBHelper.ALARM_DEVICE_ID + "='" + DID +"'" ;
        Log.i(TAG,"setData query: " + query);
        Cursor cursor = sdb.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0){
        while (cursor.moveToNext()) {
            char[] days = cursor.getString(cursor
                    .getColumnIndex(DBHelper.ALARM_DAYS)).toCharArray();
            int time = cursor.getInt(cursor
                    .getColumnIndex(DBHelper.ALARM_TIME));
            boolean activate = cursor.getInt(cursor
                    .getColumnIndex(DBHelper.ALARM_ACTIVE)) > 0;
            boolean repeats = cursor.getInt(cursor
                    .getColumnIndex(DBHelper.ALARM_REPEAT)) > 0;
            Log.i(TAG, "setData result: " + DID  + " SELECTED " + days.toString() + " active " + activate + " repeat " + repeats + "time " + time);

            for(int i=0; i < weekdayArray.length;i++){
                weekdayArray[i].setChecked(days[i] != '0');

            }
            active.setChecked(activate);
            repeat.setChecked(repeats);

            timePicker.setCurrentHour(time / 60);
            timePicker.setCurrentMinute(time % 60);

            // set true if row for device existed in table
            existed = true;
        }}

        sdb.close();

    };



    /*
    Store user alarm setting for device into db
     */
    public void ProcessSettings(View view)
    {
        //To store time in single db column
        int time = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
        Log.i(TAG,"Time to store: " + String.valueOf(time));


        // Days turn into char array to store in 1 db column
        StringBuilder days = new StringBuilder();
        for(ToggleButton day : weekdayArray){
            if(day.isChecked()){ days.append("1"); } else {days.append("0"); };
        }

        Log.v(TAG,"Days to store: " + days.toString());

        int act = 0;
        if(active.isChecked()){ act = 1; };
        Log.v("ACTIVE",String.valueOf(act));

        int rep = 0;
        if(repeat.isChecked()){ rep = 1; };
        Log.v("ACTIVE", String.valueOf(rep));

        SQLiteDatabase sdb = db.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBHelper.ALARM_DAYS, days.toString());
        values.put(DBHelper.ALARM_TIME, time);
        values.put(DBHelper.ALARM_ACTIVE, act);
        values.put(DBHelper.ALARM_REPEAT, rep);

        // if row for device existed in table - update or select
        if(existed){
        sdb.update(DBHelper.TABLE_ALARM, values,
                    DBHelper.ALARM_DEVICE_ID + "=" + DID,null);
        } else {
        values.put(DBHelper.ALARM_DEVICE_ID, DID);
        sdb.insert(DBHelper.TABLE_ALARM, null, values);
        }

        sdb.close();

        SetAlarm(time);

    }

    /*
    Close activity on click Cancel button
     */
    public void Cancel(View view){
        finish();
    };

    /*
    Setting up alarm for device
     */
    public void SetAlarm(int time){

        int hour = time / 60;
        int mins = time % 60;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, mins);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // if current time of the day more than alarm time
        // it will fire immediately, to prevent it add one day
        if(System.currentTimeMillis() > calendar.getTimeInMillis()){
            Log.i(TAG,"Add 1 day to calendar");
            calendar.add(Calendar.DATE, 1);
        }

        Intent alarmIntent = new Intent(TimePickerActivity.this, AlarmReceiver.class);
        alarmIntent.putExtra("DID", DID);
        int did = Integer.valueOf(DID);

        if(existed) {
            // Cancel existed alarms for device before inserting new
            Log.i(TAG,"Existed = true");
            PendingIntent send = PendingIntent.getBroadcast(this,did,alarmIntent,PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager al = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            al.cancel(send);
        };
        if(active.isChecked()){
            // if flag acvtive is checked, making new alarm by did
            Log.i(TAG,"Active = true");
            PendingIntent sender = PendingIntent.getBroadcast(TimePickerActivity.this, did, alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
            Toast.makeText(getBaseContext(), String.format(getResources().getString(R.string.alarm_preference_is_saved), DNAME), Toast.LENGTH_LONG).show();
        } else {
            // if active is unchecked - just cancel previous alarm
            Log.i(TAG,"Active = false");
            PendingIntent send = PendingIntent.getBroadcast(this,did,alarmIntent,PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager al = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            al.cancel(send);
            Toast.makeText(getBaseContext(), String.format(getResources().getString(R.string.alarm_is_inactive), DNAME), Toast.LENGTH_LONG).show();
        };
        // By the open MainActivity
        Intent intentz = new Intent(TimePickerActivity.this,MainActivity.class);
        startActivity(intentz);

    };


};