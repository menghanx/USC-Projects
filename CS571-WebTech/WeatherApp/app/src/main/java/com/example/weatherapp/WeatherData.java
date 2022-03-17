package com.example.weatherapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Holds data for the give location
public class WeatherData {

    private JSONObject weatherObj;

    // Initialize an empty weatherData
    public WeatherData() throws JSONException {
        weatherObj = new JSONObject("{}");
        weatherObj.put("city","null");
        weatherObj.put("state","null");
        weatherObj.put("country","null");
        weatherObj.put("latLng","null");
        weatherObj.put("isHomePage",false);
        // key "data" was not added
    }

    public WeatherData(JSONObject obj){
        this.weatherObj = obj;
    }

    public WeatherData(String objString) throws JSONException {
        JSONObject json = new JSONObject(objString);
        this.weatherObj = json;
    }

    @Override
    public String toString() {
        return weatherObj.toString();
    }

    // Returen weatherObj in JSONObject format
    public JSONObject getWeatherObject() {
        return weatherObj;
    }

    // input should be data from tomorrow IO
    public void updateJsonData(JSONObject data) throws JSONException {
        weatherObj.put("data", data);
    }

    public JSONArray getCurrentData() throws JSONException {
        return weatherObj.getJSONObject("data").getJSONArray("current");
    }

    public JSONArray getHourData() throws JSONException {
        return weatherObj.getJSONObject("data").getJSONArray("1h");
    }

    public JSONArray getDayData() throws JSONException {
        return weatherObj.getJSONObject("data").getJSONArray("1d");
    }

    public void setCity(String city) throws JSONException {
        this.weatherObj.put("city", city);
    }

    public void setState(String state) throws JSONException {
        this.weatherObj.put("state", state);

    }

    public void setCountry(String country) throws JSONException {
        this.weatherObj.put("country", country);
    }

    public String getLatLng() throws JSONException {
        return weatherObj.getString("latLng");
    }

    public void setLatLng(String latLng) throws JSONException {
        weatherObj.put("latLng", latLng);
    }

    public boolean isHomePage() throws JSONException {
        return weatherObj.getBoolean("isHomePage");
    }

    public void setHomePage(boolean homePage) throws JSONException {
        weatherObj.put("isHomePage", homePage);
    }

    public String getLocation() throws JSONException {
        if (weatherObj.getString("country").equals("null")){
            return weatherObj.getString("city")+ ", " +
                    weatherObj.getString("state");
        }
        return weatherObj.getString("city") + ", " +
                weatherObj.getString("state") + ", " +
                weatherObj.getString("country");
    }

    public String getState() throws JSONException {
        return this.weatherObj.getString("state") ;
    }

}
