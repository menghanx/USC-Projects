package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ResultActivity extends AppCompatActivity implements  GeocodeApi.GeocodeApiCallback, TomorrowApi.TomorrowApiCallback {
    private ResultActivity curActivity;

    private RelativeLayout loadingContainer;
    private RelativeLayout resultContainer;

    private RelativeLayout cardOneContainer;
    private TextView cardOneLocation;
    private TextView cardOneTemp;
    private TextView cardOneStatus;
    private ImageView cardOneIcon;
    private TextView cardTwoHumidity;
    private TextView cardTwoWind;
    private TextView cardTwoVisibility;
    private TextView cardTwoPressure;
    private LinearLayout table;
    private FloatingActionButton fab;

    private String query;
    GeocodeApi geoApi;
    TomorrowApi tomorrowApi;
    WeatherData weatherData;

    // Favorites
    private boolean isFav;
    private String favString;
    private List<WeatherData> favorites;
    private boolean isLoadedData;

    private JSONObject states;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        try {
            states = new JSONObject(loadAssetJson("state_mapping"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        cardOneContainer = findViewById(R.id.card1_container);
        cardOneLocation = findViewById(R.id.card1_location);
        cardOneTemp = findViewById(R.id.card1_temp);
        cardOneStatus = findViewById(R.id.card1_status);
        cardOneIcon = findViewById(R.id.card1_icon);
        cardTwoHumidity = findViewById(R.id.card2_humidity);
        cardTwoWind = findViewById(R.id.card2_windspeed);
        cardTwoVisibility = findViewById(R.id.card2_visibility);
        cardTwoPressure = findViewById(R.id.card2_pressure);
        // Dynamically populate scrollView table
        table = findViewById(R.id.scroll_linear_layout);

        // Favorite button
        fab = findViewById(R.id.floatingActionButton4);
        isFav = false;
        isLoadedData = false;

        // Start loading screen
        resultContainer = findViewById(R.id.result_container);
        loadingContainer = findViewById(R.id.result_progressbar_container);
        startLoadingScreen();

        // query intent
        Intent intent = getIntent();
        if (intent.hasExtra("query")){
            query = intent.getStringExtra("query");
        }
        // get favorite list from main activity
        if (intent.hasExtra("fav_jsonarray")){
            favString = intent.getStringExtra("fav_jsonarray");
        }

        curActivity = this;
        Toolbar toolbar = findViewById(R.id.result_toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_left));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(curActivity, MainActivity.class);

                if (isFav){
                    intent.putExtra("newFav", weatherData.getWeatherObject().toString());
                }else{
                    if (isLoadedData) {
                        try {
                            intent.putExtra("toRemove", weatherData.getLatLng());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                startActivity(intent);
            }
        });

        TextView location = findViewById(R.id.result_location);
        location.setText(query);

        // TODO Disable Result page debug
        boolean debug = false;
        if(debug){
            try {
                loadDummyData();

                String latLng = this.weatherData.getLatLng();
                isThisFave(latLng);
                if (this.isFav){
                    JSONArray favs = new JSONArray(this.favString);

                    for (int i = 0; i < favs.length(); i++){
                        JSONObject json = favs.getJSONObject(i);
                        if (json.getString("latLng").equals(latLng)){
                            this.weatherData = new WeatherData(json);
                            this.isLoadedData = true;
                            break;
                        }
                    }
                }else{
                    loadDummyData();
                }
                populateViews();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            stopLoadingScreen();
        }else {
            // Look up address
            geoApi = new GeocodeApi(this);
            geoApi.setCallback(this);
            geoApi.geoLookup(query);
        }
    }

    private void isThisFave(String latLng) {
        if (favString == null ){
            this.isFav = false;
            return;
        }
        if (favString.length()>0){
            if (favString.contains(latLng)){
                this.isFav = true;
            }else{
                this.isFav = false;
            }
        }else {
            this.isFav = false;
        }
    }


    @Override
    public void onGeoSuccess(WeatherData data) throws JSONException {
        this.weatherData = data;


        String state = this.weatherData.getState();
        if (states.has(state)){
            String newState = this.states.getString(state);
            this.weatherData.setState(newState);
        }
        Log.d("DEBUG", this.weatherData.toString());

        String latLng = this.weatherData.getLatLng();
        isThisFave(latLng);
        // if fav, load the data
        if (this.isFav){
            JSONArray favs = new JSONArray(this.favString);

            for (int i = 0; i < favs.length(); i++){
                JSONObject json = favs.getJSONObject(i);
                if (json.getString("latLng").equals(latLng)){
                    this.weatherData = new WeatherData(json);
                    this.isLoadedData = true;
                    break;
                }
            }
            Log.d("DEBUG", this.weatherData.toString());
            populateViews();
            stopLoadingScreen();
        }else {
            // else call tomorrow
            tomorrowApi = new TomorrowApi(this);
            tomorrowApi.setCallback(this);
            tomorrowApi.updateWeatherData(this.weatherData);
        }
    }

    @Override
    public void onTomorrowSuccess(WeatherData data) throws JSONException {
        this.weatherData = data;
        // fill the data
        populateViews();
        // stop the loading screen
        stopLoadingScreen();
    }

    private void populateViews() throws JSONException {
        // CARD 1
        JSONObject currentData = weatherData.getCurrentData().getJSONObject(0);
        JSONObject currentValues = (JSONObject) currentData.get("values");
        cardOneLocation.setText(weatherData.getLocation());
        int temperature = (int) Math.round(Double.parseDouble(currentValues.get("temperature").toString()));
        cardOneTemp.setText(temperature + "°F");
        String statusCode = currentValues.get("weatherCode").toString();
        WeatherCode codeMapper = new WeatherCode();
        String drawableName = codeMapper.lookup(statusCode).first;
        int id = getResources().getIdentifier(drawableName, "drawable", getPackageName());
        Drawable icon = getResources().getDrawable(id);
        cardOneIcon.setImageDrawable(icon);
        cardOneStatus.setText(codeMapper.lookup(statusCode).second);
        cardOneContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(curActivity, DetailsActivity.class);
                String weatherObj = weatherData.getWeatherObject().toString();
                intent.putExtra("object", weatherObj);
                String latLng = null;
                try {
                    latLng = weatherData.getLatLng();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (isFav){
                    intent.putExtra("newFav", weatherObj);
                }else{
                    if (isLoadedData) {
                        intent.putExtra("toRemove", latLng);
                    }
                }

                curActivity.startActivity(intent);
            }
        });

        // CARD 2
        cardTwoHumidity.setText(currentValues.get("humidity").toString() + "%");
        cardTwoWind.setText(currentValues.get("windSpeed").toString() + "mph");
        cardTwoVisibility.setText(currentValues.get("visibility").toString() + "mi");
        cardTwoPressure.setText(currentValues.get("pressureSeaLevel").toString()+"inHg");

        // TABLE
        int background_id = getResources().getIdentifier("weather_card_background", "drawable", getPackageName());
        Drawable bg = getResources().getDrawable(background_id);
        JSONArray daysData = weatherData.getDayData();

        for (int i = 0; i < 8; i ++){
            JSONObject thisDay = (JSONObject) daysData.get(i);

            LinearLayout row = new LinearLayout(table.getContext());
            row.setBackground(bg);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,0,0,15);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(params);

            // Date
            TextView date = new TextView(row.getContext());
            String dateText = thisDay.get("startTime").toString();
            dateText = dateText.substring(0, dateText.lastIndexOf('T'));
            date.setText(dateText);
            date.setTextSize(20);
            date.setPadding(10,0,200,0);
            date.setTextColor(getResources().getColor(android.R.color.white));
            row.addView(date);

            // values for this day
            JSONObject thisValues = (JSONObject) thisDay.get("values");

            // Icon
            ImageView day_icon = new ImageView(row.getContext());
            String thisCode = thisValues.get("weatherCode").toString();
            String thisDrawable = codeMapper.lookup(thisCode).first;
            int d_id = getResources().getIdentifier(thisDrawable, "drawable", getPackageName());
            Drawable d_img = getResources().getDrawable(d_id);
            day_icon.setImageDrawable(d_img);
            LinearLayout.LayoutParams day_icon_params = new LinearLayout.LayoutParams(
                    80,
                    80
            );
            day_icon_params.setMargins(0,0,90,0);
            day_icon.setLayoutParams(day_icon_params);
            row.addView(day_icon);


            // Max Min Temp
            TextView temps = new TextView(row.getContext());
            int high_temp = (int) Math.round(Double.parseDouble(thisValues.get("temperatureMax").toString()));
            int low_temp = (int) Math.round(Double.parseDouble(thisValues.get("temperatureMin").toString()));
            temps.setText(low_temp + "        " + high_temp);
            temps.setTextSize(20);
            temps.setTextColor(getResources().getColor(android.R.color.white));
            row.addView(temps);

            // last step add row to table
            table.addView(row);
        }

        updateFabView(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFav();
            }
        });
    }

    private void toggleFav(){
        this.isFav = !this.isFav;
        updateFabView(true);
    }

    public void updateFabView(boolean showToast){
        String action;
        if (this.isFav){
            fab.setImageDrawable(getDrawable(R.drawable.map_marker_minus));
            action = " was added to favorites";
        }else{
            fab.setImageDrawable(getDrawable(R.drawable.map_marker_plus));
            action = " was removed from favorites";
        }
        String output = query + action;
        if (showToast){
            Toast.makeText(this, output, Toast.LENGTH_SHORT).show();
        }
    }

    private void startLoadingScreen(){
        resultContainer.setVisibility(View.INVISIBLE);
        loadingContainer.setVisibility(View.VISIBLE);
    }

    private void stopLoadingScreen(){
        resultContainer.setVisibility(View.VISIBLE);
        loadingContainer.setVisibility(View.INVISIBLE);
    }

    private void loadDummyData() throws JSONException {
        String filename = "dummy_result.json";
        this.weatherData = new WeatherData();
        this.weatherData.setCity("Burlington");
        this.weatherData.setState("MA");
        this.weatherData.setLatLng("42.544473,-71.1686381");

        try {
            InputStream is = this.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            JSONObject data = new JSONObject(new String(buffer, "UTF-8"));
            this.weatherData.updateJsonData(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadAssetJson(String filename) {
        String json = null;
        int res_id = getResources().getIdentifier(filename, "raw", this.getPackageName());
        try {
//            InputStream is = this.getAssets().open(filename);
            InputStream is = this.getResources().openRawResource(res_id);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}