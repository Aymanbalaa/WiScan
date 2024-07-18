package com.example.nsgs_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WiFiActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

    private RecyclerView recyclerView;
    private NetworkAdapter networkAdapter;
    private List<Network> networkList = new ArrayList<>();
    private List<Network> filteredNetworkList = new ArrayList<>();
    private List<SystemStats> systemStats;
    private TextView cpuTempTextView, cpuTimeTextView, scanningStatusTextView;
    private TextView totalNetworksTextView;
    private Handler handler;
    private Runnable fetchTask;
    private int fetchInterval; // This variable will hold the fetch interval in milliseconds
    private Comparator<Network> currentComparator; // Save the current comparator
    private Button btnExportCsv;
    private String currentQuery = ""; // This will hold the current search query

    private static final String PREFS_NAME = "WiFiActivityPrefs"; // USED TO SAVE POS IN SHARED PREFFFF
    private static final String SCROLL_POSITION_KEY = "scroll_position";
    private static final String SCROLL_OFFSET_KEY = "scroll_offset";
    private static final String NETWORK_LIST_KEY = "network_list";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        totalNetworksTextView = findViewById(R.id.totalNetworksTextView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cpuTempTextView = findViewById(R.id.cpuTempTextView);
        cpuTimeTextView = findViewById(R.id.cpuTimeTextView);
        scanningStatusTextView = findViewById(R.id.scanningStatusTextView);
        btnExportCsv = findViewById(R.id.btn_export_csv);

        handler = new Handler();

        // Retrieve the fetch interval from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
        String fetchIntervalString = preferences.getString("fetch_unit", "10");
        int fetchIntervalSeconds = Integer.parseInt(fetchIntervalString);
        fetchInterval = fetchIntervalSeconds * 1000; // Convert to milliseconds

        fetchTask = new Runnable() {
            @Override
            public void run() {
                saveScrollPosition(); // Save the scroll position before fetching data
                fetchNetworks();
                fetchSystemStats();
                handler.postDelayed(this, fetchInterval);
            }
        };

        fetchNetworks(); // Initial fetch on create
        fetchSystemStats();

        handler.postDelayed(fetchTask, fetchInterval); // Schedule fetch every interval

        // Check for write permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }

        btnExportCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportToCsv();
            }
        });

        // Setup SearchView
        SearchView searchView = findViewById(R.id.search_ssid);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                filterNetworks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                filterNetworks(newText);
                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save the current scroll position and offset
        saveScrollPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restore the scroll position and offset
        restoreScrollPosition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchTask);
    }

    private void fetchNetworks() {
        OkHttpClient client = new OkHttpClient();

        // DO NOT CHANGE
        // 10.0.2.2:5000 is to be used if the emulator and server are running on the same device
        // otherwise use the endpoint of the server
        //String url = "http://10.0.2.2:5000/get_all_networks";
        String url = "http://217.15.171.225:5000/get_all_networks";
        //String url = "http://nsgs-proxy-server.online:5000/get_all_networks";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("WiFiActivity", "Error fetching data: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(WiFiActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
            }

            @SuppressLint("StringFormatMatches")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        // Fetch JSON
                        JSONObject jsonObject = new JSONObject(responseData);
                        Gson gson = new Gson();

                        // Filtering JSON data and feeding it into List of Networks
                        Type networkListType = new TypeToken<List<Network>>() {}.getType();
                        networkList = gson.fromJson(jsonObject.getJSONArray("networks").toString(), networkListType);

                        // Save the network list to shared preferences
                        saveNetworkList(networkList);

                        runOnUiThread(() -> {
                            // Update the total networks count (Top Page)
                            totalNetworksTextView.setText(getString(R.string.total_networks_label, networkList.size()));

                            // Sort the network list if a comparator is set
                            if (currentComparator != null) {
                                networkList.sort(currentComparator);
                            }

                            // Update filtered network list
                            filteredNetworkList.clear();
                            filteredNetworkList.addAll(networkList);

                            // Apply current search query to the updated list
                            filterNetworks(currentQuery);

                            // linking recycler view from xml to java
                            if (networkAdapter == null) {
                                networkAdapter = new NetworkAdapter(WiFiActivity.this, filteredNetworkList);
                                recyclerView.setAdapter(networkAdapter);
                            } else {
                                networkAdapter.notifyDataSetChanged();
                            }

                            // Restore the scroll position and offset
                            restoreScrollPosition();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(WiFiActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(WiFiActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void saveNetworkList(List<Network> networkList) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String networkListJson = gson.toJson(networkList);
        editor.putString(NETWORK_LIST_KEY, networkListJson);
        editor.apply();
    }

    private void fetchSystemStats() {
        OkHttpClient client2 = new OkHttpClient();

        // DO NOT CHANGE
        // 10.0.2.2:5000 is to be used if the emulator and server are running on the same device
        // otherwise use the endpoint of the server
        String url = "http://217.15.171.225:5000/get_system_stats";

        Request request2 = new Request.Builder()
                .url(url)
                .build();

        client2.newCall(request2).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("WiFiActivity", "Error fetching system stats: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(WiFiActivity.this, "Error fetching system stats", Toast.LENGTH_SHORT).show());
            }

            @SuppressLint({"StringFormatMatches", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("WiFiActivity", "System Stats Response: " + responseData); // Log the response data
                    try {
                        // Fetch JSON
                        JSONObject jsonObject = new JSONObject(responseData);
                        Gson gson = new Gson();

                        // Filtering JSON data and feeding it into List of Networks
                        Type systemStatsType = new TypeToken<List<SystemStats>>() {}.getType();
                        systemStats = gson.fromJson(jsonObject.getJSONArray("system_stats").toString(), systemStatsType);

                        runOnUiThread(() -> {
                            if (systemStats != null && !systemStats.isEmpty()) {
                                SystemStats stats = systemStats.get(0);

                                // Get the temperature unit from SharedPreferences
                                SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
                                String temperatureUnit = preferences.getString("temperature_unit", "Celsius");

                                cpuTempTextView.setText(getString(R.string.cpuTemperature) + stats.getTemperature(temperatureUnit));
                                cpuTimeTextView.setText(getString(R.string.cpu_time) + stats.getTime());
                                scanningStatusTextView.setText(getString(R.string.scanning_status) + (stats.getStatus() == 1 ? getString(R.string.active) : getString(R.string.inactive)));
                            }
                        });
                    } catch (Exception e) {
                        Log.e("WiFiActivity", "Error parsing system stats JSON: " + e.getMessage(), e);
                        runOnUiThread(() -> Toast.makeText(WiFiActivity.this, "Error parsing system stats", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("WiFiActivity", "Unsuccessful response for system stats: " + response.code());
                    runOnUiThread(() -> Toast.makeText(WiFiActivity.this, "Error fetching system stats", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void exportToCsv() {
        File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File csvFile = new File(downloadFolder, "networks.csv");

        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.append("SSID,BSSID,Security,Longitude,Latitude,Neighborhood,Postal Code\n");
            for (Network network : networkList) {
                writer.append(network.getSsid())
                        .append(',')
                        .append(network.getBssid())
                        .append(',')
                        .append(network.getSecurity())
                        .append(',')
                        .append(String.valueOf(network.getCoordinates()))
                        .append(',')
                        .append(String.valueOf(network.getNeighborhood()))
                        .append(',')
                        .append(String.valueOf(network.getPostalCode()))
                        .append('\n');
            }
            Toast.makeText(this, "CSV file exported to Downloads folder", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to export CSV file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with exporting
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_wifi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {// Back button
            navigateToMainActivity();
            return true;
        } else if (itemId == R.id.sort_by_ssid) {// Sort by SSID
            sortNetworkList(Comparator.comparing(Network::getSsid, String::compareToIgnoreCase));
            return true;
        } else if (itemId == R.id.sort_by_security) {// Sort by Security Protocol
            sortNetworkList(Comparator.comparing(Network::getSecurity, String::compareToIgnoreCase));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void sortNetworkList(Comparator<Network> comparator) {
        if (networkList != null) {
            // Save the current scroll position and offset
            saveScrollPosition();

            networkList.sort(comparator);
            filteredNetworkList.clear();
            filteredNetworkList.addAll(networkList);
            networkAdapter.notifyDataSetChanged();

            // Restore the scroll position and offset
            restoreScrollPosition();
        }
        currentComparator = comparator; // Save the current comparator
    }

    private void saveScrollPosition() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            int scrollPosition = layoutManager.findFirstVisibleItemPosition();
            int scrollOffset = 0;
            if (scrollPosition != RecyclerView.NO_POSITION) {
                View firstVisibleView = layoutManager.findViewByPosition(scrollPosition);
                if (firstVisibleView != null) {
                    scrollOffset = firstVisibleView.getTop();
                }
            }
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(SCROLL_POSITION_KEY, scrollPosition);
            editor.putInt(SCROLL_OFFSET_KEY, scrollOffset);
            editor.apply();
        }
    }

    private void restoreScrollPosition() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int scrollPosition = preferences.getInt(SCROLL_POSITION_KEY, RecyclerView.NO_POSITION);
        int scrollOffset = preferences.getInt(SCROLL_OFFSET_KEY, 0);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null && scrollPosition != RecyclerView.NO_POSITION) {
            layoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffset);
        }
    }

    private void filterNetworks(String query) {
        filteredNetworkList.clear();
        if (TextUtils.isEmpty(query)) {
            filteredNetworkList.addAll(networkList);
        } else {
            for (Network network : networkList) {
                if (network.getSsid().toLowerCase().contains(query.toLowerCase())) {
                    filteredNetworkList.add(network);
                }
            }
        }
        if (networkAdapter != null) {
            networkAdapter.notifyDataSetChanged();
        }
    }
}
