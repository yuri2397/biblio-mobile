package com.univ.thies.bibliothque;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.univ.thies.bibliothque.views.Login;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_activity);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }, 1500);
    }
}