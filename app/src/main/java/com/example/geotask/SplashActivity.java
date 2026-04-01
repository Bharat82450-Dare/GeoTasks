package com.example.geotask;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide the Action Bar (top bar) for full screen look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Wait for 3 seconds, then open MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Destroys the Splash screen so "Back" button doesn't come back here
            }
        }, 3000); // 3000 milliseconds = 3 seconds
    }
}