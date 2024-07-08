package com.example.nsgs_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WiFiActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NetworkAdapter networkAdapter;
    private List<Network> networkList;
    private TextView totalNetworksTextView;
    private Handler handler;
    private Runnable fetchTask;
    private final int FETCH_INTERVAL_SECONDS = 10; // Duration between HTTP requests
    private final int FETCH_INTERVAL = FETCH_INTERVAL_SECONDS * 1000; // DO NOT CHANGE
    private Comparator<Network> currentComparator; // Save the current comparator
    private int scrollPosition = 0; // Save the scroll position
    private int scrollOffset = 0; // Save the scroll offset

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

        handler = new Handler();
        fetchTask = new Runnable() {
            @Override
            public void run() {
                fetchNetworks();
                handler.postDelayed(this, FETCH_INTERVAL);
            }
        };

        fetchNetworks(); // Initial fetch on create
        handler.postDelayed(fetchTask, FETCH_INTERVAL); // Schedule fetch every interval
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

                        runOnUiThread(() -> {
                            // Save the current scroll position and offset
                            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                            if (layoutManager != null) {
                                scrollPosition = layoutManager.findFirstVisibleItemPosition();
                                if (scrollPosition != RecyclerView.NO_POSITION) {
                                    scrollOffset = layoutManager.findViewByPosition(scrollPosition).getTop();
                                }
                            }

                            // Update the total networks count (Top Page)
                            totalNetworksTextView.setText(getString(R.string.total_networks_label, networkList.size()));

                            // Sort the network list if a comparator is set
                            if (currentComparator != null) {
                                networkList.sort(currentComparator);
                            }

                            // linking recycler view from xml to java
                            networkAdapter = new NetworkAdapter(WiFiActivity.this, networkList);
                            recyclerView.setAdapter(networkAdapter);

                            // Restore the scroll position and offset
                            if (layoutManager != null && scrollPosition != RecyclerView.NO_POSITION) {
                                layoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffset);
                            }
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
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                scrollPosition = layoutManager.findFirstVisibleItemPosition();
                if (scrollPosition != RecyclerView.NO_POSITION) {
                    scrollOffset = layoutManager.findViewByPosition(scrollPosition).getTop();
                }
            }

            networkList.sort(comparator);
            networkAdapter.notifyDataSetChanged();

            // Restore the scroll position and offset
            if (layoutManager != null && scrollPosition != RecyclerView.NO_POSITION) {
                layoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffset);
            }
        }
        currentComparator = comparator; // Save the current comparator
    }
}
