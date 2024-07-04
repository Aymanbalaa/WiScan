package com.example.nsgs_app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NetworkAdapter networkAdapter;
    private List<Network> networkList;
    private TextView totalNetworksTextView;
    private Handler handler;
    private Runnable fetchTask;
    private final int FETCH_INTERVAL_SECONDS = 10; // Duration between HTTP requests
    private final int FETCH_INTERVAL = FETCH_INTERVAL_SECONDS * 1000; // DO NOT CHANGE
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}