package com.boozefinder.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

public class MainActivity extends SherlockActivity {

    private static final String TAG = "MainActivity";
    private static final String MAP_STATE = "MyMapActivityState";

    private TextView test_tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        
        Button mapButton = (Button) findViewById(R.id.main_menu_map_button);

        mapButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(MainActivity.this, MyMapActivity.class));
            }
        });
    }
    
    @Override
    public void onDestroy() {
        SharedPreferences state = getSharedPreferences(MAP_STATE, 0);
        state.edit().clear().commit();
        super.onDestroy();
    }
}
