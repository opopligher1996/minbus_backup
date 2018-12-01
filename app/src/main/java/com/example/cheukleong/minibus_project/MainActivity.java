package com.example.cheukleong.minibus_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.provider.Settings.System;
import android.widget.Toast;

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
    private TextView show_CarId;
    private TextView CarID;
    private EditText ID;
    private EditText Route;
    private Spinner route_spinner;
    private ImageButton route_change;
    private CheckBox insert_trial_checkBox;
    private String choose_route = "8x";
    private List<String> route_ids = new ArrayList<String>();
    public static boolean insert_trial_checked = false;
    public final Context context=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ID=findViewById(R.id.Car_ID);
        start=findViewById(R.id.startButton);
        route_spinner=findViewById(R.id.route_spinner);

        show_CarId = findViewById(R.id.show_carid);
        route_spinner = findViewById(R.id.route_spinner);
        route_change = findViewById(R.id.route_change);
        ID.setText(Build.ID);
        route_ids.add("線路");
        route_ids.add("8x");
        route_ids.add("8");
        route_ids.add("8s");
        route_ids.add("5");
        MySpinnerAdapter adapter = new MySpinnerAdapter();
        route_spinner.setAdapter(adapter);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Gash:","Start");
                new_GPSTracker.CAR_ID=ID.getText().toString();
                startService(new Intent(context, new_GPSTracker.class));
            }
        });

        route_change.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    route_change.setImageResource(R.drawable.button_clicked);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    route_change.setImageResource(R.drawable.button_unclicked);
                    int item = route_spinner.getSelectedItemPosition();
                    if(item!=0) {
                        final String select_route = route_ids.get(item);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("確定轉線")
                                .setMessage("由"+choose_route+"轉為"+select_route)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                        if(!choose_route.equals(select_route))
                                        {
                                            choose_route = select_route;
                                            show_CarId.setText(select_route);
                                            Toast.makeText(context, "已轉" + select_route + "路線", Toast.LENGTH_SHORT).show();
                                            change_route();
                                        }
                                        else
                                        {
                                            Toast.makeText(context, "維持" + select_route + "路線", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(context, "取消路線", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        builder.show();
                    }
                    else
                    {
                        Toast.makeText(context, "你沒有選擇路線", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            start.callOnClick();
        }
    }

    public void change_route(){
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



    class MySpinnerAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return route_ids.size();
        }

        @Override
        public Object getItem(int position) {
            return route_ids.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(position==0)
            {
                LinearLayout ll = new LinearLayout(MainActivity.this);
                ll.setGravity(Gravity.CENTER);
                TextView tv = new TextView(MainActivity.this);
                tv.setText(route_ids.get(position));
                tv.setTextSize(40);
                tv.setTextColor(Color.GRAY);
                tv.setGravity(Gravity.CENTER);
                ll.addView(tv);
                return ll;
            }
            else
            {
                LinearLayout ll = new LinearLayout(MainActivity.this);
                ll.setGravity(Gravity.CENTER);
                TextView tv = new TextView(MainActivity.this);
                tv.setText(route_ids.get(position));
                tv.setTextSize(40);
                tv.setTextColor(Color.rgb(9, 83, 0));
                tv.setGravity(Gravity.CENTER);
                ll.addView(tv);
                return ll;
            }
        }

        @Override
        public boolean isEnabled(int position){
            if(position == 0)
            {
                // Disable the first item from Spinner
                // First item will be use for hint
                return false;
            }
            else
            {
                return true;
            }
        }
    }
}
