package com.example.cheukleong.minibus_project;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.Tag;
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
    public static Location Sent_location = new Location("");
    // -1 is equal to the minbus don't arrive any station
    public static int Arr_station = -1;
    // -2 is equal to the minibus don't arrive any station before
    public static int Pre_station = -2;
    public static boolean init=false;
    public static int dans=100;
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
            {22.3155480062186,114.2643589},
            {22.3239136706781,114.2686509332},
            {22.3369558728058,114.259236763901},
            {22.3386360145456,114.262161534528}
    };

    public static double test_11m_back_station[][]={
            {22.3386508695072,114.262131520301},
            {22.3201032027678,114.270050575393},
            {22.3169961979765,114.270752545671},
            {22.3154247969597,114.265771784544}
    };

    public static double test_11_go_station[][]={
            {22.334882292983,114.208199802671},
            {22.333872547164,114.221120459639},
            {22.3169858225788,114.270759645098},
            {22.3205275205682,114.266430590858}
    };

    public static double test_11_back_station[][]={
            {22.3205081059827,114.266441690369},
            {22.3330777153122,114.262999610027},
            {22.3397613538672,114.246774213957},
            {22.3338762522471,114.221025296549},
            {22.3343050891782,114.210504183526}
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
            Set_Current_time();
            Set_Current_location(location);
            init();
//            location.getAccuracy()
            Log.e("Validate Record:", Boolean.toString(Validate_Record(location)));
            Log.e("a:",Float.toString(location.getAccuracy()));
            Log.e("provider: ",location.getProvider());
            if(Validate_Record(location) && init)
            {
                Check_Arrive_Station();
                Check_Quit_Station();
                Check_Finish_Journey();
                Set_Sent_location(location);
            }
            else if(init){
                Set_Sent_location(location);
            }
            else{
                Set_Sent_location(location);
            }

            Log.e("Gash: ","After process onLocationChnaged");
            Log.e("Gash: ","onLocationChanged");
            if(routeid ==null)
                Log.e("routeid","null");
            else
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
                        if(init)
                        {
                            Log.e("Debug: ","send");
                            Log.d(TAG, "Start_update_location");
                            Log.d(TAG, "CAR_ID = "+CAR_ID );
                            Log.d(TAG, "route = "+MainActivity.choose_route);
                            Log.d(TAG, "seq = "+routeid);
                        }
                        else {
                            Log.e("Debug: ","Not send");
                        }
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
        //init=true;
        Set_Compare_location(go_station[0][0],go_station[0][1]);

        Log.e("Gash:","LocationDistance(Current_location,Compare_location)");
        Log.e("Gash", Current_location.toString());
        Log.e("Gash",Compare_location.toString());

        double distance_start_point = LocationDistance(Current_location,Compare_location);



        Set_Compare_location(back_station[0][0],back_station[0][1]);


        Log.e("Gash:","LocationDistance(Current_location,Compare_location)");
        Log.e("Gash", Current_location.toString());
        Log.e("Gash",Compare_location.toString());

        double distance_end_point = LocationDistance(Current_location,Compare_location);
        Log.e("distance_start_point = ",String.valueOf(distance_start_point));
        Log.e("distance_end_point = ", String.valueOf(distance_end_point));
        if(distance_start_point<200 && routeid==null) {
            Set_routeid(1);
            Set_journeyid();
            init = true;
        }
        else if(distance_end_point<200 && routeid==null){
            Set_routeid(2);
            Set_journeyid();
            init = true;
        }
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
            dans=100;
        Set_Compare_location(station[Arr_station][0],station[Arr_station][1]);
        Log.d("Gash: ","Debug distance = "+LocationDistance(Current_location,Compare_location));
        if(LocationDistance(Current_location,Compare_location)>dans) {
            dans = 100;
            return true;
        }
        dans=100;
        return false;
    }

    public boolean Validate_Record(Location location){
        if(location.getProvider()=="gps") {
            Log.e("Debug: ","Validate Record ");
            return true;
        }
        else{
            Log.e("Debug: ","Invalidate Record ");
            return false;
        }
    }

    public int Enter_Which_Station(){
        double station[][];
        if(routeid.equals("1"))
            station = go_station;
        else
            station = back_station;
        for(int i=0;i<station.length;i++){
            Log.e(TAG, "station "+i+" = " );
            if((i==0 && routeid.equals("1"))||(i==station.length-1 && routeid.equals("2")))
                dans=100;
            Set_Compare_location(station[i][0],station[i][1]);
            if(LocationDistance(Current_location,Compare_location)<dans)
            {
                dans = 100;
                return i;
            }
            dans = 100;
        }
        return -1;
    }

    public double LocationDistance(Location current_location,Location compare_location){
        Log.e(TAG, "LocationDistance: "+current_location.distanceTo(compare_location));
        return current_location.distanceTo(compare_location);
    }

    public static void update_location(){
        if(init==false)
            return;
        if(routeid==null)
            return;
        if(CAR_ID=="" || CAR_ID==null || CAR_ID==" ")
            return;
        Log.d(TAG, "Start_update_location");
        Log.d(TAG, "CAR_ID = "+CAR_ID );
        Log.d(TAG, "route = "+MainActivity.choose_route);
        Log.d(TAG, "seq = "+routeid);
        Map< String, Object > jsonValues = new HashMap< String, Object >();
        jsonValues.put("lat", Sent_location.getLatitude());
        jsonValues.put("lng", Sent_location.getLongitude());
        float accuracy = Sent_location.getAccuracy();
        float speed = Sent_location.getSpeed();
        String provider= Sent_location.getProvider();
        String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
        Log.e(TAG, "provider: "+provider );
        Log.e(TAG, "GPS_PROVIDER: "+GPS_PROVIDER);
        Log.e(TAG, "speed : "+speed );
        Log.e(TAG, "accuracy : "+accuracy);
        JSONObject update_location = new JSONObject(jsonValues);
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://minibus-api-prod.socif.co/api/v2/record/addLocationRecord");
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            Log.e(TAG, "timestamp = "+Long.toString(Current_time));
            Log.e(TAG, "upload perm "+update_location.toString()+CAR_ID+MainActivity.choose_route+routeid+Long.toString(Current_time)+Integer.toString(MainActivity.battery_level));
            nameValuePairs.add(new BasicNameValuePair("location", update_location.toString()));
            nameValuePairs.add(new BasicNameValuePair("license", CAR_ID));
            nameValuePairs.add(new BasicNameValuePair("route", MainActivity.choose_route));
            nameValuePairs.add(new BasicNameValuePair("seq", routeid));
            nameValuePairs.add(new BasicNameValuePair("speed",Float.toString(speed)));
            nameValuePairs.add(new BasicNameValuePair("accuracy", Float.toString(accuracy)));
            nameValuePairs.add(new BasicNameValuePair("timestamp",Long.toString(Current_time)));
            nameValuePairs.add(new BasicNameValuePair("batteryLeft",Integer.toString(MainActivity.battery_level)));
            nameValuePairs.add(new BasicNameValuePair("provider",provider));
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

    public void Set_Sent_location(Location location){
        Sent_location = location;
    }

    public void Set_routeid(int go){
        routeid = String.valueOf(go);
    }

    public void Set_journeyid(){
        journeyid = Current_time+CAR_ID;
    }
}