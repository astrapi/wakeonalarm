package org.skimens.wakeonalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.Calendar;

public class TimePickerActivity extends AppCompatActivity {

    TimePicker timePicker;

    LinearLayout weekdays;

    ToggleButton[] weekday;

    String DIP;
    String DNAME;
    String DID;

    ToggleButton monday;
    ToggleButton tuesday;
    ToggleButton wednesday;
    ToggleButton thursday;
    ToggleButton friday;
    ToggleButton saturday;
    ToggleButton sunday;

    CheckBox repeat;
    CheckBox active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timepicker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        weekdays = (LinearLayout) findViewById(R.id.weekdays);

        monday = (ToggleButton) findViewById(R.id.monday);
        tuesday = (ToggleButton) findViewById(R.id.tuesday);
        wednesday = (ToggleButton) findViewById(R.id.wednesday);
        thursday = (ToggleButton) findViewById(R.id.thursday);
        friday = (ToggleButton) findViewById(R.id.friday);
        saturday = (ToggleButton) findViewById(R.id.saturday);
        sunday = (ToggleButton) findViewById(R.id.sunday);

        weekday = new ToggleButton[]{monday,tuesday,wednesday,thursday,friday,saturday,sunday};

        repeat = (CheckBox) findViewById(R.id.repeat);

        active = (CheckBox) findViewById(R.id.active);

        timePicker = (TimePicker) findViewById(R.id.timePicker);

        DIP = getIntent().getExtras().getString("IP");
        DNAME = getIntent().getExtras().getString("name");
        DID = getIntent().getExtras().getString("ID");

        Log.v("GetIntent",DNAME + " ( " + DIP + " ) " + DID);

        TextView deviceInfo = (TextView) findViewById(R.id.deviceInfo);
        Log.v("text", deviceInfo.toString());
        deviceInfo.setText(DNAME + " ( " + DIP + " )\n");

        weekdays.post(new Runnable()
        {
            @Override
            public void run(){
                Log.v("TEST", "Layout width : " + weekdays.getWidth());
                int width = weekdays.getWidth() / 7;
                for(ToggleButton button : weekday){
                           button.setLayoutParams(new LinearLayout.LayoutParams(width, width));
                }
            }
        });}

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




    public void ProcessSettings(View view)
    {
        //Prepare data
        int time = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
        Log.v("TIME",String.valueOf(time));


        StringBuilder days = new StringBuilder();
        for(ToggleButton day : weekday){
            if(day.isChecked()){ days.append("1"); } else {days.append("0"); };
        }

        Log.v("DAYS",days.toString());

        int act;
        if(active.isChecked()){ act = 1; } else { act = 0; };
        Log.v("ACTIVE",String.valueOf(act));

        int rep;
        if(repeat.isChecked()){ rep = 1; } else { rep = 0; };
        Log.v("ACTIVE",String.valueOf(rep));

        DBHelper mDatabaseHelper = new DBHelper(this);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.ALARM_DEVICE_ID, DID);
        values.put(DBHelper.ALARM_DAYS, days.toString());
        values.put(DBHelper.ALARM_TIME, time);
        values.put(DBHelper.ALARM_ACTIVE, act);
        values.put(DBHelper.ALARM_REPEAT, rep);
        db.insert(DBHelper.TABLE_ALARM, null, values);

        SetAlarm(time);

    }

    public void Return(View view){};

    public void SetAlarm(int time){

        int hour = time / 60;
        int mins = time % 60;

        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.HOUR_OF_DAY, hour);
        calendar.set(calendar.MINUTE, mins);
        calendar.set(calendar.SECOND, 0);
        calendar.set(calendar.MILLISECOND, 0);
        long sdl = calendar.getTimeInMillis();

        Intent intent = new Intent(TimePickerActivity.this, AlarmReceiver.class);
        intent.putExtra("DID",DID);
        PendingIntent sender = PendingIntent.getBroadcast(TimePickerActivity.this, Integer.valueOf(DID), intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, sdl, AlarmManager.INTERVAL_DAY, sender);

    };


};