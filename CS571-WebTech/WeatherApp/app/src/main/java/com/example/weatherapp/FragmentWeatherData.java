package com.example.weatherapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.highsoft.highcharts.core.HIChartView;
import com.highsoft.highcharts.common.HIColor;
import com.highsoft.highcharts.common.HIGradient;
import com.highsoft.highcharts.common.HIStop;
import com.highsoft.highcharts.core.*;
import com.highsoft.highcharts.common.hichartsclasses.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentWeatherData#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentWeatherData extends Fragment {

    private static final String ARG_PARAM1 = "data";

    private String dataString;
    private String renderIconsString = "function renderIcons() {" +
            "                            if(!this.series[0].icon) {" +
            "                               this.series[0].icon = this.renderer.path(['M', -8, 0, 'L', 8, 0, 'M', 0, -8, 'L', 8, 0, 0, 8]).attr({'stroke': '#303030','stroke-linecap': 'round','stroke-linejoin': 'round','stroke-width': 2,'zIndex': 10}).add(this.series[2].group);}this.series[0].icon.translate(this.chartWidth / 2 - 10,this.plotHeight / 2 - this.series[0].points[0].shapeArgs.innerR -(this.series[0].points[0].shapeArgs.r - this.series[0].points[0].shapeArgs.innerR) / 2); if(!this.series[1].icon) {this.series[1].icon = this.renderer.path(['M', -8, 0, 'L', 8, 0, 'M', 0, -8, 'L', 8, 0, 0, 8,'M', 8, -8, 'L', 16, 0, 8, 8]).attr({'stroke': '#ffffff','stroke-linecap': 'round','stroke-linejoin': 'round','stroke-width': 2,'zIndex': 10}).add(this.series[2].group);}this.series[1].icon.translate(this.chartWidth / 2 - 10,this.plotHeight / 2 - this.series[1].points[0].shapeArgs.innerR -(this.series[1].points[0].shapeArgs.r - this.series[1].points[0].shapeArgs.innerR) / 2); if(!this.series[2].icon) {this.series[2].icon = this.renderer.path(['M', 0, 8, 'L', 0, -8, 'M', -8, 0, 'L', 0, -8, 8, 0]).attr({'stroke': '#303030','stroke-linecap': 'round','stroke-linejoin': 'round','stroke-width': 2,'zIndex': 10}).add(this.series[2].group);}this.series[2].icon.translate(this.chartWidth / 2 - 10,this.plotHeight / 2 - this.series[2].points[0].shapeArgs.innerR -(this.series[2].points[0].shapeArgs.r - this.series[2].points[0].shapeArgs.innerR) / 2);}";

    public FragmentWeatherData() {
        // Required empty public constructor
    }

    public static FragmentWeatherData newInstance(String param1) {
        FragmentWeatherData fragment = new FragmentWeatherData();
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
        View view = inflater.inflate(R.layout.fragment_weather_data, container, false);

        JSONObject current = null;
        try {
            current = new JSONObject(dataString).getJSONObject("data").getJSONArray("current").getJSONObject(0).getJSONObject("values");
//            Log.d("TEST", current.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HIChartView chartView = (HIChartView) view.findViewById(R.id.gauge_hc);
        HIOptions options = new HIOptions();

        HIChart chart = new HIChart();
        chart.setType("solidgauge");
        chart.setEvents(new HIEvents());
        chart.getEvents().setRender(new HIFunction(renderIconsString));
        options.setChart(chart);

        HITitle title = new HITitle();
        title.setText("Stat Summary");
        title.setStyle(new HICSSObject());
        title.getStyle().setFontSize("24px");
        options.setTitle(title);

        HITooltip tooltip = new HITooltip();
        tooltip.setBorderWidth(0);
        tooltip.setBackgroundColor(HIColor.initWithName("none"));
        tooltip.setShadow(false);
        tooltip.setStyle(new HICSSObject());
        tooltip.getStyle().setFontSize("16px");
        tooltip.setPointFormat("{series.name}<br><span style=\"font-size:2em; color: {point.color}; font-weight: bold\">{point.y}%</span>");
        tooltip.setPositioner(
                new HIFunction(
                        "function (labelWidth) {" +
                                "   return {" +
                                "       x: (this.chart.chartWidth - labelWidth) /2," +
                                "       y: (this.chart.plotHeight / 2) + 15" +
                                "   };" +
                                "}"
                ));
        options.setTooltip(tooltip);

        HIPane pane = new HIPane();
        pane.setStartAngle(0);
        pane.setEndAngle(360);

        HIBackground paneBackground1 = new HIBackground();
        paneBackground1.setOuterRadius("112%");
        paneBackground1.setInnerRadius("88%");
        paneBackground1.setBackgroundColor(HIColor.initWithRGBA(97, 151, 11, 0.35));
        paneBackground1.setBorderWidth(0);

        HIBackground paneBackground2 = new HIBackground();
        paneBackground2.setOuterRadius("87%");
        paneBackground2.setInnerRadius("63%");
        paneBackground2.setBackgroundColor(HIColor.initWithRGBA(100, 181, 255, 0.35));
        paneBackground2.setBorderWidth(0);

        HIBackground paneBackground3 = new HIBackground();
        paneBackground3.setOuterRadius("62%");
        paneBackground3.setInnerRadius("38%");
        paneBackground3.setBackgroundColor(HIColor.initWithRGBA(253, 139, 105, 0.35));
        paneBackground3.setBorderWidth(0);

        pane.setBackground(new ArrayList<>(Arrays.asList(paneBackground1, paneBackground2, paneBackground3)));
        options.setPane(pane);

        HIYAxis yaxis = new HIYAxis();
        yaxis.setMin(0);
        yaxis.setMax(100);
        yaxis.setLineWidth(0);
        yaxis.setTickPositions(new ArrayList<>()); // to remove ticks from the chart
        options.setYAxis(new ArrayList<>(Collections.singletonList(yaxis)));

        HIPlotOptions plotOptions = new HIPlotOptions();
        plotOptions.setSolidgauge(new HISolidgauge());

        HIDataLabels dataLabels = new HIDataLabels();
        dataLabels.setEnabled(false);
        ArrayList<HIDataLabels> dataLabelsList = new ArrayList<>();
        dataLabelsList.add(dataLabels);
        plotOptions.getSolidgauge().setDataLabels(dataLabelsList);

        plotOptions.getSolidgauge().setLinecap("round");
        plotOptions.getSolidgauge().setStickyTracking(false);
        plotOptions.getSolidgauge().setRounded(true);
        options.setPlotOptions(plotOptions);

        HISolidgauge solidgauge1 = new HISolidgauge();
        solidgauge1.setName("Cloud Cover");
        HIData data1 = new HIData();
        data1.setColor(HIColor.initWithRGB(97, 151, 11));
        data1.setRadius("112%");
        data1.setInnerRadius("88%");
//        data1.setY(33);

        try {
            data1.setY((int) Math.round(Double.parseDouble(current.getString("cloudCover"))));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        solidgauge1.setData(new ArrayList<>(Collections.singletonList(data1)));

        HISolidgauge solidgauge2 = new HISolidgauge();
        solidgauge2.setName("Precipitation");
        HIData data2 = new HIData();
        data2.setColor(HIColor.initWithRGB(100, 181, 255));
        data2.setRadius("87%");
        data2.setInnerRadius("63%");
//        data2.setY(22);
        try {
            data2.setY((int) Math.round(Double.parseDouble(current.getString("precipitationProbability"))));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        solidgauge2.setData(new ArrayList<>(Collections.singletonList(data2)));

        HISolidgauge solidgauge3 = new HISolidgauge();
        solidgauge3.setName("Humidity");
        HIData data3 = new HIData();
        data3.setColor(HIColor.initWithRGB(253, 139, 105));
        data3.setRadius("62%");
        data3.setInnerRadius("38%");
//        data3.setY(22);
        try {
            data3.setY((int) Math.round(Double.parseDouble(current.getString("humidity"))));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        solidgauge3.setData(new ArrayList<>(Collections.singletonList(data3)));

        options.setSeries(new ArrayList<>(Arrays.asList(solidgauge1, solidgauge2, solidgauge3)));

        chartView.setOptions(options);

        return view;
    }
}