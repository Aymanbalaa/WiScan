package com.example.nsgs_app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button guidanceButton = findViewById(R.id.guidance_button);
        Button wifiButton = findViewById(R.id.wifi_button);
        Button locationButton = findViewById(R.id.location_button);
        Button settingsButton = findViewById(R.id.settings_button);

        guidanceButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Not available yet", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(MainActivity.this, GuidanceActivity.class);
//            startActivity(intent);
        });

        wifiButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WiFiActivity.class);
            startActivity(intent);
        });

        locationButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Not available yet", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
//            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Not available yet", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
//            startActivity(intent);
        });
    }
}
