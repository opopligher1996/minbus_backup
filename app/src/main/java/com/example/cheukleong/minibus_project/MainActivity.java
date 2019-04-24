package com.example.cheukleong.minibus_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends Activity {
    private Button start;
    public static TextView show_CarId;
    private EditText Car_ID;
    private Spinner route_spinner;
    private ImageButton route_change;
    private TextView show_battery_level;
    private List<String> route_ids = new ArrayList<String>();
    public static String choose_route = "11";
    public final Context context=this;
    public static int battery_level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Car_ID=findViewById(R.id.Car_ID);
        start=findViewById(R.id.startButton);
        route_spinner=findViewById(R.id.route_spinner);

        show_CarId = findViewById(R.id.show_carid);
        route_spinner = findViewById(R.id.route_spinner);
        route_change = findViewById(R.id.route_change);
        show_battery_level = findViewById(R.id.battery_level);
//        Car_ID.setText(Build.ID);
        Car_ID.setText("8722");
        route_ids.add("線路");
        route_ids.add("8x");
        route_ids.add("8");
        route_ids.add("8s");
        route_ids.add("11M");
        route_ids.add("11");
        route_ids.add("油站");
        MySpinnerAdapter adapter = new MySpinnerAdapter();
        route_spinner.setAdapter(adapter);
        show_CarId.setText(choose_route);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Gash:","Start");
                new_GPSTracker.CAR_ID=Car_ID.getText().toString();
                Intent intent = new Intent(context, new_GPSTracker.class);
                startService(intent);
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
                                            change_route(choose_route);
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
            if(choose_route.equals("11M")){
                Log.e("Gash","choose 11M");
                new_GPSTracker.go_station = new_GPSTracker.test_11m_go_station;
                new_GPSTracker.back_station = new_GPSTracker.test_11m_back_station;
            }
            else if(choose_route.equals("11")){
                Log.e("Gash","choose 11");
                new_GPSTracker.go_station = new_GPSTracker.test_11_go_station;
                new_GPSTracker.back_station = new_GPSTracker.test_11_back_station;
            }
            else{
                Log.e("Gash","choose 8");
                new_GPSTracker.go_station = new_GPSTracker.test_8x_go_station;
                new_GPSTracker.back_station = new_GPSTracker.test_8x_back_station;
            }
            start.callOnClick();
        }

        this.registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            int level = intent.getIntExtra("level", 0);
            battery_level = level;
            show_battery_level.setText(String.valueOf(level) + "%");
        }
    };

    public void change_route(String choose_route){
        new_GPSTracker.CAR_ID=Car_ID.getText().toString();
        new_GPSTracker.init=false;
        new_GPSTracker.journeyid=null;
        new_GPSTracker.Arr_station = -1;
        new_GPSTracker.Pre_station = -2;
        if(choose_route.equals("11M")){
            new_GPSTracker.go_station = new_GPSTracker.test_11m_go_station;
            new_GPSTracker.back_station = new_GPSTracker.test_11m_back_station;
        }
        else if(choose_route.equals("11")){
            new_GPSTracker.go_station = new_GPSTracker.test_11_go_station;
            new_GPSTracker.back_station = new_GPSTracker.test_11_back_station;
        }
        else{
            new_GPSTracker.go_station = new_GPSTracker.test_8x_go_station;
            new_GPSTracker.back_station = new_GPSTracker.test_8x_back_station;
        }
    }

    public double[][] get_stations(String route, int seq) {
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
            request.setURI(new URI("http://128.199.88.79:3002/api/v2/minibus/getStations/?route="+route+"&seq="+Integer.toString(seq)));
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
