package com.example.weatherapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TomorrowApi {
    Context context;
    TomorrowApiCallback callback;

    public TomorrowApi(Context context) {
        this.context = context;
    }

    public void setCallback(TomorrowApiCallback callback){
        this.callback = callback;
    }

    public WeatherData updateWeatherData(WeatherData weatherData) throws JSONException {
        String latLng = weatherData.getLatLng();
        // TODO remove local
        String url ="https://sunny-day-cycling.wl.r.appspot.com/api/weather?loc=" + latLng;
//        String url ="http://localhost:3000/api/weather?loc=" + latLng;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
//                            json = new JSONObject(response.toString());
                            weatherData.updateJsonData(new JSONObject(response.toString()));
                            callback.onTomorrowSuccess(weatherData);
//                            Toast.makeText(context, json.toString(), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        VolleySingleton.getInstance(context).addToRequestQueue(request);

        return weatherData;
    }

    public interface TomorrowApiCallback{
        void onTomorrowSuccess(WeatherData weatherData) throws JSONException;
    }
}


