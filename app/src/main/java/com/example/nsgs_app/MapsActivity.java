package com.example.nsgs_app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.Objects;
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


    private static final int REFRESH_INTERVAL = 3000; // 3 seconds in miliseconds
    private double clusterRadius = 0.00000005; // default is lowww

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getSupportActionBar().setTitle(getString(R.string.map_activity_bar_title));

        // theme setupd
        // causes bugs?
        String currentTheme = ThemeSelection.themeInitializer(findViewById(R.id.map), this, this);
        switch(currentTheme) {
            case "Light":
            case "Clair":
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
                break;
            default:
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.black)));
                break;
        }

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // xoom in/out
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        handler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshData();

                handler.postDelayed(this, REFRESH_INTERVAL); // threads created
            }
        };
    }

    // all needed lists refresh
    private void refreshData() {
        fetchNetworkList();
        addSecurityToTriangulated();
        filterBothLists();
        groupNetworksByLocation();
        updateNetworkPins();
    }

    // trianmgulated networks doesnt have security so we add it heree
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

        for (Network network : networkList) {if (network != null && network.getBssid() != null && !bssidSet.contains(network.getBssid())) {
                finalNetworkList.add(network);
            }
        }
    }

    // google maps sdk help page copied from
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        enableMyLocation();
        handler.post(refreshRunnable);
        setMarkerClickListener();
    }

    // google maps sdk help page copied from
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

    // google maps sdk help page copied from
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

    // fetch botgh ists
    private void fetchNetworkList() {
        NetworkManager networkManager = NetworkManager.getInstance(this);
        String networksUrl = "http://217.15.171.225:5000/get_all_networks";
        String triangulatedUrl = "http://217.15.171.225:6000/filtered_triangulated"; // different endpoints!
        // maybe not use 217? dont want to use endpint

        networkManager.fetchNetworks(networksUrl, false,false);
        networkManager.fetchNetworks(triangulatedUrl, true,false);
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
                    boolean addedToCluster = false;

                    for (LatLng existingLocation : locationNetworkMap.keySet()) {
                        if (distanceBetween(existingLocation, location) < clusterRadius) {
                            locationNetworkMap.get(existingLocation).add(network);
                            addedToCluster = true;
                            break;
                        }
                    }

                    if (!addedToCluster) {
                        List<Network> networks = new ArrayList<>();
                        networks.add(network);
                        locationNetworkMap.put(location, networks);
                    }
                }
            }
        }
    }

    // cartesian math using maps coord
    private double distanceBetween(LatLng loc1, LatLng loc2) {
        double latDiff = loc1.latitude - loc2.latitude;
        double lngDiff = loc1.longitude - loc2.longitude;
        return Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
    }

    // dont remove pins but refresh them? tested enough to mke sure refresh wont ruin display
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
        builder.setPositiveButton(getString(R.string.ok_button), null);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cluster_radius_menu, menu);
        return true;
    }

    @Override
    // in toobaer we have optin with wrench
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_set_cluster_radius) {
            showClusterRadiusDialog();
            return true;
        } else if (itemId == android.R.id.home) {
            Log.d(TAG, "Home button clicked");
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showClusterRadiusDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setTitle(getString(R.string.choose_cluster));
        builder.setView(inflater.inflate(R.layout.dialog_cluster_radius, null));
        builder.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                AlertDialog alertDialog = (AlertDialog) dialog;
                RadioGroup radioGroup = alertDialog.findViewById(R.id.radioGroupClusterRadius);
                int selectedId = radioGroup.getCheckedRadioButtonId();

                if (selectedId == R.id.radius_very_small) {
                    clusterRadius = 0.00000005;
                    Toast.makeText(MapsActivity.this, R.string.set_cluster_very_small, Toast.LENGTH_SHORT).show();
                }
                else if (selectedId == R.id.radius_small) {
                    clusterRadius = 0.0001;
                    Toast.makeText(MapsActivity.this, getString(R.string.set_cluster_small), Toast.LENGTH_SHORT).show();
                } else if (selectedId == R.id.radius_medium) {
                    clusterRadius = 0.0005;
                    Toast.makeText(MapsActivity.this, getString(R.string.set_cluster_medium), Toast.LENGTH_SHORT).show();
                } else if (selectedId == R.id.radius_large) {
                    clusterRadius = 0.001;
                    Toast.makeText(MapsActivity.this, getString(R.string.set_cluster_large), Toast.LENGTH_SHORT).show();
                }
                refreshData();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog = (AlertDialog) dialog;
                RadioGroup checkBox = alertDialog.findViewById(R.id.radioGroupClusterRadius);
                if (clusterRadius == 0.00000005) {
                    checkBox.check(R.id.radius_very_small);
                }
                else if (clusterRadius == 0.0001) {
                    checkBox.check(R.id.radius_small);
                }
                else if (clusterRadius == 0.0005) {
                    checkBox.check(R.id.radius_medium);
                } else if (clusterRadius == 0.001) {
                    checkBox.check(R.id.radius_large);
                }
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refreshRunnable);
    }
}
