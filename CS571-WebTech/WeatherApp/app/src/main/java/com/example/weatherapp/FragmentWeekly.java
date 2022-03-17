package com.example.weatherapp;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.highsoft.highcharts.common.HIColor;
import com.highsoft.highcharts.common.HIGradient;
import com.highsoft.highcharts.common.HIStop;
import com.highsoft.highcharts.core.*;
import com.highsoft.highcharts.common.hichartsclasses.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentWeekly#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentWeekly extends Fragment {

    private static final String ARG_PARAM1 = "data";
    private String dataString;

    public FragmentWeekly() {
        // Required empty public constructor
    }

    public static FragmentWeekly newInstance(String param1) {
        FragmentWeekly fragment = new FragmentWeekly();
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
        View view = inflater.inflate(R.layout.fragment_weekly, container, false);
        JSONObject json = null;
        try {
            json = new JSONObject(dataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HIChartView chartView = (HIChartView) view.findViewById(R.id.range_hc);

        HIOptions options = new HIOptions();

        HIChart chart = new HIChart();
        chart.setType("arearange");
        chart.setZoomType("x");

        options.setChart(chart);

        HITitle title = new HITitle();
        title.setText("Temperature variation by day");
        options.setTitle(title);


        HIXAxis xaxis = new HIXAxis();
//        xaxis.setCrosshair(new HICrosshair());
        //TODO check highchart x-axis

        // date
        xaxis.setType("time");
//        xaxis.setTickInterval(24 * 3600 * 1000);
        xaxis.setTickInterval(2);

        int size = 0;
        try {
            size = json.getJSONObject("data").getJSONArray("1d").length();
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        ArrayList<String> cats = new ArrayList<>();
//        for (int i = 0; i < size; i++){
//            cats.add(""+i);
//        }
//        xaxis.setMin(0);
//        xaxis.setMax(size);
//        xaxis.setTickInterval(2);




        options.setXAxis(new ArrayList<HIXAxis>(){{add(xaxis);}});


        HIYAxis yaxis = new HIYAxis();
        yaxis.setTitle(new HITitle());
        options.setYAxis(new ArrayList<HIYAxis>(){{add(yaxis);}});

        HITooltip tooltip = new HITooltip();
        tooltip.setShadow(true);
        tooltip.setShared(true);
        tooltip.setValueSuffix("°F");
        tooltip.setXDateFormat("%Y-%m-%d");
        options.setTooltip(tooltip);


        HIPlotOptions plotOptions = new HIPlotOptions();
        HISeries plotSeries = new HISeries();
        // Linear gradient
        HIGradient gradient = new HIGradient(0, 0.2f, 0.5f, 1);
        LinkedList<HIStop> stops = new LinkedList<>();
        stops.add(new HIStop(0, HIColor.initWithRGB(246, 186, 86)));
        stops.add(new HIStop(1, HIColor.initWithRGB(125, 182, 242)));
        plotSeries.setColor(HIColor.initWithLinearGradient(gradient, stops));



        plotOptions.setSeries(plotSeries);
        options.setPlotOptions(plotOptions);

        
        HILegend legend = new HILegend();
        legend.setEnabled(false);
        options.setLegend(legend);

        HIArearange series = new HIArearange();
        series.setName("Temperatures");

        ArrayList<ArrayList<Object>> data = null;
        try {
            data = getData(json);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

        series.setData(data);
        options.setSeries(new ArrayList<>(Arrays.asList(series)));


        chartView.setOptions(options);

        return view;
    }

    private ArrayList<ArrayList<Object>> getData(JSONObject json) throws JSONException, ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.ENGLISH);

        JSONArray days = json.getJSONObject("data").getJSONArray("1d");

        ArrayList<ArrayList<Object>> res = new ArrayList<>();

        for (int i = 0; i < days.length(); i++){
            ArrayList<Object> dayData = new ArrayList<>();

            // date mills
            String startTime = days.getJSONObject(i).getString("startTime");
//            Date date = formatter.parse(startTime);
//            dayData.add(i);
            startTime = startTime.split("T")[0];
            dayData.add(startTime);
            // min max temp
            Double min_temp = Double.parseDouble(days.getJSONObject(i).getJSONObject("values").getString("temperatureMin"));
            Double max_temp = Double.parseDouble(days.getJSONObject(i).getJSONObject("values").getString("temperatureMax"));
            dayData.add(min_temp);
            dayData.add(max_temp);
            res.add(dayData);
        }
        return res;
    }
}