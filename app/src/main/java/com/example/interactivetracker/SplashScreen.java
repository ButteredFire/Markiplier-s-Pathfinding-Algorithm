package com.example.interactivetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashScreen extends AppCompatActivity {
    public final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        handler.postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), APIKeyRequest.class);
            startActivity(intent);

            finish();
        }, 3000);
    }
}