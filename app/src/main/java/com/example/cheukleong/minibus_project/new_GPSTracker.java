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
    public static String journeyid = null;
    public static String routeid = null;
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

    //hard code route
    public static double test_8x_go_station[][]={
            {22.2837,114.1588},
            {22.2841445,114.1392645},
            {22.2836933,114.1366914},
            {22.26823162,114.12865509},
            {22.26642942,114.12825444},
            {22.261973,114.134431},
            {22.2619,114.1319}
    };

    public static double test_8x_back_station[][]={
            {22.2619,114.1319},
            {22.266572,114.128184},
            {22.269442,114.129753},
            {22.2843794,114.13428},
            {22.2837,114.1588}
    };

    public static double test_11m_go_station[][]={
            {22.3155645,114.2643589},
            {22.323900,114.268589},
            {22.336946,114.259167},
            {22.338634,114.262070}
    };

    public static double test_11m_back_station[][]={
            {22.338634,114.262070},
            {22.321541,114.269054},
            {22.3190835,114.2683805},
            {22.3155645,114.2643589}
    };

    public static double test_11_go_station[][]={
            {22.334909,114.208252},
            {22.333919,114.221078},
            {22.316984,114.270832},
            {22.320528,114.266447}
    };

    public static double test_11_back_station[][]={
            {22.320528,114.266447},
            {22.336934,114.259155},
            {22.333678,114.236938},
            {22.334074,114.209304}
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
            Log.e("init", String.valueOf(init));
            Log.e("Arr_station", String.valueOf(Arr_station));
            Log.e("Pre_station", String.valueOf(Pre_station));
            Set_Current_time();
            Set_Current_location(location);
            init();
//            update_location();
            Check_Arrive_Station();
            Check_Quit_Station();
            Check_Finish_Journey();
            Log.e("Gash: ","After process onLocationChnaged");
            Log.e("Gash: ","onLocationChanged");
            Log.e("routeid",routeid);
            Log.e("init", String.valueOf(init));
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


        Thread t = new Thread() {
            public void run() {
                while(true)
                {
                    Log.e(TAG, "run: ");
                    try
                    {
                        sleep(3000);
                        Calendar cal = Calendar.getInstance();
                        Date currentLocalTime = cal.getTime();
                        Long Current_time = currentLocalTime.getTime();
                        new_GPSTracker.Current_time = Current_time;
                        Log.e("New Thread Start","before running time:"+new_GPSTracker.Current_time);
                        new_GPSTracker.update_location();
                        Configs.getConfigs();
                        Log.e("New Thread End", "after running");
                    }
                    catch (InterruptedException e)
                    {}
                }
            }
        };

        t.start();
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

    public static void update_location(){
        if(routeid==null)
            return;
        Log.d(TAG, "Start_update_location");
        Map< String, Object > jsonValues = new HashMap< String, Object >();
        jsonValues.put("lat", Current_location.getLatitude());
        jsonValues.put("lng", Current_location.getLongitude());
        JSONObject update_location = new JSONObject(jsonValues);
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://128.199.88.79:3002/api/v2/record/addLocationRecord");
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            Log.e(TAG, "upload perm "+update_location.toString()+CAR_ID+MainActivity.choose_route+routeid+Long.toString(Current_time)+Integer.toString(MainActivity.battery_level));
            nameValuePairs.add(new BasicNameValuePair("location", update_location.toString()));
            nameValuePairs.add(new BasicNameValuePair("license", CAR_ID));
            nameValuePairs.add(new BasicNameValuePair("route", MainActivity.choose_route));
            nameValuePairs.add(new BasicNameValuePair("seq", routeid));
            nameValuePairs.add(new BasicNameValuePair("timestamp",Long.toString(Current_time)));
            nameValuePairs.add(new BasicNameValuePair("batteryLeft",Integer.toString(MainActivity.battery_level)));
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