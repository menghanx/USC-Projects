package com.example.weatherapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingScreen {
    private Activity activity;
    private AlertDialog dialog;

    LoadingScreen(Activity activity){
        this.activity = activity;
    }

    void startLoadingScreen(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
        LayoutInflater inflater = this.activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_screen, null));
        builder.setCancelable(true);

        this.dialog = builder.create();
        this.dialog.show();
    }

    void dismissLoadingScreen(){
        this.dialog.dismiss();
    }
}
