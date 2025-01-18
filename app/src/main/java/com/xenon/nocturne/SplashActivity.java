package com.xenon.nocturne;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Set a delay for the splash screen, then move to MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start MainActivity after 2 seconds (2000 milliseconds)
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finish SplashActivity so it won't be in the back stack
            }
        }, 2000); // Delay in milliseconds (2 seconds)
    }
}
