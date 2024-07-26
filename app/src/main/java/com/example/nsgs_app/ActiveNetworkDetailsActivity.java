package com.example.nsgs_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ActiveNetworkDetailsActivity extends AppCompatActivity {
    private TextView ssid, bssid, security, coordinates, postalCode, neighborhood, provider;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_network_details);

        backgroundUI();
        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ssid = findViewById(R.id.ssid);
        bssid = findViewById(R.id.bssid);
        security = findViewById(R.id.security);
        coordinates = findViewById(R.id.coordinates);
        postalCode = findViewById(R.id.postalCode);
        neighborhood = findViewById(R.id.neighborhood);
        provider = findViewById(R.id.provider);

        // Retrieve the network details from the intent
        Intent intent = getIntent();
        ssid.setText(getString(R.string.ssid_label, intent.getStringExtra("ssid")));
        bssid.setText(getString(R.string.bssid_label, intent.getStringExtra("bssid")));
        security.setText(getString(R.string.security_label, intent.getStringExtra("security")));
        coordinates.setText(getString(R.string.coordinates_label, intent.getStringExtra("coordinates")));
        postalCode.setText(getString(R.string.postal_code_label, intent.getStringExtra("postalCode")));
        neighborhood.setText(getString(R.string.neighborhood_label, intent.getStringExtra("neighborhood")));
        provider.setText(getString(R.string.provider_label, NetworkProviderGuesser.getNetworkProvider(intent.getStringExtra("ssid"))));
    }


    private void backgroundUI() {

        View view = findViewById(R.id.active_network_details_layout);
        BackgroundUI.backgroundUI(this,view);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
