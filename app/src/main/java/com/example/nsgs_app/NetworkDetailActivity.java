// NetworkDetailActivity.java
package com.example.nsgs_app;

import static com.example.nsgs_app.NetworkProviderGuesser.getNetworkProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NetworkDetailActivity extends AppCompatActivity {
    private TextView ssid, bssid, security, coordinates, postalCode, neighborhood, provider;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_detail);

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
        ssid.setText(getString(R.string.ssid_label,intent.getStringExtra("ssid")));
        bssid.setText(getString(R.string.bssid_label,intent.getStringExtra("bssid")));
        security.setText(getString(R.string.security_label,intent.getStringExtra("security")));
        coordinates.setText(getString(R.string.coordinates_label,intent.getStringExtra("coordinates")));
        postalCode.setText(getString(R.string.postal_code_label,intent.getStringExtra("postalCode")));
        neighborhood.setText(getString(R.string.neighborhood_label,intent.getStringExtra("neighborhood")));
        provider.setText(getString(R.string.provider_label,getNetworkProvider(intent.getStringExtra("ssid"))));
    }

    // Static method to start this activity with network details
    public static void start(Context context, Network network) {
        Intent intent = new Intent(context, NetworkDetailActivity.class);
        intent.putExtra("ssid", network.getSsid());
        intent.putExtra("bssid", network.getBssid());
        intent.putExtra("security", network.getSecurity());
        intent.putExtra("coordinates", network.getCoordinates());
        intent.putExtra("postalCode", network.getPostalCode());
        intent.putExtra("neighborhood", network.getNeighborhood());
        context.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button press
            Intent intent = new Intent(this, WiFiActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
