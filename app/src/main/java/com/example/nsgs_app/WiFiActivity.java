package com.example.nsgs_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WiFiActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NetworkAdapter networkAdapter;
    private List<Network> networkList;
    private List<Network> filteredNetworkList; // List for filtered networks
    private List<SystemStats> systemStats;
    private TextView cpuTempTextView, cpuTimeTextView, scanningStatusTextView;
    private TextView totalNetworksTextView;
    private Handler handler;
    private Runnable fetchTask;
    private final int FETCH_INTERVAL_SECONDS = 10; // Duration between HTTP requests
    private final int FETCH_INTERVAL = FETCH_INTERVAL_SECONDS * 1000; // DO NOT CHANGE
    private Comparator<Network> currentComparator; // Save the current comparator
    private String currentFilter; // Save the current filter
    private boolean isFilteringMode = false; // Track whether filtering mode is active

    private static final String PREFS_NAME = "WiFiActivityPrefs"; // USED TO SAVE POS IN SHARED PREFFFF
    private static final String SCROLL_POSITION_KEY = "scroll_position";
    private static final String SCROLL_OFFSET_KEY = "scroll_offset";

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

        handler = new Handler();
        fetchTask = new Runnable() {
            @Override
            public void run() {
                saveScrollPosition(); // Save the scroll position before fetching data
                fetchNetworks();
                fetchSystemStats();
                handler.postDelayed(this, FETCH_INTERVAL);
            }
        };

        fetchNetworks(); // Initial fetch on create
        fetchSystemStats();
        handler.postDelayed(fetchTask, FETCH_INTERVAL); // Schedule fetch every interval
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
        // Check if there is a filter state passed back from NetworkDetailActivity
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("currentFilter")) {
            currentFilter = intent.getStringExtra("currentFilter");
            isFilteringMode = currentFilter != null;
        }
        applyCurrentSortOrFilter();

        /*else {
            // Apply the current sort or filter if no filter state is passed back
            applyCurrentSortOrFilter();
        }*/

        recyclerView.setAdapter(networkAdapter);

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
                        networkList  = gson.fromJson(jsonObject.getJSONArray("networks").toString(), networkListType);

                        runOnUiThread(() -> {

                            // Update the total networks count (Top Page)
                            totalNetworksTextView.setText(getString(R.string.total_networks_label, networkList.size()));

                            // Apply the current sort or filter
                            applyCurrentSortOrFilter();
                            recyclerView.setAdapter(networkAdapter);

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
                runOnUiThread(() -> Toast.makeText(WiFiActivity.this, "Error fetching system stats", Toast.LENGTH_SHORT).show()); // @TODO STRINGS.XML
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
                                cpuTempTextView.setText(getString(R.string.cpu_temperature) + stats.getTemperature("Celsius")); // @TODO replace with the actual settings
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_wifi, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {// Back button
            navigateToMainActivity();
            return true;
        } else if (itemId == R.id.sort_by_ssid) {// Sort by SSID
            isFilteringMode = false;
            sortNetworkList(Comparator.comparing(Network::getSsid, (ssid1, ssid2) -> {
                boolean ssid1HasNumbers = ssid1.matches(".*\\d.*");
                boolean ssid2HasNumbers = ssid2.matches(".*\\d.*");
                if (ssid1HasNumbers && !ssid2HasNumbers) return 1;
                if (!ssid1HasNumbers && ssid2HasNumbers) return -1;
                return ssid1.compareToIgnoreCase(ssid2);
            }));
            //sortNetworkList(Comparator.comparing(Network::getSsid, String::compareToIgnoreCase));
            return true;
        } else if (itemId == R.id.sort_by_security) {// Sort by Security Protocol
            isFilteringMode = false;
            sortNetworkList(Comparator.comparing(Network::getSecurity, String::compareToIgnoreCase));
            return true;
        } else if (itemId == R.id.action_filter_wep) { // Filter by WEP
            isFilteringMode = true;
            filterNetworkList("WEP");
            return true;
        } else if (itemId == R.id.action_filter_wpa) { // Filter by WPA
            isFilteringMode = true;
            filterNetworkList("WPA");
            return true;
        } else if (itemId == R.id.action_filter_wpa2) { // Filter by WPA2
            isFilteringMode = true;
            filterNetworkList("WPA2");
            return true;
        } else if (itemId == R.id.action_filter_wpa3) { // Filter by WPA3
            isFilteringMode = true;
            filterNetworkList("WPA3");
            return true;
        }
        else if (itemId == R.id.action_filter_unprotected) { // Filter by WPA3
            isFilteringMode = true;
            filterNetworkList("unprotected");
            return true;
        }
        else if (itemId == R.id.action_default_view) { // Default View
            resetFiltersAndSort();
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
        currentComparator = comparator;
        if (networkList != null) {
            filteredNetworkList = new ArrayList<>(networkList);
            filteredNetworkList.sort(comparator);
            updateAdapter(filteredNetworkList);
        }
    }

    private void filterNetworkList(String securityType) {
        currentFilter = securityType;
        if (networkList != null) {
            if (securityType == null) {
                filteredNetworkList = new ArrayList<>(networkList); // No filter, show all networks
            } else {
                filteredNetworkList = networkList.stream()
                        .filter(network -> network.getSecurity().equalsIgnoreCase(securityType))
                        .collect(Collectors.toList());
            }
            updateAdapter(filteredNetworkList);
        }
    }

    private void applyCurrentSortOrFilter() {
        if (networkList == null) {
            return;
        }

        if (isFilteringMode && currentFilter != null) {
            // Apply the current filter
            filteredNetworkList = networkList.stream()
                    .filter(network -> network.getSecurity().equalsIgnoreCase(currentFilter))
                    .collect(Collectors.toList());
        } else if (!isFilteringMode && currentComparator != null) {
            // Apply the current sort
            filteredNetworkList = new ArrayList<>(networkList);
            filteredNetworkList.sort(currentComparator);
        } else {
            filteredNetworkList = new ArrayList<>(networkList);
        }

        updateAdapter(filteredNetworkList);
    }

    //Reset to default view

    private void resetFiltersAndSort() {
        isFilteringMode = false;
        currentFilter = null;
        currentComparator = null;
        applyCurrentSortOrFilter();
    }

    @SuppressLint("StringFormatMatches")
    private void updateAdapter(List<Network> networkList) {
        // Update the total networks count (Top Page)
        totalNetworksTextView.setText(getString(R.string.total_networks_label, networkList.size()));

        if (networkAdapter == null) {
            networkAdapter = new NetworkAdapter(WiFiActivity.this, networkList, currentFilter);
            recyclerView.setAdapter(networkAdapter);
        } else {
            networkAdapter.updateNetworkList(networkList);
            networkAdapter.notifyDataSetChanged();
        }
    }

    private void saveScrollPosition() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            int scrollPosition = layoutManager.findFirstVisibleItemPosition();
            int scrollOffset = 0;
            if (scrollPosition != RecyclerView.NO_POSITION) {
                scrollOffset = layoutManager.findViewByPosition(scrollPosition).getTop();
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
}
