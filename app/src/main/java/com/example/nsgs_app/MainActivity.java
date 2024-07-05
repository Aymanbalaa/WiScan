package com.example.nsgs_app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.Collections;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NetworkAdapter networkAdapter;
    private List<Network> networkList;
    private TextView totalNetworksTextView;
    protected NetworkAdapterFilteringBySSID networkAdapterFilteringBySSID;
    protected NetworkAdapterFiteringByProtocol networkRecyclerViewByProtocolAdapter;
    private Handler handler;
    private Runnable fetchTask;
    private final int FETCH_INTERVAL_SECONDS = 10; // Duration between HTTP requests
    private final int FETCH_INTERVAL = FETCH_INTERVAL_SECONDS * 1000; // DO NOT CHANGE
    List<Network> networksList; //for testing sorting and filtering functions
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toobar setting
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        //Testing sorting and filtering
        // Testing-generating list of networks
        networksList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            networksList.add(new Network(i, "BELL_"+i, "bssid"+i, "H3SE9", "WEP"+i, "Concordia"+i, "548621", "BELL"+i));
        }


        sortBySSID(networksList);

    }
    // Setting up Actions item on toolbar in Main

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu item on action bar
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item ) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_sort_SSID) {
            sortBySSID(networksList);
            return true;
        } else if (id == R.id.action_sort_protocol) {
            sortByProtocol(networksList);
            return true;
        } else if (id == R.id.action_filter_SSID) {
            filterBySSID();

        }else {
            filterByProtocol();
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchTask);
    }

    private void fetchNetworks() {
        OkHttpClient client = new OkHttpClient();

        //DO NOT CHANGE
        //10.0.2.2:5000 is to be used if the emulator and server are running on same device??
        //otherwise use the endpoint of server
        String url = "http://10.0.2.2:5000/get_all_networks";
        //String url = "https://0040-192-226-194-155.ngrok-free.app/get_all_networks";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "Error fetching data: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
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

                        // Sort the list by SSID
                        networkList.sort(new Comparator<Network>() {
                            @Override
                            public int compare(Network n1, Network n2) {
                                return n1.getSsid().compareToIgnoreCase(n2.getSsid());
                            }

                        });

                        runOnUiThread(() -> {
                            // Update the total networks count (Top Page)
                            totalNetworksTextView.setText(getString(R.string.total_networks_label, networkList.size()));

                            // linking recycler view from xml to java
                            networkAdapter = new NetworkAdapter(MainActivity.this, networkList);
                            recyclerView.setAdapter(networkAdapter);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    @SuppressLint("StringFormatMatches")
    void sortBySSID(List<Network>networks){
        //Sorting by SSIDs
        //Collections.sort(networkList, Comparator.comparing(Network::getBssid));
        Collections.sort(networks, Comparator.comparing(Network::getBssid));

        runOnUiThread(() -> {
            // Update the total networks count (Top Page)
            totalNetworksTextView.setText(getString(R.string.total_networks_label, networks.size()));

            // linking recycler view from xml to java
            recyclerView.setAdapter(networkAdapter);
            networkAdapter = new NetworkAdapter(MainActivity.this, networks);
        });

    }

    @SuppressLint("StringFormatMatches")
    void sortByProtocol (List<Network>networks){
        //Sorting by Security Protocols
        Collections.sort(networks, Comparator.comparing(Network::getBssid));

        runOnUiThread(() -> {
            // Update the total networks count (Top Page)
            totalNetworksTextView.setText(getString(R.string.total_networks_label, networks.size()));

            // linking recycler view from xml to java
            networkAdapter = new NetworkAdapter(MainActivity.this, networks);
            recyclerView.setAdapter(networkAdapter);
        });

    }

    @SuppressLint("StringFormatMatches")
    void filterBySSID(){

        // Testing-generating list of networks
        List<Network> networksList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            networksList.add(new Network(i, "Network" + i, "Wep" + i));
        }

        //Sorting by Security Protocol using Collections Library
        Collections.sort(networksList, Comparator.comparing(Network::getSsid));
        runOnUiThread(() -> {
            //runOnUiThread method is used to update the UI with the fetched data.
            // This ensures that database operations do not block the main thread, preventing the UI from freezing.

            // Update the total networks count (Top Page)
            totalNetworksTextView.setText(getString(R.string.total_networks_label, networksList.size()));

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this); //creates a linear layout manager for recycler view
            recyclerView = findViewById(R.id.recyclerView);
            networkAdapterFilteringBySSID = new NetworkAdapterFilteringBySSID (networksList); //binds networks with adapter so networks can be set to adapted view

            recyclerView.setLayoutManager(linearLayoutManager); //set linear layout manager to recycler view layout
            recyclerView.setAdapter(networkAdapterFilteringBySSID); //integrate adapter in recycler view layout
    });


    }

    @SuppressLint("StringFormatMatches")
    void filterByProtocol(){

        // Testing-generating list of networks
        List<Network> networksList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            networksList.add(new Network(i, "Network" + i, "Wep" + i));
        }

        //Sorting by Security Protocol using Collections Library
        Collections.sort(networksList, Comparator.comparing(Network::getSsid));
        runOnUiThread(() -> {
            //runOnUiThread method is used to update the UI with the fetched data.
            // This ensures that database operations do not block the main thread, preventing the UI from freezing.
            totalNetworksTextView.setText(getString(R.string.total_networks_label, networksList.size()));

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this); //creates a linear layout manager for recycler view
            recyclerView = findViewById(R.id.recyclerView);
            networkRecyclerViewByProtocolAdapter = new NetworkAdapterFiteringByProtocol(networksList); //binds networks with adapter so networks can be set to adapted view

            recyclerView.setLayoutManager(linearLayoutManager); //set linear layout manager to recycler view layout
            recyclerView.setAdapter(networkRecyclerViewByProtocolAdapter); //integrate adapter in recycler view layout

        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}