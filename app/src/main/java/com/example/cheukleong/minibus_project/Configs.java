package com.example.cheukleong.minibus_project;

import android.os.StrictMode;
import android.util.Log;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configs {

    public static String ConfigsId = null;

    public static String TAG = "Debug:";

    public static void getConfigs(){
        HttpResponse response = null;
        Log.e(TAG, "getConfigs:" );
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
            request.setURI(new URI("http://minibus-api-prod.scif.co/api/v2/minibus/getCarConfigs/?license="+new_GPSTracker.CAR_ID));
            response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String text_responese = EntityUtils.toString(entity);
            JSONObject obj = new JSONObject(text_responese);
            JSONObject getResponse = obj.getJSONObject("response");
            Log.e(TAG, "getConfigs: "+getResponse.getBoolean("set") );
            boolean set = getResponse.getBoolean("set");
            if(set)
            {
                Log.e(TAG, "enter set" );
                Log.e(TAG, getResponse.getString("seq"));
                new_GPSTracker.routeid = getResponse.getString("seq");
                MainActivity.choose_route = getResponse.getString("route");
                new_GPSTracker.init=false;
                new_GPSTracker.journeyid=null;
                new_GPSTracker.Arr_station = -1;
                new_GPSTracker.Pre_station = -2;
                MainActivity.show_CarId.setText(MainActivity.choose_route);
                if(MainActivity.choose_route.equals("11M")){
                    new_GPSTracker.go_station = new_GPSTracker.test_11m_go_station;
                    new_GPSTracker.back_station = new_GPSTracker.test_11m_back_station;
                }
                else if(MainActivity.choose_route.equals("11")){
                    new_GPSTracker.go_station = new_GPSTracker.test_11_go_station;
                    new_GPSTracker.back_station = new_GPSTracker.test_11_back_station;
                }
                else{
                    new_GPSTracker.go_station = new_GPSTracker.test_8x_go_station;
                    new_GPSTracker.back_station = new_GPSTracker.test_8x_back_station;
                }
            }

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
    }
}
