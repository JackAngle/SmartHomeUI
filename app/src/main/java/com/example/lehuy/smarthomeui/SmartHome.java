package com.example.lehuy.smarthomeui;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import android.graphics.*;

import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class SmartHome extends AppCompatActivity {

    private int count = 0;
    private TextView mText = null;
    private ImageButton mImageButton01 = null;
    private ImageButton mImageButton02 = null;
    private ImageButton mImageButton03 = null;
    private ImageButton mImageButton04 = null;
    private boolean imageStatus01 = false;
    private boolean imageStatus02 = false;
    private int imageStatus03 = 0;

    /*Control commnand define */
    private final int FIRST_LIGHT_ON = 1;
    private final int FIRST_LIGHT_OFF = 2;
    private final int SECOND_LIGHT_ON = 3;
    private final int SECOND_LIGHT_OFF = 4;
    private final int FAN_UP = 5;
    private final int FAN_OFF = 6;
    private final int ENVIRONMENT_INFO_UPDATE = 7;

    private int command = 0;
    private RequestQueue mRequestQueue = null;
    //private final String url = "https://api.thingspeak.com/channels/630223/fields/3/last.txt?api_key=L681BGTSS07Z3EUK";
    private final String GET_URL_HEADER = "https://api.thingspeak.com/channels/630223/fields/";
    private final String GET_URL_FOOTER = "/last.txt?api_key=L681BGTSS07Z3EUK";
    private final String POST_URL_HEADER = "https://api.thingspeak.com/update"
                                            + "?api_key=OB1A7ARSVFCK5510";
    private final int TEMPERATURE_FIELD = 1;
    private final int LUX_FIELD = 2;
    private final int FIRST_LIGHT_FIELD = 3;
    private final int SECOND_LIGHT_FIELD = 4;
    private final int FAN_FIELD = 5;

    private List<DataViewer> image_details = null;
    private DataViewer temperature = null;
    private DataViewer lightDensity = null;

    private CustomGridAdapter mAdapter = null;

    /*Runnable Thread*/
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            controlMethod(command);
            //sendGETRequest();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_smart_home);

        image_details = getListData();
        final GridView gridView = (GridView) findViewById(R.id.gridView01);
        mAdapter = new CustomGridAdapter(this, image_details);
        gridView.setAdapter(mAdapter);

        //Intialize UI elements
        mText = (TextView) findViewById(R.id.text);
        mImageButton01 = (ImageButton) findViewById(R.id.imageButton01);
        mImageButton02 = (ImageButton) findViewById(R.id.imageButton02);
        mImageButton03 = (ImageButton) findViewById(R.id.imageButton03);
        mImageButton04 = (ImageButton) findViewById(R.id.imageButton04);

        //set listener
        mImageButton01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!imageStatus01) {
                   // mImageButton01.setImageResource(R.drawable.light_icon);
                    command = FIRST_LIGHT_ON;
                    mHandler.post(mRunnable);
                } else{
                    //mImageButton01.setImageResource(R.drawable.bulblight);
                    command = FIRST_LIGHT_OFF;
                    mHandler.post(mRunnable);
                }
            }
        });

        mImageButton02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!imageStatus02) {
                    //mImageButton02.setImageResource(R.drawable.light_icon);
                    command = SECOND_LIGHT_ON;
                    mHandler.post(mRunnable);
                } else{
                    //mImageButton02.setImageResource(R.drawable.bulblight);
                    command = SECOND_LIGHT_OFF;
                    mHandler.post(mRunnable);
                }
            }
        });

        mImageButton03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //Set fan's imageStatus to 0 if current value is greater than 2

                int temp = imageStatus03 + 1;
                if (temp > 2){
                    temp = 0;
                }
                if (temp == 0) {
                    //mImageButton03.setImageResource(R.drawable.fan);
                    command = FAN_OFF;
                    mHandler.post(mRunnable);
                } else if (temp == 1){
                   // mImageButton03.setImageResource(R.drawable.fan02);
                    command = FAN_UP;
                    mHandler.post(mRunnable);
                } else {
                   // mImageButton03.setImageResource(R.drawable.fan03);
                    command = FAN_UP;
                    mHandler.post(mRunnable);
                }
            }
        });

        mImageButton04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                command = ENVIRONMENT_INFO_UPDATE;
                mHandler.post(mRunnable);
            }
        });

        /*Get devices status & environment info*/
        updateDevicesStatus();
        updateEnvironmentInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /*Create & return List<DataViewer>*/
    private  List<DataViewer> getListData() {
        List<DataViewer> list = new ArrayList<DataViewer>();
        temperature = new DataViewer("30", "temperature_icon", "Celcius");
        lightDensity = new DataViewer("100", "sun", "Lux");


        list.add(temperature);
        list.add(lightDensity);

        return list;
    }

/*
    public void sayHi(View view) {

        if (count == 0) {
            mText.setText("WATASHI WA NAMBAH WAN!");
            mText.setTextColor(Color.RED);
            count = 1;
        }else if (count == 1) {
            mText.setText("WATASHI WA NAMBAH WAN!");
            mText.setTextColor(Color.BLACK);
            count = 0;
        }
    }
    */

    /*
    FIRST_LIGHT_ON = 1;
    FIRST_LIGHT_OFF = 2;
    FIRST_LIGHT_GET = 3;
    SECOND_LIGHT_ON = 4;
    SECOND_LIGHT_OFF = 5;
    SECOND_LIGHT_GET = 6;
    FAN_UP = 7;
    fanDown = 8;
    FAN_GET = 9;
    ENVIRONMENT_INFO_UPDATE = 10;
    */
    private void controlMethod(int command){
        switch(command){
            case FIRST_LIGHT_ON:
                sendPOSTRequest(1, 1);
                break;
            case FIRST_LIGHT_OFF:
                sendPOSTRequest(1, 0);
                break;
            case SECOND_LIGHT_ON:
                sendPOSTRequest(2, 1);
                break;
            case SECOND_LIGHT_OFF:
                sendPOSTRequest(2, 0);
                break;
            case FAN_UP:
                /*I realized that (imageStatus03 + 1) is equals to the value i want to set
                => I decided to set (imageStatus03 + 1) as data value for fan-controlling
                 */
                sendPOSTRequest(3, imageStatus03 + 1);
                break;
            case FAN_OFF:
                sendPOSTRequest(3, 0);
                break;
            case ENVIRONMENT_INFO_UPDATE:
                updateEnvironmentInfo();
                updateDevicesStatus();
                break;
        }
    }

    /*Get temperature and light density datas from Thingspeak & print to device's screen*/
    private void updateEnvironmentInfo(){
        /*Get temperature data*/
        String url = GET_URL_HEADER + TEMPERATURE_FIELD + GET_URL_FOOTER;
        Log.i("url:","" + url);
        StringRequest tempRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("StringRequest", "Success");
                Log.d("Temp:", "" + response);
                if(response.equals("nan")){
                    temperature.setData("0");
                }
                else {
                    temperature.setData(response);
                }
                mAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("StringRequest", "Failed");
            }
        });
        mRequestQueue.add(tempRequest);

        /*Get lux data*/
        url = GET_URL_HEADER + LUX_FIELD + GET_URL_FOOTER;
        Log.i("url:","" + url);
        StringRequest luxRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("StringRequest", "Success");
                Log.d("Lux: ", "" + response);
                if(response.equals("nan")){
                    lightDensity.setData("0");
                }
                else {
                    lightDensity.setData(response);
                }
                mAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("StringRequest", "Failed");
            }
        });
        mRequestQueue.add(luxRequest);
    }

    /*
    Device number: 1 - First Light, 2 - Second Light, 3 - Fan
    Field number = Device number + 2;
     */
    private void deviceControlHandler(final int device){
        switch (device){
            case 1:
                imageStatus01 = (!imageStatus01);
                if (imageStatus01) {
                    mImageButton01.setImageResource(R.drawable.light_icon);

                } else{
                    mImageButton01.setImageResource(R.drawable.bulblight);
                }
                break;
            case 2:
                imageStatus02 = (!imageStatus02);
                if (imageStatus02) {
                    mImageButton02.setImageResource(R.drawable.light_icon);
                } else{
                    mImageButton02.setImageResource(R.drawable.bulblight);
                }
                break;
            case 3:
                imageStatus03 = imageStatus03 + 1;
                if (imageStatus03 > 2){
                    imageStatus03 = 0;
                }
                if (imageStatus03 == 0) {
                    mImageButton03.setImageResource(R.drawable.fan);
                } else if (imageStatus03 == 1){
                    mImageButton03.setImageResource(R.drawable.fan02);
                } else if (imageStatus03 == 2){
                    mImageButton03.setImageResource(R.drawable.fan03);
                }
                break;
        }
    }

    private void sendPOSTRequest(final int device, int data){
        if (device <4) {
            String field = "&field" + (device + 2);
            String url = POST_URL_HEADER + field + "=" + data;
            Log.d("URL", "" + url);
            StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("POSTRequest", "Success");
                    Log.d("POST: ", "" + response);
                    if ((!response.equals("0"))&&(!response.equals("null"))){
                        deviceControlHandler(device);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("StringRequest", "" + error.networkResponse);
                }
            });
            mRequestQueue.add(postRequest);
        }
    }

    private void updateDevicesStatus(){
        /*Get first light status & set ImageButton image*/
        String url = GET_URL_HEADER + FIRST_LIGHT_FIELD + GET_URL_FOOTER;
        Log.i("url:","" + url);
        StringRequest firstLightRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("StringRequest", "Success");
                Log.d("FirstLight:", "" + response);
                if (response.equals("1")){
                    mImageButton01.setImageResource(R.drawable.light_icon);
                    imageStatus01 = true;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("StringRequest", "Failed");
            }
        });
        mRequestQueue.add(firstLightRequest);

        /*Get second light status & set ImageButton image*/
        url = GET_URL_HEADER + SECOND_LIGHT_FIELD + GET_URL_FOOTER;
        Log.i("url:","" + url);
        StringRequest secondLightRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("StringRequest", "Success");
                Log.d("SecondLight:", "" + response);
                if (response.equals("1")){
                    mImageButton02.setImageResource(R.drawable.light_icon);
                    imageStatus02 = true;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("StringRequest", "Failed");
            }
        });
        mRequestQueue.add(secondLightRequest);

        /*Get fan status & set ImageButton image*/
        url = GET_URL_HEADER + FAN_FIELD + GET_URL_FOOTER;
        Log.i("url:","" + url);
        StringRequest fanRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("StringRequest", "Success");
                Log.d("Fan:", "" + response);

                if (response.equals("0")) {
                    mImageButton03.setImageResource(R.drawable.fan);
                    imageStatus03 = 0;
                } else if (response.equals("1")){
                    mImageButton03.setImageResource(R.drawable.fan02);
                    imageStatus03 = 1;
                } else if (response.equals("2")){
                    mImageButton03.setImageResource(R.drawable.fan03);
                    imageStatus03 = 2;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("StringRequest", "Failed");
            }
        });
        mRequestQueue.add(fanRequest);
    }

}
