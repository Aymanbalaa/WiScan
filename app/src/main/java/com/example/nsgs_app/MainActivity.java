package com.example.nsgs_app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button wifiButton = findViewById(R.id.wifi_button);
        Button locationButton = findViewById(R.id.location_button);

        wifiButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WiFiActivity.class);
            startActivity(intent);
        });

        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });
    }
}
