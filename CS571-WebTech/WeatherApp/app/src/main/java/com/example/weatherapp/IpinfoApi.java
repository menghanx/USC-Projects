package com.example.weatherapp;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class IpinfoApi {
    JSONObject json;
    Context context;
    IpinfoApiCallback callback;

    public IpinfoApi(Context context) {
        this.context = context;
    }

    public void setCallback(IpinfoApiCallback callback){
        this.callback = callback;
    }

    public JSONObject getCurLocation(){
        String url ="https://ipinfo.io/json?token=15ee8a4671b9a0";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            json = new JSONObject(response.toString());
                            callback.onIpinfoSuccess(json);
//                            Toast.makeText(context, json.toString(), Toast.LENGTH_LONG).show();
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

        return json;
    }

    public interface IpinfoApiCallback{
        void onIpinfoSuccess(JSONObject json) throws JSONException;
    }
}
