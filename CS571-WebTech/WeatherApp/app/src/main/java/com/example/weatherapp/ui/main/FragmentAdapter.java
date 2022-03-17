package com.example.weatherapp.ui.main;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.weatherapp.FragmentToday;
import com.example.weatherapp.FragmentWeatherData;
import com.example.weatherapp.FragmentWeekly;
import com.example.weatherapp.WeatherData;
import com.google.gson.Gson;

public class FragmentAdapter extends FragmentStateAdapter {

    String objString;

    public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, String objectString) {
        super(fragmentManager, lifecycle);
        this.objString = objectString;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position){
            case 1:
                return FragmentWeekly.newInstance(objString);
            case 2:
                return FragmentWeatherData.newInstance(objString);
        }

        return FragmentToday.newInstance(objString);
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
