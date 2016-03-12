package org.skimens.wakeonalarm;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class deviceLayout {

    Context context;
    Resources RS;

    LinearLayout mainLayout;
    LinearLayout buttonLayout;
    LinearLayout textLayout;

    String IP;
    String name;
    String MAC;

    public deviceLayout(Context c, String name,String ip,String mac){
        this.RS = c.getResources();
        this.context = c;
        this.name = name;
        this.IP = ip;
        this.MAC = mac;
        initCommonElements();


    };

    public LinearLayout getLayout(int id)
    {
        setButtons(id);
        return this.mainLayout;
    }

    public LinearLayout getLayout()
    {
        setButtons();
        return this.mainLayout;
    }


    private void initCommonElements() {
        mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);

        textLayout = new LinearLayout(context);
        textLayout.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textLayout.setOrientation(LinearLayout.VERTICAL);

        TextView nametxt = new TextView(context);
        nametxt.setGravity(Gravity.START);
        nametxt.setText(name);
        nametxt.setTextSize(20);
        nametxt.setTextColor(Color.BLACK);

        TextView iptxt = new TextView(context);
        iptxt.setText(IP + "\n" + MAC);
        iptxt.setGravity(Gravity.START);
        iptxt.setTextSize(10);

        textLayout.addView(nametxt, 0);
        textLayout.addView(iptxt, 1);

        buttonLayout = new LinearLayout(context);
        buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.RIGHT);

        mainLayout.addView(textLayout, 0);
        mainLayout.addView(buttonLayout,1);

    };


       public void setButtons(final int id){
           boolean active = false;

           DBHelper db = new DBHelper(context);
           SQLiteDatabase sdb = db.getReadableDatabase();
           String query = "SELECT " + DBHelper.ALARM_ACTIVE +" FROM " + DBHelper.TABLE_ALARM + " WHERE " + DBHelper.ALARM_DEVICE_ID + "=" + String.valueOf(id);
           Cursor cursor = sdb.rawQuery(query, null);
           while (cursor.moveToNext()) {
               active = cursor.getInt(cursor
                       .getColumnIndex(DBHelper.ALARM_ACTIVE)) > 0;}
           cursor.close();

           ImageButton alarmbt = new ImageButton(context);
           alarmbt.setBackgroundResource(0);
           if(active) {
               alarmbt.setImageDrawable(RS.getDrawable(R.drawable.ic_alarm_on_black));
           } else {
               alarmbt.setImageDrawable(RS.getDrawable(R.drawable.ic_alarm_add_black));
           }



           alarmbt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                   LinearLayout.LayoutParams.WRAP_CONTENT));
           alarmbt.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent TimePickerActivity = new Intent(context, TimePickerActivity.class);
                   TimePickerActivity.putExtra("IP", IP);
                   TimePickerActivity.putExtra("name", name);
                   TimePickerActivity.putExtra("ID", String.valueOf(id));
                   context.startActivity(TimePickerActivity);
               }
           });
           ImageButton wakebt = new ImageButton(context);
           wakebt.setBackgroundResource(0);
           wakebt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                   LinearLayout.LayoutParams.WRAP_CONTENT));
           wakebt.setImageDrawable(RS.getDrawable(R.drawable.ic_move_to_inbox_black));
           wakebt.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   new WakeOnLan(IP, MAC).execute();
                   Toast.makeText(context, String.format(RS.getString(R.string.wake_packet_sent_to),name,IP), Toast.LENGTH_LONG).show();
               }
           });
           buttonLayout.addView(alarmbt, 0);
           buttonLayout.addView(wakebt, 1);

           textLayout.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   ((MainActivity) context).onDeviceClick(id, name, IP, MAC);
               }

               ;
           });
       }


    public void setButtons(){
        ImageButton addbt = new ImageButton(context);
        addbt.setBackgroundResource(0);
        addbt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        addbt.setImageDrawable(RS.getDrawable(R.drawable.ic_add_circle_outline_black));
        addbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddPC)context).addDialog(name, IP, MAC);
            }
        });
        buttonLayout.addView(addbt, 0);
    }


}
