package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IpinfoApi.IpinfoApiCallback, TomorrowApi.TomorrowApiCallback, AutoCompleteApi.autoCompleteApiCallback {
    MainActivity currentActivity;

    // Homepage toolbar
    private Toolbar searchbar;
    private static String[] SUGGESTIONS = new String[]{""};
    ArrayAdapter<String> autoAdapter;
    AutoCompleteApi auto;
    AutoCompleteTextView autoCompleteTextView;

    // Test SharedPreferences
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SAVE = "savedData";

    // Weather card slider and indicators
    private  WeatherCardAdapter weatherCardAdapter;
    private LinearLayout sliderIndicators;
    private ViewPager2 cardViewPager;
    private RelativeLayout loadingContainer;

    // weather data
    private List<WeatherData> viewDataList;
    private String loadedFavs;
    private String newFavString;
    private String latLngToRemove;

    private JSONObject states;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // Receive intent with new favorite
        Intent intent = getIntent();
        if (intent.hasExtra("newFav")){
            this.newFavString = intent.getStringExtra("newFav");
//            Log.d("DEBUG_NEW_FAV", " Got a new Fav " + this.newFavString);
        }
        if (intent.hasExtra("toRemove")){
            this.latLngToRemove = intent.getStringExtra("toRemove");
        }

        try {
            states = new JSONObject(loadAssetJson("state_mapping"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Read saved String from local storage
        try {
            loadFavorites();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setTheme(R.style.Theme_WeatherApp);
        setContentView(R.layout.activity_main);
        currentActivity = this;
        // populate search bar
        searchbar = findViewById(R.id.searchbar);
        setSupportActionBar(searchbar);
        auto = new AutoCompleteApi(currentActivity);
        auto.setCallback(currentActivity);

        // Container for loading screen
        loadingContainer = findViewById(R.id.dialogContainer);

        // create slider indicators container
        sliderIndicators = findViewById(R.id.cardIndicators);

        // view pager2
        cardViewPager = findViewById(R.id.weatherCardViewPager);

        viewDataList = new ArrayList<>();
        startLoadingScreen();

        IpinfoApi ipinfo = new IpinfoApi(this);
        ipinfo.setCallback(this);

        // callback function getCurrentLocation() will execute on success
        ipinfo.getCurLocation();

    }

    private void startLoadingScreen(){
        sliderIndicators.setVisibility(View.INVISIBLE);
        cardViewPager.setVisibility(View.INVISIBLE);
    }

    private void stopLoadingScreen(){
        sliderIndicators.setVisibility(View.VISIBLE);
        cardViewPager.setVisibility(View.VISIBLE);
        loadingContainer.setVisibility(View.INVISIBLE);
    }

    // Callback function return location info
    // set up list of WeatherData holds current location + favs
    // set up WeatherCardAdapter
    @Override
    public void onIpinfoSuccess(JSONObject json) throws JSONException {
//        List<WeatherData> weatherDataList = new ArrayList<>();

        String city = String.valueOf(json.get("city"));
        String state = String.valueOf(json.get("region"));
        String country = String.valueOf(json.get("country"));
        String latLng = String.valueOf(json.get("loc"));

        WeatherData one = new WeatherData();
        one.setCity(city);
        one.setState(state);
//        one.setCountry(country);
        one.setLatLng(latLng);
        one.setHomePage(true);
        viewDataList.add(one);

        // use local storage to populate favorites
        addFavs();

        // add new Favorite to the listF
        if (this.newFavString != null){
            WeatherData newFavData = new WeatherData(this.newFavString);
            viewDataList.add(newFavData);
        }

        // Get data for each saved location in the weatherDataList
        TomorrowApi tomorrowApi = new TomorrowApi(this);
        tomorrowApi.setCallback(this);

        // TODO: Disable DEBUG, Make sure debug is false for production
        boolean debug = false;
        if (debug){
            one.updateJsonData(new JSONObject(loadAssetJson("dummy")));

            String toSave = getFavString();
            if (toSave.length()>0) {
                saveFavorites(toSave);
            }
            populateViewPager(viewDataList);
            stopLoadingScreen();
        }else{
            // Get current date populated from tomorrow io
            viewDataList.set(0,tomorrowApi.updateWeatherData(viewDataList.get(0)) );
        }
    }

    public String getFavString() {
        if (viewDataList.size()<2 || viewDataList.isEmpty()){

            return "";
        }else{
//            Log.d("DEBUG_STR_FAV", "Making Fav String");
            JSONArray favs = new JSONArray();
            for (int i = 1; i < viewDataList.size(); i++){
                favs.put(viewDataList.get(i).getWeatherObject());
            }
//            Log.d("DEBUG_STR_FAV", favs.length()+"");
            return favs.toString();
        }
    }


    // Callback function for tomorrowApi, update viewDataList and stop the loading screen.
    @Override
    public void onTomorrowSuccess(WeatherData weatherData) throws JSONException {
        String toSave = getFavString();
        if (toSave.length()>0) {
            saveFavorites(toSave);
        }
        populateViewPager(viewDataList);
        stopLoadingScreen();
    }

    private void populateViewPager(List<WeatherData> weatherDataList) {
        weatherCardAdapter = new WeatherCardAdapter(weatherDataList, this);

        // set up viewPager2
        cardViewPager.setAdapter(weatherCardAdapter);

        // create indicators and highlight the default index
        setupSliderIndicators();
        setCurrentIndicator(0);

        // Link pageViewer with indicators
        cardViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position){
                super.onPageSelected(position);
                setCurrentIndicator(position);
            }
        });
    }

    // Save data to local
    public void saveFavorites(String toSave) throws JSONException {
//        Log.d("DEBUG_FAV_SAVED", toSave);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SAVE, toSave);
        editor.apply();
    }

    // Reload data from sharedPreferences
    public void loadFavorites() throws JSONException {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        loadedFavs = sharedPreferences.getString(SAVE, "");

        if (latLngToRemove!=null){
//            Log.d("FAV_REMOVE", ""+ (loadedFavs.contains(latLngToRemove)));
            if (loadedFavs.length() > 0){
                JSONArray favs = new JSONArray(loadedFavs);
                int removeIndex = -1;
                for (int i = 0; i < favs.length(); i++) {
                    JSONObject fav = favs.getJSONObject(i);
                    if (fav.getString("latLng").equals(this.latLngToRemove )){
                        removeIndex = i;
                    }
                }
                favs.remove(removeIndex);
                String newFavs = favs.toString();
//                Log.d("FAV_REMOVED_LOCAL", newFavs);
                saveFavorites(newFavs);
            }
        }
    }

    private void addFavs() throws JSONException {
        if(this.loadedFavs.length()>0) {
            JSONArray favs = new JSONArray(loadedFavs);
//            Log.d("DEBUG_FAV_LOADED_SIZE", "" + favs.length());
            for (int i = 0; i < favs.length(); i++) {
                JSONObject fav = favs.getJSONObject(i);
                if (fav.getString("latLng").equals(this.latLngToRemove )){
//                    Log.d("FAV_TO_REMOVE", "FOUND!");
                    continue;
                }
                WeatherData data = new WeatherData(fav);
                this.viewDataList.add(data);
            }
        }
    }
    // dynamically create indicators and add them to the linearLayout
    public void setupSliderIndicators(){
        // remove all the indicators each time this method is called
        sliderIndicators.removeAllViewsInLayout();
        ImageView[] indicators = new ImageView[weatherCardAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = 20;
        layoutParams.setMargins(margin,margin,margin,margin);
        for (int i = 0; i < indicators.length; i++){
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            sliderIndicators.addView(indicators[i]);
        }
    }

    // given and index, highlight the indicator at the index
    private void setCurrentIndicator(int index){
        int childCount = sliderIndicators.getChildCount();
        for (int i = 0; i < childCount; i++){
            ImageView imageView = (ImageView) sliderIndicators.getChildAt(i);
            if (i == index){
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.indicator_active)
                );
            }else{
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.indicator_inactive)
                );
            }
        }
    }

    // inflate menu search icon
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);


        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        autoCompleteTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        autoCompleteTextView.setThreshold(1);
        autoAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.suggestion, SUGGESTIONS);
        autoAdapter.setNotifyOnChange(true);
        autoCompleteTextView.setAdapter(autoAdapter);


        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // select suggestion
                searchView.setQuery(SUGGESTIONS[position], false);
            }
        });
        searchView.setQueryHint("Search...");

        // Action to do when search button is clicked
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent toSearch = new Intent(currentActivity, ResultActivity.class);
                toSearch.putExtra("query", query);
                if(viewDataList.size() > 1){
                    JSONArray array = new JSONArray();
                    for (int i = 1; i < viewDataList.size(); i++){
                        array.put(viewDataList.get(i).getWeatherObject());
                    }
//                    Log.d("DEBUG_PASS_FAV", array.length()+"");
                    toSearch.putExtra("fav_jsonarray", array.toString());
                }
                startActivity(toSearch);
                return false;
            }

            // Auto complete goes here
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()>0) {
                    try {
                        auto.getSuggestions(newText);
                        autoAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public void onAutoSuccess(JSONArray suggestions) throws JSONException {

        String[] newSugs = new String[suggestions.length()];

        for (int i = 0; i < suggestions.length(); i++) {
            JSONObject sugObj = suggestions.getJSONObject(i);
//            newSugs[i] = sugObj.getString("city") + ", " + sugObj.getString("state");
            String stateKey = sugObj.getString("state");
            String state = stateKey;

            if (states.has(stateKey)){
                state = states.getString(stateKey);
            }

            newSugs[i] = sugObj.getString("city") + ", " + state;

        }
        SUGGESTIONS = newSugs.clone();

        // Destroy and build suggestions again
        autoAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.suggestion, SUGGESTIONS);
        autoCompleteTextView.setAdapter(autoAdapter);
        autoAdapter.notifyDataSetChanged();

    }
    // handle menu icon click event
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //
//        switch (item.getItemId()) {
//            case R.id.search:
//                Toast.makeText(this, "Searching", Toast.LENGTH_SHORT).show();
//                break;
//        }
        return super.onOptionsItemSelected(item);
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