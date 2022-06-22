package com.example.myloverspace;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatActivity;



public abstract class BaseActivity extends AppCompatActivity {

    public final String TAG = "BaseActivity";
    public final String ACTION = "NIGHT_SWITCH";
    protected BroadcastReceiver receiver;
    protected IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setNightMode();
        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(R.attr.tvBackground, typedValue, true);
        //StatusBarCompat.setStatusBarColor(this, ContextCompat.getColor(this, typedValue.resourceId));

        filter = new IntentFilter();
        filter.addAction(ACTION);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                needRefresh();
            }
        };

        registerReceiver(receiver, filter);
    }

    public boolean isNightMode(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return sharedPreferences.getBoolean("nightMode", false);
    }
    public void setNightMode(){
        if(isNightMode()){
            this.setTheme(R.style.NightTheme);
        }
        else{
            this.setTheme(R.style.DayTheme);
        }
    }

    protected abstract void needRefresh();


    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
