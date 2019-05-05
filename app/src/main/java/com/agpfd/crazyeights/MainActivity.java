package com.agpfd.crazyeights;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
// Imports for window features (fullscreen)
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    // Called when activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TitleView tView = new TitleView(this);

        // Make sure screen doesn't timeout/blacken
        tView.setKeepScreenOn(true);

        // Do not show title while app is open
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Make game take full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(tView);
    }


}
