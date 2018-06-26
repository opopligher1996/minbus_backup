package com.example.cheukleong.minibus_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.provider.Settings.System;

import org.w3c.dom.Text;

public class MainActivity extends Activity {
    private Button start;
    private Button show;
    private Button e_setting;
    private TextView Show_Station;
    private TextView Show_Routeid;
    private TextView Show_Journeyid;
    private TextView Show_Location;
    private EditText ID;
    private EditText Route;
    private EditText editText_routeid;
    public final Context context=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ID=findViewById(R.id.Car_ID);
        start=findViewById(R.id.startButton);
        show=findViewById(R.id.show_button);
        Show_Location=findViewById(R.id.Show_Location);
        Show_Station=findViewById(R.id.Show_Station);
        Show_Routeid=findViewById(R.id.Show_Routid);
        Show_Journeyid=findViewById(R.id.Show_Journeyid);
        Route=findViewById(R.id.Route);
        e_setting=findViewById(R.id.Emerge_Set);
        editText_routeid = findViewById(R.id.editText_routeid);


        e_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new_GPSTracker.routeid=editText_routeid.getText().toString();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Gash:","Start");
                new_GPSTracker.CAR_ID=ID.getText().toString();
                startService(new Intent(context, new_GPSTracker.class));
            }
        });


        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Show_Location.setText("Location: "+ Double.toString(new_GPSTracker.Current_location.getLatitude())+" "+Double.toString(new_GPSTracker.Current_location.getLongitude()));
                    Show_Station.setText("Station: "+new_GPSTracker.Arr_station);
                    Show_Journeyid.setText("Journeyid: "+new_GPSTracker.journeyid);
                    Show_Routeid.setText("Routeid: "+new_GPSTracker.routeid);

                }
                catch (Exception e)
                {
                    Show_Location.setText("You have to start the GPSTracker first or you get error. Reason : "+e);
                }
            }
        });
    }
}
