package com.example.weatherapp;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import java.util.List;

public class WeatherCardAdapter extends RecyclerView.Adapter<WeatherCardAdapter.WeatherCardViewHolder>{

    private List<WeatherData> weatherDataItems;
    private MainActivity mainActivity;



    public WeatherCardAdapter(List<WeatherData> weatherDataItems, MainActivity mainActivity) {
        this.weatherDataItems = weatherDataItems;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public WeatherCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WeatherCardViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.weather_card_container, parent, false
                ), mainActivity
        );
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherCardViewHolder holder, int position) {
        boolean isHomePage = (position == 0);
        try {
            holder.setWeatherCardData(weatherDataItems.get(position), isHomePage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weatherDataItems.size();
    }

    class WeatherCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MainActivity parentActivity;

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


        public WeatherCardViewHolder(@NonNull View itemView, MainActivity mainActivity) {
            super(itemView);
            parentActivity = mainActivity;

            cardOneContainer = itemView.findViewById(R.id.card1_container);
            cardOneLocation = itemView.findViewById(R.id.card1_location);
            cardOneTemp = itemView.findViewById(R.id.card1_temp);
            cardOneStatus = itemView.findViewById(R.id.card1_status);
            cardOneIcon = itemView.findViewById(R.id.card1_icon);

            cardTwoHumidity = itemView.findViewById(R.id.card2_humidity);
            cardTwoWind = itemView.findViewById(R.id.card2_windspeed);
            cardTwoVisibility = itemView.findViewById(R.id.card2_visibility);
            cardTwoPressure = itemView.findViewById(R.id.card2_pressure);
            // Dynamically populate scrollView table
            table = itemView.findViewById(R.id.scroll_linear_layout);
            fab = itemView.findViewById(R.id.floatingActionButton4);
        }

        void setWeatherCardData(WeatherData weatherData, boolean isHomePage) throws JSONException {
            // disable the FAB if the view is home page
            if (weatherData.isHomePage()){
                fab.setVisibility(View.INVISIBLE);
            }else{
                fab.setOnClickListener(this);
            }
            // CARD 1 :
            // get current weather data
            JSONObject currentData = weatherData.getCurrentData().getJSONObject(0);
            JSONObject currentValues = (JSONObject) currentData.get("values");

            // update values of views
            cardOneLocation.setText(weatherData.getLocation());

            int temperature = (int) Math.round(Double.parseDouble(currentValues.get("temperature").toString()));
            cardOneTemp.setText(temperature + "°F");

            String statusCode = currentValues.get("weatherCode").toString();
            WeatherCode codeMapper = new WeatherCode();
            String drawableName = codeMapper.lookup(statusCode).first;
            int id = mainActivity.getResources().getIdentifier(drawableName, "drawable", mainActivity.getPackageName());
            Drawable icon = mainActivity.getResources().getDrawable(id);
            cardOneIcon.setImageDrawable(icon);

            cardOneStatus.setText(codeMapper.lookup(statusCode).second);
            // Card 1 click listener:
            cardOneContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(parentActivity, DetailsActivity.class);
                    String weatherObj = weatherData.getWeatherObject().toString();
                    intent.putExtra("object", weatherObj);
                    parentActivity.startActivity(intent);
                }
            });




            // CARD 2:
            cardTwoHumidity.setText(currentValues.get("humidity").toString() + "%");
            cardTwoWind.setText(currentValues.get("windSpeed").toString() + "mph");
            cardTwoVisibility.setText(currentValues.get("visibility").toString() + "mi");
            cardTwoPressure.setText(currentValues.get("pressureSeaLevel").toString()+"inHg");
            // update other data here

            // card background drawable
            int background_id = mainActivity.getResources().getIdentifier("weather_card_background", "drawable", mainActivity.getPackageName());
            Drawable bg = mainActivity.getResources().getDrawable(background_id);

            JSONArray daysData = weatherData.getDayData();

            // just populate 8 rows
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
                date.setTextColor(mainActivity.getResources().getColor(android.R.color.white));
                row.addView(date);

                // values for this day
                JSONObject thisValues = (JSONObject) thisDay.get("values");

                // Icon
                ImageView day_icon = new ImageView(row.getContext());
                String thisCode = thisValues.get("weatherCode").toString();
                String thisDrawable = codeMapper.lookup(thisCode).first;
                int d_id = mainActivity.getResources().getIdentifier(thisDrawable, "drawable", mainActivity.getPackageName());
                Drawable d_img = mainActivity.getResources().getDrawable(d_id);
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
                temps.setTextColor(mainActivity.getResources().getColor(android.R.color.white));
                row.addView(temps);

                // last step add row to table
                table.addView(row);
            }
        }

        // Removing Favs
        @Override
        public void onClick(View v) {
            String text = cardOneLocation.getText() + " was removed from favorites";
            Toast.makeText(v.getContext(), text, Toast.LENGTH_SHORT).show();

            try {
                removeAt(getAdapterPosition());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // remove item from data list and notify changes
    private void removeAt(int position) throws JSONException {
        weatherDataItems.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, weatherDataItems.size());
        mainActivity.saveFavorites(mainActivity.getFavString());
        // update indicator
        mainActivity.setupSliderIndicators();
    }
}
