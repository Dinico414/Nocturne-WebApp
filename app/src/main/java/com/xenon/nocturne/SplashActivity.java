package com.xenon.nocturne;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        GifImageView gifImageView = findViewById(R.id.gif_background);
        try {
            // Load your GIF from resources
            GifDrawable gifDrawable = new GifDrawable(getResources(), R.drawable.inshot_20250220_173925074);

            // Set the GIF to play only once.
            // If you need it to loop more than once, set the desired count.
            gifDrawable.setLoopCount(1);

            // Add an animation listener to trigger when the GIF completes a loop.
            gifDrawable.addAnimationListener(loopNumber -> {
                // When the GIF completes its single loop, start MainActivity.
                // Wrapping in runOnUiThread ensures we’re on the UI thread.
                runOnUiThread(() -> {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            });

            // Set the GIF drawable to the ImageView
            gifImageView.setImageDrawable(gifDrawable);
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback: If the GIF fails to load, transition after a fixed delay.
            gifImageView.postDelayed(() -> {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 5000);
        }
    }
}
