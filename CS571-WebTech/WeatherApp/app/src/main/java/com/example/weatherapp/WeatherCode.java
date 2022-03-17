package com.example.weatherapp;

import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// WeatherCode Lookup Singleton
public class WeatherCode {

    private static WeatherCode instance;
    private Map<String, Pair<String, String>> codeTable;
    private final String codeMap = "{\"1000\":[\"ic_clear_day\",\"Clear\"],\"1001\":[\"ic_cloudy\",\"Cloudy\"],\"1100\":[\"ic_mostly_clear_day\",\"Mostly Clear\"],\"1101\":[\"ic_partly_cloudy_day\",\"Partly Cloudy\"],\"1102\":[\"ic_mostly_cloudy\",\"Mostly Cloudy\"],\"2000\":[\"ic_fog\",\"Fog\"],\"2100\":[\"ic_fog_light\",\"Light Fog\"],\"4000\":[\"ic_drizzle\",\"Drizzle\"],\"4001\":[\"ic_rain\",\"Rain\"],\"4200\":[\"ic_rain_light\",\"Light Rain\"],\"4201\":[\"ic_rain_heavy\",\"Heavy Rain\"],\"5000\":[\"ic_snow\",\"Snow\"],\"5001\":[\"ic_flurries\",\"Flurries\"],\"5100\":[\"ic_snow_light\",\"Light Snow\"],\"5101\":[\"ic_snow_heavy\",\"Heavy Snow\"],\"6000\":[\"ic_freezing_drizzle\",\"Freezing Drizzle\"],\"6001\":[\"ic_freezing_rain\",\"Freezing Rain\"],\"6200\":[\"ic_freezing_rain_light\",\"Light Freezing Rain\"],\"6201\":[\"ic_freezing_rain_heavy\",\"Heavy Freezing Rain\"],\"7000\":[\"ic_ice_pellets\",\"Ice Pellets\"],\"7101\":[\"ic_ice_pellets_heavy\",\"Heavy Ice Pellets\"],\"7102\":[\"ic_ice_pellets_light\",\"Light Ice Pellets\"],\"8000\":[\"ic_tstorm\",\"Thunderstorm\"]}";
    WeatherCode(){
        codeTable = new HashMap<String, Pair<String, String>>();
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(codeMap);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Iterator<String> keys = jsonObject.keys();
        while(keys.hasNext()){
            String key = keys.next();
            try {
                JSONArray values = (JSONArray) jsonObject.get(key);
                String drawable = values.get(0).toString();
                String status = values.get(1).toString();
                this.codeTable.put(key, new Pair<>(drawable, status));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Pair<String, String> lookup(String code){
        if (!codeTable.containsKey(code)){
            return null;
        }
        return codeTable.get(code);
    }

}
