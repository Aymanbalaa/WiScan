package com.example.nsgs_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView cpuTempTextView, cpuTimeTextView, scanningStatusTextView;
    private List<SystemStats> systemStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Language.setLanguage(this, Language.getLanguage(this));
        setContentView(R.layout.activity_main);
        backgroundUI();
        Button wifiButton = findViewById(R.id.wifi_button);
        Button locationButton = findViewById(R.id.location_button);
        Button activeButton = findViewById(R.id.active_button);

        wifiButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WiFiActivity.class);
            startActivity(intent);
        });

        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        activeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ActiveActivity.class);
            startActivity(intent);
        });

        cpuTempTextView = findViewById(R.id.cpuTempTextView);
        cpuTimeTextView = findViewById(R.id.cpuTimeTextView);
        scanningStatusTextView = findViewById(R.id.scanningStatusTextView);

        fetchSystemStats();
    }

    private void backgroundUI() {

        ViewGroup view = findViewById(R.id.main);
        BackgroundUI.backgroundPage(view, this);

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
                Log.e("MainActivity", "Error fetching system stats: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching system stats", Toast.LENGTH_SHORT).show());
            }

            @SuppressLint({"StringFormatMatches", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("MainActivity", "System Stats Response: " + responseData); // Log the response data
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
                        Log.e("MainActivity", "Error parsing system stats JSON: " + e.getMessage(), e);
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error parsing system stats", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("MainActivity", "Unsuccessful response for system stats: " + response.code());
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching system stats", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.help) {
            Intent helpIntent = new Intent(this, HelpActivity.class);
            startActivity(helpIntent);
            return true;
        } else if (id == R.id.settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            settingsIntent.putExtra("isDialog", true);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
