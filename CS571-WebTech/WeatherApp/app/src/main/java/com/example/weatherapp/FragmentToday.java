package com.example.weatherapp;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentToday#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentToday extends Fragment {
    private TextView windSpeed;
    private TextView pressure;
    private TextView precipitation;
    private TextView temperature;
    private TextView status;
    private TextView humidity;
    private TextView visibility;
    private TextView cloudCover;
    private TextView uvIndex;
    private ImageView statusIcon;


    private static final String ARG_PARAM1 = "data";
    private String dataString;

    public FragmentToday() {
        // Required empty public constructor
    }

    public static FragmentToday newInstance(String param1) {
        FragmentToday fragment = new FragmentToday();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataString = "";

        if (getArguments() != null) {
            dataString = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        windSpeed = view.findViewById(R.id.details_windspeed);
        pressure = view.findViewById(R.id.details_pressure);
        precipitation = view.findViewById(R.id.details_precipitation);
        temperature = view.findViewById(R.id.details_temp);
        status = view.findViewById(R.id.details_status);
        statusIcon = view.findViewById(R.id.details_status_icon);
        humidity = view.findViewById(R.id.details_humidity);
        visibility = view.findViewById(R.id.details_visibility);
        cloudCover = view.findViewById(R.id.details_cloudcover);
        uvIndex = view.findViewById(R.id.details_ozone);

        JSONObject weatherData = null;
        try {
            weatherData = new JSONObject(dataString);
            JSONObject current = weatherData.getJSONObject("data").getJSONArray("current").getJSONObject(0).getJSONObject("values");
            windSpeed.setText(current.getString("windSpeed") +" mph");
            pressure.setText(current.getString("pressureSeaLevel") +" inHg");
            Double rain = Double.parseDouble(current.getString("precipitationProbability"));
            precipitation.setText(String.format("%.2f", rain) + " %");
            int temp = (int) Math.round(Double.parseDouble(current.getString("temperature")));
            temperature.setText( temp +" °F");
            String statusCode = current.getString("weatherCode");
            WeatherCode codeMapper = new WeatherCode();
            String drawableName = codeMapper.lookup(statusCode).first;
            int id = getContext().getResources().getIdentifier(drawableName, "drawable", getContext().getPackageName());
            Drawable icon = getContext().getResources().getDrawable(id);
            statusIcon.setImageDrawable(icon);
            humidity.setText(current.getString("humidity") + " %");
            visibility.setText(current.getString("visibility") + " mi");
            cloudCover.setText(current.getString("cloudCover") + " %");
            Double uv = Double.parseDouble(current.getString("uvIndex"));
            uvIndex.setText( String.format("%.2f", uv)+ " ");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }
}