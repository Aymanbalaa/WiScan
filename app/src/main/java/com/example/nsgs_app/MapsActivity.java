package com.example.nsgs_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.nsgs_app.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<Network> networkList = new ArrayList<>();
    private List<Network> triangulatedList = new ArrayList<>();
    private List<Network> finalNetworkList = new ArrayList<>();
    private Map<LatLng, List<Network>> locationNetworkMap = new HashMap<>();
    private Map<LatLng, Marker> currentMarkers = new HashMap<>();
    private Handler handler;
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 3000; // 10 seconds

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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        handler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshData();
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
    }

    private void refreshData() {
        fetchNetworkList();
        addSecurityToTriangulated();
        filterBothLists();
        groupNetworksByLocation();
        updateNetworkPins();
    }

    private void addSecurityToTriangulated() {
        Map<String, String> bssidToSecurityMap = new HashMap<>();
        for (Network network : networkList) {
            if (network != null && network.getBssid() != null) {
                bssidToSecurityMap.put(network.getBssid(), network.getSecurity());
            }
        }

        for (Network network : triangulatedList) {
            if (network != null && network.getBssid() != null) {
                String security = bssidToSecurityMap.get(network.getBssid());
                if (security != null) {
                    network.setSecurity(security);
                }
            }
        }
    }

    public void filterBothLists() {
        Set<String> bssidSet = new HashSet<>();
        finalNetworkList.clear();

        for (Network network : triangulatedList) {
            if (network != null && network.getBssid() != null) {
                finalNetworkList.add(network);
                bssidSet.add(network.getBssid());
            }
        }

        for (Network network : networkList) {
            if (network != null && network.getBssid() != null && !bssidSet.contains(network.getBssid())) {
                finalNetworkList.add(network);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        enableMyLocation();
        handler.post(refreshRunnable);
        setMarkerClickListener();
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
                                mMap.addMarker(new MarkerOptions().position(myLocation).title(getString(R.string.you_are_here)));
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
                Toast.makeText(this, R.string.location_permission_is_required_to_use_this_feature, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchNetworkList() {
        NetworkManager networkManager = NetworkManager.getInstance(this);
        String networksUrl = "http://217.15.171.225:5000/get_all_networks";
        String triangulatedUrl = "http://217.15.171.225:6000/filtered_triangulated";

        networkManager.fetchNetworks(networksUrl, false);
        networkManager.fetchNetworks(triangulatedUrl, true);
        networkManager.refreshLists();

        networkList = networkManager.getNetworkList();
        triangulatedList = networkManager.getTriangulatedList();
    }

    private void groupNetworksByLocation() {
        locationNetworkMap.clear();
        if (finalNetworkList != null) {
            for (Network network : finalNetworkList) {
                if (network != null) {
                    LatLng location = new LatLng(network.getLatitude(), network.getLongitude());
                    locationNetworkMap.computeIfAbsent(location, k -> new ArrayList<>()).add(network);
                }
            }
        }
    }

    private void updateNetworkPins() {
        Set<LatLng> locationsToRemove = new HashSet<>(currentMarkers.keySet());
        if (locationNetworkMap != null) {
            for (Map.Entry<LatLng, List<Network>> entry : locationNetworkMap.entrySet()) {
                LatLng location = entry.getKey();
                List<Network> networks = entry.getValue();
                String title;
                String snippet = null;
                if (networks.size() == 1) {
                    Network network = networks.get(0);
                    title = network.getSsid();
                    snippet = String.format(getString(R.string.security_label), network.getSecurity());
                } else {
                    title = getString(R.string.multiple_networks);
                }

                if (currentMarkers.containsKey(location)) {
                    Marker marker = currentMarkers.get(location);
                    marker.setTitle(title);
                    marker.setSnippet(snippet);
                    locationsToRemove.remove(location);
                } else {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(title)
                            .snippet(snippet));
                    currentMarkers.put(location, marker);
                }
            }
        }

        // Remove markers that are no longer present
        for (LatLng location : locationsToRemove) {
            Marker marker = currentMarkers.remove(location);
            if (marker != null) {
                marker.remove();
            }
        }
    }

    private void setMarkerClickListener() {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng position = marker.getPosition();
                List<Network> networks = locationNetworkMap.get(position);
                if (networks != null && networks.size() > 1) {
                    showNetworkListDialog(networks);
                }
                return false;
            }
        });
    }

    private void showNetworkListDialog(List<Network> networks) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.networks_at_this_location);

        ListView networkListView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        for (Network network : networks) {
            String securityLabel = String.format(getString(R.string.security_label), network.getSecurity());
            adapter.add(network.getSsid() + " (" + securityLabel + ")");
        }
        networkListView.setAdapter(adapter);

        builder.setView(networkListView);
        builder.setPositiveButton("OK", null);
        builder.show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refreshRunnable);
    }
}
