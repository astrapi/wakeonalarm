package org.skimens.wakeonalarm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class deviceLayout {

    Context context;

    LinearLayout mainLayout;
    LinearLayout buttonLayout;
    LinearLayout textLayout;

    String IP;
    String name;
    String MAC;

    public deviceLayout(Context c, String name,String ip,String mac){
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
           Button alarmbt = new Button(context);
           alarmbt.setText("Alarm");
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
           Button wakebt = new Button(context);
           wakebt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                   LinearLayout.LayoutParams.WRAP_CONTENT));
           wakebt.setText("Wake");
           wakebt.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   new WakeOnLan(IP, MAC).execute();
                   Toast.makeText(context, "Wake signal sent to " + name, Toast.LENGTH_LONG).show();
               }
           });
           buttonLayout.addView(alarmbt, 0);
           buttonLayout.addView(wakebt, 1);

           textLayout.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   ((MainActivity) context).editDialog(String.valueOf(id), name, IP, MAC);
               }

               ;
           });
       }


    public void setButtons(){
        Button addbt = new Button(context);
        addbt.setText("Add");
        addbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddPC)context).addDialog(name, IP, MAC);
            }
        });
        buttonLayout.addView(addbt, 0);
    }


}
