package com.example.weatherapp;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


public class GeocodeApi {
    Context context;
    GeocodeApiCallback callback;
    WeatherData weatherData;
    private String states;

    public GeocodeApi(Context context){
        this.context = context;

    }

    public void setCallback(GeocodeApiCallback callback){
        this.callback = callback;
    }

    public WeatherData geoLookup(String query){
        String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?key=%s&address=%s",
                "AIzaSyCLJBmNPC4h2bQlqiUl17X0m0hzYMgKzAs",
                query);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject json = new JSONObject(response.toString());
                            JSONObject results = json.getJSONArray("results").getJSONObject(0);
                            String formattedAddress = results.getString("formatted_address");
                            JSONObject geoLocation = results.getJSONObject("geometry").getJSONObject("location");
                            String latLng = geoLocation.getString("lat")+","+geoLocation.getString("lng");
                            String[] locations = formattedAddress.split(",");
                            weatherData = new WeatherData();
                            weatherData.setCity(locations[0].trim());
                            weatherData.setState(locations[1].trim());

//                            weatherData.setCountry(locations[2].trim());
                            weatherData.setLatLng(latLng);
                            callback.onGeoSuccess(weatherData);
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

    public interface GeocodeApiCallback{
        void onGeoSuccess(WeatherData weatherData) throws JSONException;
    }
}
