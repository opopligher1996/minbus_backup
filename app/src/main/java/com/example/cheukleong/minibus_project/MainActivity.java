package com.example.cheukleong.minibus_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.provider.Settings.System;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.cheukleong.minibus_project.new_GPSTracker.journeyid;

public class MainActivity extends Activity {
    private Button start;
    private Button show;
    private Button send_request;
    private TextView Show_Station;
    private TextView Show_Routeid;
    private TextView Show_Journeyid;
    private TextView Show_Location;
    private EditText ID;
    private EditText Route;
    private EditText editText_routeid;
    private Spinner route_spinner;
    private CheckBox insert_trial_checkBox;
    public static boolean insert_trial_checked = false;
    public final Context context=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ID=findViewById(R.id.Car_ID);
        start=findViewById(R.id.startButton);
        show=findViewById(R.id.show_button);
        send_request=findViewById(R.id.send_request_button);
        Show_Location=findViewById(R.id.Show_Location);
        Show_Station=findViewById(R.id.Show_Station);
        Show_Routeid=findViewById(R.id.Show_Routid);
        Show_Journeyid=findViewById(R.id.Show_Journeyid);
        route_spinner=findViewById(R.id.route_spinner);
        insert_trial_checkBox=findViewById(R.id.insert_trial_checkBox);

        final String[] route_ids = {"8X","36M"};
        ArrayAdapter<String> route_ids_List = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                route_ids);
        route_spinner.setAdapter(route_ids_List);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Gash:","Start");
                new_GPSTracker.CAR_ID=ID.getText().toString();
                startService(new Intent(context, new_GPSTracker.class));
            }
        });

        send_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("GASH:","Start Testing");
                if(new_GPSTracker.journeyid!=null)
                {
                    Log.e("Gash : ","journeyid is not null");
                    delete_jorney(new_GPSTracker.journeyid);
                }
                int selected_item = route_spinner.getSelectedItemPosition();
                new_GPSTracker.go_station = get_stations(selected_item*2+1);
                new_GPSTracker.back_station = get_stations(selected_item*2+2);
                new_GPSTracker.CAR_ID=ID.getText().toString();
                new_GPSTracker.routeid_range=selected_item*2+1;
                new_GPSTracker.init=false;
                new_GPSTracker.journeyid=null;
                new_GPSTracker.Arr_station = -1;
                new_GPSTracker.Pre_station = -2;
            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Show_Location.setText("Location: "+ Double.toString(new_GPSTracker.Current_location.getLatitude())+" "+Double.toString(new_GPSTracker.Current_location.getLongitude()));
                    Show_Station.setText("Station: "+new_GPSTracker.Arr_station);
                    Show_Journeyid.setText("Journeyid: "+ journeyid);
                    Show_Routeid.setText("Routeid: "+new_GPSTracker.routeid);

                }
                catch (Exception e)
                {
                    Show_Location.setText("You have to start the GPSTracker first or you get error. Reason : "+e);
                }
            }
        });

        insert_trial_checkBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(insert_trial_checkBox.isChecked())
                    insert_trial_checked = true;
                else
                    insert_trial_checked = false;
            }
        });


    }

    public double[][] get_stations(int routeid) {
        HttpResponse response = null;
        try {
            if (android.os.Build.VERSION.SDK_INT > 9)
            {
                StrictMode.ThreadPolicy policy = new
                        StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setHeader("Content-Type", "application/json");
            request.setURI(new URI("http://128.199.88.79:3001/api/v1/minibus/getStations/?routeid="+routeid));
            response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String text_responese = EntityUtils.toString(entity);
            JSONObject obj = new JSONObject(text_responese);
            JSONArray array_stations = obj.getJSONArray("response");

            double results[][] = new double[array_stations.length()][2];
            for(int i = 0; i < array_stations.length(); i++)
            {
                JSONObject station = (JSONObject) array_stations.get(i);
                JSONObject station_location = new JSONObject(String.valueOf(station.get("stationLocation")));
                double station_long_lat[] = {(double) station_location.get("latitude"), (double) station_location.get("longitude")};
                results[i] = station_long_lat;
            }

            return results;

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    };

    public void delete_jorney(String journeyid){
        Log.d("Gash:","Start delete Journey");
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://128.199.88.79:3001/api/v1/minibus/deleteJourney");
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("journeyid", journeyid));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            Log.d("httppost: ",httppost.toString());

            try {
                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                HttpResponse response =client.execute(httppost);
            } catch (ClientProtocolException e) {
                Log.e("Error:","ClientProtocol");
            } catch (IOException e) {
                Log.e("Error:","IOException");
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("Error:","UnsupportedEncodingException");
        }
        Log.d("Gash: ","Finish delete Journey");
        return ;
    }

}
