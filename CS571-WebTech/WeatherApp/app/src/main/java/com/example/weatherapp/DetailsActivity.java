package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapp.ui.main.FragmentAdapter;


import com.google.android.material.tabs.TabLayout;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class DetailsActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 pager2;
    FragmentAdapter fragAdapter;
    DetailsActivity curActivity;
    String newFav;
    String toRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_WeatherApp);
        setContentView(R.layout.activity_details);
        curActivity = this;



        // Uncomment it if need to set it as action bar
        //setSupportActionBar(toolbar);
        TextView location = findViewById(R.id.details_location);
        ImageView twitter = findViewById(R.id.twitter_button);

        Intent intent = getIntent();
        String objectString = intent.getStringExtra("object");

        if (intent.hasExtra("newFav")){
            this.newFav = intent.getStringExtra("newFav");
        }

        if (intent.hasExtra("toRemove")){
            this.toRemove = intent.getStringExtra("toRemove");
        }

        Toolbar toolbar = findViewById(R.id.details_toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.arrow_left));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(curActivity, MainActivity.class);
                if (newFav!=null){
                    intent.putExtra("newFav", newFav);
                }
                if(toRemove!=null){
                    intent.putExtra("toRemove",toRemove);
                }
                startActivity(intent);
            }
        });

        WeatherData data = null;
        try {
            data = new WeatherData(new JSONObject(objectString));
            String loc = data.getLocation();
            location.setText(loc);
            JSONObject current = data.getCurrentData().getJSONObject(0).getJSONObject("values");
            int temp = (int) Math.round(Double.parseDouble(current.getString("temperature")));
            twitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String tweet = "Check Out "+ loc + "’s Weather! It is " + temp +"°F!";
                    String hashtag = "CSCI571WeatherSearch";
                    String tweetUrl = String.format("https://twitter.com/intent/tweet?text=%s&hashtags=%s",
                            urlEncode(tweet),
                            urlEncode(hashtag));
                    Intent ti = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));
                    startActivity(ti);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


//        // set up adapter
        tabLayout = findViewById(R.id.tab_layout);
        pager2 = findViewById(R.id.view_pager2);

        FragmentManager fm = getSupportFragmentManager();

        fragAdapter = new FragmentAdapter(fm, getLifecycle(), objectString);
        pager2.setAdapter(fragAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoder.encode() failed for " + s);
        }
    }
}