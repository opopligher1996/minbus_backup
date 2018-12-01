package com.example.cheukleong.minibus_project;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class new_GPSTracker extends Service
{
    public static String CAR_ID;
    private static final String TAG = "Gash";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    public static int routeid_range = 1;
    public static String journeyid = null;
    public static String routeid;
    public static Long Station_startTime;
    public static Long Station_endTime;
    public static Long Current_time;
    public static Location Current_location =  new Location("");
    public static Location Compare_location = new Location("");
    // -1 is equal to the minbus don't arrive any station
    public static int Arr_station = -1;
    // -2 is equal to the minibus don't arrive any station before
    public static int Pre_station = -2;
    public static boolean init=false;
    public static int dans=50;
    public int Bat_info=100;
    private Context ctx;
    public static double go_station[][]={
            {22.2837,114.1588},
            {22.2841445,114.1392645},
            {22.2836933,114.1366914},
            {22.26823162,114.12865509},
            {22.26642942,114.12825444},
            {22.261973,114.134431},
            {22.2619,114.1319}
    };
    public static double back_station[][]={
            {22.2619,114.1319},
            {22.266572,114.128184},
            {22.269442,114.129753},
            {22.2843794,114.13428},
            {22.2837,114.1588}
    };

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e("Gash: ","Before process onLocationChnaged");
            Log.e("Gash: ","onLocationChanged");
            if (journeyid==null)
                Log.e("journeyid","null");
            else
                Log.e("journeyid: ",journeyid);

            if(routeid==null)
                Log.e("routeid","null");
            else
                Log.e("routeid",routeid);
            Log.e("init", String.valueOf(init));
            Log.e("routeid_range", String.valueOf(routeid_range));
            Log.e("Arr_station", String.valueOf(Arr_station));
            Log.e("Pre_station", String.valueOf(Pre_station));
            Set_Current_time();
            Set_Current_location(location);
            init();
            update_location();
            Check_Arrive_Station();
            Check_Quit_Station();
            Check_Finish_Journey();
            Log.e("Gash: ","After process onLocationChnaged");
            Log.e("Gash: ","onLocationChanged");
            Log.e("journeyid: ",journeyid);
            Log.e("routeid",routeid);
            Log.e("init", String.valueOf(init));
            Log.e("routeid_range", String.valueOf(routeid_range));
            Log.e("Arr_station", String.valueOf(Arr_station));
            Log.e("Pre_station", String.valueOf(Pre_station));
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Calendar cal = Calendar.getInstance();
            Date currentLocalTime = cal.getTime();
            DateFormat date = new SimpleDateFormat("dd-mm-yy hh:mm:ss");
            String localTime = date.format(currentLocalTime);
            Log.e(TAG, "onStatusChanged: " + provider);
            Log.e(TAG, "time " + localTime);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.e(TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        super.onCreate();
        ctx = this;
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void init(){

        if(init)
            return;
        init=true;
        Set_Compare_location(go_station[0][0],go_station[0][1]);
        double distance_start_point = LocationDistance(Current_location,Compare_location);
        Set_Compare_location(go_station[go_station.length-1][0],go_station[go_station.length-1][1]);
        double distance_end_point = LocationDistance(Current_location,Compare_location);
        if(distance_start_point<distance_end_point)
            Set_routeid(1);
        else
            Set_routeid(2);
        Set_journeyid();
//        insert_journey();
    }

    public void Check_Arrive_Station(){
        if(Arr_station!=-1)
            return;
        if(Enter_Which_Station()>=0 && Enter_Which_Station()!=Pre_station) {
            Set_Arr_station(Enter_Which_Station());
            Station_startTime = Current_time;
            Pre_station = Enter_Which_Station();
        }
        else
            Set_Arr_station(-1);
    }

    public void Check_Quit_Station(){
        boolean Quitting_station;
        if(Arr_station==-1)
            return;
        if(routeid.equals("1"))
            Quitting_station = Check_Quitting(go_station);
        else
            Quitting_station = Check_Quitting(back_station);
        Log.d("GASH: ","DEBUG Quittng_station = "+Quitting_station);
        if(Quitting_station) {
            Log.d("Gash:","Enter reset");
            Station_endTime  = Current_time;
            if(MainActivity.insert_trial_checked==false)
                insert_station();
            else
                insert_trial_station();
            Set_Arr_station(-1);
        }
    }


    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent info) {
            // TODO Auto-generated method stub
            if(Intent.ACTION_BATTERY_CHANGED.equals(info.getAction())){
                int level = info.getIntExtra("level", 0);
                Bat_info=level;
            }
        }
    };


    public void Check_Finish_Journey(){
        if((Arr_station==go_station.length-1 && routeid.equals("1"))||(Arr_station==back_station.length-1 && routeid.equals("2"))){
            Set_New_Journey();
//            insert_journey();
        }
    }

    public void Set_New_Journey(){
        Set_Arr_station(0);
        if(routeid.equals("1"))
            Set_routeid(2);
        else
            Set_routeid(1);
        Set_journeyid();
    }

    public boolean Check_Quitting(double station[][]){
        if((Arr_station==0 && routeid.equals("1"))||(Arr_station==back_station.length-1 && routeid.equals("2")))
            dans=150;
        Set_Compare_location(station[Arr_station][0],station[Arr_station][1]);
        Log.d("Gash: ","Debug distance = "+LocationDistance(Current_location,Compare_location));
        if(LocationDistance(Current_location,Compare_location)>dans) {
            dans = 50;
            return true;
        }
        dans=50;
        return false;
    }

    public int Enter_Which_Station(){
        double station[][];
        if(routeid.equals("1"))
            station = go_station;
        else
            station = back_station;
        for(int i=0;i<station.length;i++){
            if((i==0 && routeid.equals("1"))||(i==station.length-1 && routeid.equals("2")))
                dans=150;
            Set_Compare_location(station[i][0],station[i][1]);
            if(LocationDistance(Current_location,Compare_location)<dans)
            {
                dans = 50;
                return i;
            }
            dans = 50;
        }
        return -1;
    }

    public double LocationDistance(Location current_location,Location compare_location){
        return current_location.distanceTo(compare_location);
    }

    private void insert_station(){
        Log.e("Insert_station: ","Start Insert Station");
        Log.e("Insert_station: ",Station_startTime.toString()+CAR_ID+String.valueOf(Pre_station));
        Log.e("Insert_station: ", String.valueOf(Current_location.getLatitude()));
        Log.e("Insert_station: ", String.valueOf(Current_location.getLongitude()));
        Log.e("Insert_station", Station_startTime.toString());
        Log.e("Insert_station",Station_endTime.toString());
        Log.e("Insert_station",String.valueOf(Pre_station));
        Log.e("Insert_station: ",journeyid);
        Map< String, Object > jsonValues = new HashMap< String, Object >();
        jsonValues.put("x", Current_location.getLatitude());
        jsonValues.put("y", Current_location.getLongitude());
        JSONObject endlocation = new JSONObject(jsonValues);
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://128.199.88.79:3001/api/v1/minibus/insertStation");
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("stationid",Station_startTime.toString()+CAR_ID+String.valueOf(Pre_station)));
            nameValuePairs.add(new BasicNameValuePair("location", endlocation.toString()));
            nameValuePairs.add(new BasicNameValuePair("startTime", Station_startTime.toString()));
            nameValuePairs.add(new BasicNameValuePair("endTime", Station_endTime.toString()));
            nameValuePairs.add(new BasicNameValuePair("journeyid", journeyid));
            nameValuePairs.add(new BasicNameValuePair("stationOrder",String.valueOf(Pre_station)));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            Log.d("httppost: ",httppost.toString());

            try {
                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                Log.d(TAG, "Before send message");
                HttpResponse response =client.execute(httppost);
                Log.d(TAG, "After send message");
                Log.d("myapp", "response " + response.getEntity());

            } catch (ClientProtocolException e) {
                Log.d("Error:","ClientProtocol");
            } catch (IOException e) {
                Log.d("Error:","IOException");
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("Error:","UnsupportedEncodingException");
        }

        Log.d("Gash:","Finish insert station");
        return ;
    }

    private void insert_trial_station(){
        Log.d("GASH:","Start Insert Station");
        Map< String, Object > jsonValues = new HashMap< String, Object >();
        jsonValues.put("x", Current_location.getLatitude());
        jsonValues.put("y", Current_location.getLongitude());
        JSONObject endlocation = new JSONObject(jsonValues);
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://128.199.88.79:3001/api/v1/minibus/insertTrialLocation");
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("stationid",Station_startTime.toString()+CAR_ID+String.valueOf(Pre_station)));
            nameValuePairs.add(new BasicNameValuePair("location", endlocation.toString()));
            nameValuePairs.add(new BasicNameValuePair("startTime", Station_startTime.toString()));
            nameValuePairs.add(new BasicNameValuePair("endTime", Station_endTime.toString()));
            nameValuePairs.add(new BasicNameValuePair("journeyid", journeyid));
            nameValuePairs.add(new BasicNameValuePair("stationOrder",String.valueOf(Pre_station)));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            Log.d("httppost: ",httppost.toString());

            try {
                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                Log.d(TAG, "Before send message");
                HttpResponse response =client.execute(httppost);
                Log.d(TAG, "After send message");
                Log.d("myapp", "response " + response.getEntity());

            } catch (ClientProtocolException e) {
                Log.d("Error:","ClientProtocol");
            } catch (IOException e) {
                Log.d("Error:","IOException");
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("Error:","UnsupportedEncodingException");
        }

        Log.d("Gash:","Finish insert station");
        return ;
    }

    private void update_location(){

        Log.d(TAG, "Start_update_location");
        Map< String, Object > jsonValues = new HashMap< String, Object >();
        jsonValues.put("x", Current_location.getLatitude());
        jsonValues.put("y", Current_location.getLongitude());
        JSONObject update_location = new JSONObject(jsonValues);
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://128.199.88.79:3001/api/v1/minibus/updateLocation");
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("location", update_location.toString()));
            nameValuePairs.add(new BasicNameValuePair("time", Current_time.toString()));
            nameValuePairs.add(new BasicNameValuePair("journeyid", journeyid));
            nameValuePairs.add(new BasicNameValuePair("routeid", routeid));
            nameValuePairs.add(new BasicNameValuePair("batteryLeft", Integer.toString(Bat_info)));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            Log.d("httppost: ",httppost.toString());

            try {
                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                HttpResponse response =client.execute(httppost);
            } catch (ClientProtocolException e) {
                Log.d("Error:","ClientProtocol");
            } catch (IOException e) {
                Log.d("Error:","IOException");
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("Error:","UnsupportedEncodingException");
        }
        Log.d("Gash:","Finish update location");
        return ;
    }

    private void insert_journey(){
        Log.d("Gash:","Start Insert Journey");
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://128.199.88.79:3001/api/v1/minibus/insertJourney");
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("journeyid", journeyid));
            nameValuePairs.add(new BasicNameValuePair("route", routeid));
            nameValuePairs.add(new BasicNameValuePair("distance", String.valueOf(0)));
            nameValuePairs.add(new BasicNameValuePair("avgSpd", String.valueOf(0)));
            nameValuePairs.add(new BasicNameValuePair("startTime", Current_time.toString()));
            nameValuePairs.add(new BasicNameValuePair("endTime", String.valueOf(0)));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            Log.d("httppost: ",httppost.toString());

            try {
                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                Log.d(TAG, "Before sending journey data");
                HttpResponse response =client.execute(httppost);
                Log.d(TAG, "After sending journey data");
                Log.d("myapp", "response " + response.getEntity());

            } catch (ClientProtocolException e) {
                Log.d("Error:","ClientProtocol");
            } catch (IOException e) {
                Log.d("Error:","IOException");
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("Error:","UnsupportedEncodingException");
        }
        Log.d("Gash: ","Finish Insert Journey");
        return ;
    }



    public void Set_Arr_station(int station_id){
        Arr_station = station_id;
    }

    public void Set_Current_time(){
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        Current_time = currentLocalTime.getTime();
    }

    public void Set_Current_location(Location location){
        Current_location = location;
    }

    public void Set_Compare_location(double x,double y){
        Compare_location.setLatitude(x);
        Compare_location.setLongitude(y);
    }

    public void Set_routeid(int go){
        routeid = String.valueOf(go);
    }

    public void Set_journeyid(){
        journeyid = Current_time+CAR_ID;
    }
}