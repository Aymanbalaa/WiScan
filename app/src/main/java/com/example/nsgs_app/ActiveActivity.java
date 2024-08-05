package com.example.nsgs_app;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActiveActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActiveNetworkAdapter activeNetworkAdapter;
    private List<ActiveNetwork> activeNetworkList = new ArrayList<>();
    private List<ActiveNetwork> filteredActiveNetworkList = new ArrayList<>();
    private Handler handler;
    private Runnable fetchTask;
    private int fetchInterval;
    private List<Network> networkList;

    private static final String PREFS_NAME = "ActiveActivityPrefs";
    private static final String SCROLL_POSITION_KEY = "scroll_position";
    private static final String SCROLL_OFFSET_KEY = "scroll_offset";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active);

        ViewGroup view = findViewById(R.id.active_networks_layout);
        String currentTheme = ThemeSelection.themeInitializer(view,this,this);

        getSupportActionBar().setTitle(getString(R.string.activity_networks_bar_title));
        switch(currentTheme) {
            case "Light":
            case "Clair":
            case "Свет":
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        }



        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }



        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch the network list from shared preferences
        SharedPreferences preferences = getSharedPreferences("WiFiActivityPrefs", MODE_PRIVATE);
        String networkListJson = preferences.getString("network_list", null);
        if (networkListJson != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Network>>() {}.getType();
            networkList = gson.fromJson(networkListJson, listType);
        }

        handler = new Handler();

        fetchInterval = 1000; // Convert to milliseconds

        fetchTask = new Runnable() {
            @Override
            public void run() {
                saveScrollPosition(); // Save the scroll position before fetching data
                fetchActiveNetworks();
                handler.postDelayed(this, fetchInterval);
            }
        };

        fetchActiveNetworks(); // Initial fetch on create

        handler.postDelayed(fetchTask, fetchInterval); // Schedule fetch every interval
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

    private void fetchActiveNetworks() {
        OkHttpClient client = new OkHttpClient();

        String url = "http://217.15.171.225:5000/get_all_active_networks";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ActiveActivity", "Error fetching data: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ActiveActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
            }

            @SuppressLint("StringFormatMatches")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        // Fetch JSON
                        JSONObject jsonObject = new JSONObject(responseData);
                        Gson gson = new Gson();

                        // Filtering JSON data and feeding it into List of ActiveNetworks
                        Type activeNetworkListType = new TypeToken<List<ActiveNetwork>>() {}.getType();
                        activeNetworkList = gson.fromJson(jsonObject.getJSONArray("activeScanNetworks").toString(), activeNetworkListType);
                        activeNetworkList.sort(Comparator.comparing(ActiveNetwork::getSignalStrength).reversed());

                        runOnUiThread(() -> {
                            // Update filtered active network list
                            filteredActiveNetworkList.clear();
                            filteredActiveNetworkList.addAll(activeNetworkList);

                            // linking recycler view from xml to java
                            if (activeNetworkAdapter == null) {
                                activeNetworkAdapter = new ActiveNetworkAdapter(ActiveActivity.this, filteredActiveNetworkList, networkList);
                                recyclerView.setAdapter(activeNetworkAdapter);
                            } else {
                                activeNetworkAdapter.notifyDataSetChanged();
                            }

                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(ActiveActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(ActiveActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
                }
            }
        });
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
