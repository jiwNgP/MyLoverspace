package com.example.myloverspace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.widget.Toolbar;

public class UserSettingsActivity extends BaseActivity {

    final String TAG = "usersettings";
    private Switch nightMode;
    private SharedPreferences sharedPreferences;
    public Intent intent_back = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perference_layout);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Intent intent = getIntent();
        initView();

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myToolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setResult(RESULT_OK, intent_back);
                finish();
            }
        });

        //if(isNightMode()) myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_settings_white_24));
        //else myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_baseline_settings_24));
    }


    @Override
    protected void needRefresh(){
        Log.d(TAG, "needRefresh: Usersettings");
    }


    public void initView(){
        nightMode = findViewById(R.id.nightMode);
        nightMode.setChecked(sharedPreferences.getBoolean("nightMode",false));
        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                setNightModePref(isChecked);
                setSelfNightMode();

            }
        });
    }

    private void setNightModePref(boolean night){
        //通过 nightMode switch修改pref中的nightMode
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("nightMode",night);
        editor.commit();
    }

    private void setSelfNightMode(){
        //重新赋值并重启本acitivty
        super.setNightMode();
        Intent intent = new Intent(this, UserSettingsActivity.class);
        startActivity(intent);
        finish();
    }


    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            Intent intent = new Intent();
            intent.setAction("NIGHT_SWITCH");
            sendBroadcast(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
