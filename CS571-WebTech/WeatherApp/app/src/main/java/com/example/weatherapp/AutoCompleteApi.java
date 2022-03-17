package com.example.weatherapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;


public class AutoCompleteApi {
    Context context;
    autoCompleteApiCallback callback;
    JSONArray suggestions;

    public AutoCompleteApi(Context context) {
        this.context = context;
    }

    public void setCallback(autoCompleteApiCallback callback){
        this.callback = callback;
    }

    public JSONArray getSuggestions(String query) throws JSONException {


//        String url ="https://sunny-day-cycling.wl.r.appspot.com/api/auto?input=" + query;

        String url = String.format("https://sunny-day-cycling.wl.r.appspot.com/api/auto?input=%s",
                query);


        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
//                            json = new JSONObject(response.toString());
                            suggestions = response;
                            callback.onAutoSuccess(suggestions);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        VolleySingleton.getInstance(context).addToRequestQueue(request);

        return this.suggestions;
    }

    public interface autoCompleteApiCallback{
        void onAutoSuccess(JSONArray suggestions) throws JSONException;
    }
}


