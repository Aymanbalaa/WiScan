package com.example.nsgs_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nsgs_app.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private OkHttpClient client;
    private final String url = "http://217.15.171.225:5000/get_all_networks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        client = new OkHttpClient();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        enableMyLocation();
        fetchAndAddNetworkPins();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission granted");
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Log.d(TAG, "Location found: " + location.toString());
                                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                                mMap.addMarker(new MarkerOptions().position(myLocation).title("You are here"));
                            } else {
                                Log.d(TAG, "Location is null");
                            }
                        }
                    });
        } else {
            Log.d(TAG, "Requesting location permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted in requestPermissionsResult");
                enableMyLocation();
            } else {
                Log.d(TAG, "Location permission denied in requestPermissionsResult");
                Toast.makeText(this, "Location permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchAndAddNetworkPins() {
        Log.d(TAG, "Fetching network pins from " + url);
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to fetch network data", e);
                runOnUiThread(() -> Toast.makeText(MapsActivity.this, "Failed to fetch network data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Network data fetched successfully: " + responseData);
                    runOnUiThread(() -> addNetworkPins(responseData));
                } else {
                    Log.e(TAG, "Failed to fetch network data: " + response.message());
                    runOnUiThread(() -> Toast.makeText(MapsActivity.this, "Failed to fetch network data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void addNetworkPins(String responseData) {
        Log.d(TAG, "Adding network pins to map");
        Gson gson = new Gson();

        try {
            Type listType = new TypeToken<List<Network>>() {}.getType();
            List<Network> networkList = gson.fromJson(responseData, listType);

            for (Network network : networkList) {
                LatLng networkLocation = new LatLng(network.getLatitude(), network.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(networkLocation)
                        .title(network.getSsid())
                        .snippet("Security: " + network.getSecurityProtocol()));
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            Log.e(TAG, "Failed to parse network data", e);
            Toast.makeText(this, "Failed to parse network data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Home button clicked");
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Network class representing the data structure
    class Network {
        private String ssid;
        private double latitude;
        private double longitude;
        private String securityProtocol;

        public String getSsid() {
            return ssid;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getSecurityProtocol() {
            return securityProtocol;
        }
    }
}
