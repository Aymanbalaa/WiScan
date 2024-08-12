package com.example.nsgs_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView cpuTempTextView, cpuTimeTextView, scanningStatusTextView;
    private List<SystemStats> systemStats;
    private static boolean disclaimerShown = false;
    private AlertDialog discText;
    private boolean isListOpened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Language.setLanguage(this, Language.getLanguage(this));
        setContentView(R.layout.activity_main);
        String currentTheme = ThemeSelection.themeInitializer(findViewById(R.id.main), this,this);

// theme setup + action bar
        getSupportActionBar().setTitle(getString(R.string.home_bar_title));
        switch(currentTheme) {

            case "Light":
            case "Clair":
            case "Свет":
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        }

        isListOpened = true;

        Button wifiButton = findViewById(R.id.wifi_button);
        Button locationButton = findViewById(R.id.location_button);
        Button activeButton = findViewById(R.id.active_button);
        Button statsButton = findViewById(R.id.stats_button);

        setButtonColor(currentTheme, wifiButton, locationButton, activeButton, statsButton);

        if (!disclaimerShown) {
            showDisclaimer(); // starts the disclaimer
            disclaimerShown = true;
        }


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
        statsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatsActivity.class);
            startActivity(intent);
        });

        cpuTempTextView = findViewById(R.id.cpuTempTextView);
        cpuTimeTextView = findViewById(R.id.cpuTimeTextView);
        scanningStatusTextView = findViewById(R.id.scanningStatusTextView);

        fetchSystemStats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isListOpened = false;
        if (discText != null && discText.isShowing()) {
            discText.dismiss();
        }
    }

    public void fetchSystemStats() {
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

                        // Filtering JSON data
                        Type systemStatsType = new TypeToken<List<SystemStats>>() {}.getType();


                        // systen_stats is tabe name
                        systemStats = gson.fromJson(jsonObject.getJSONArray("system_stats").toString(), systemStatsType);

                        runOnUiThread(() -> {
                            if (systemStats != null && !systemStats.isEmpty()) {
                                SystemStats stats = systemStats.get(0);

                                // Get the temperature unit from SharedPreferences
                                SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
                                String temperatureUnit = preferences.getString("temperature_unit", "Celsius");

                                cpuTempTextView.setText(getString(R.string.cpuTemperature)+ " " + stats.getTemperature(temperatureUnit));
                                cpuTimeTextView.setText(getString(R.string.cpu_time)+ " " + stats.getTime());
                                scanningStatusTextView.setText(getString(R.string.scanning_status) + " " + (stats.getStatus() == 1 ? getString(R.string.active) : getString(R.string.inactive)));
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
// too bar for settings help and additional info
        if (id == R.id.help) {
            Intent helpIntent = new Intent(this, HelpActivity.class);
            startActivity(helpIntent);
            return true;
        } else if (id == R.id.settings) {
            showSettingsDialog();
            return true;
        }
        else if (id ==R.id.additional_info)
        {
            Intent infoIntent = new Intent(this, InfoActivity.class);
            startActivity(infoIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {
        SettingsDialogFragment settingsDialogFragment = new SettingsDialogFragment();
        settingsDialogFragment.show(getSupportFragmentManager(), "SettingsDialogFragment");
    }

    private void showDisclaimer() {
        if (!isListOpened) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") LinearLayout disclaimerLayout = (LinearLayout) inflater.inflate(R.layout.dialog_disclaimer, null);

        // Adding language change button and functionality
        ImageButton languageButton = disclaimerLayout.findViewById(R.id.language_button);
        languageButton.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
            String currentLanguage = preferences.getString("language", "en");

            String newLanguage;
            switch (currentLanguage) {
                case "en":
                    newLanguage = "fr";
                    break;
                case "fr":
                    newLanguage = "ru";
                    break;
                default:
                    newLanguage = "en";
            }

            Language.setLanguage(this, newLanguage);
            Language.saveLanguage(this, newLanguage);

            // disclaimer text and xml setups
            TextView textDisc = disclaimerLayout.findViewById(R.id.disclaimer_text);
            textDisc.setText(R.string.disclaimer_text);
            TextView disclaimerTitle = disclaimerLayout.findViewById(R.id.disclaimer_title);
            disclaimerTitle.setText(R.string.disclaimer_title);
            Button acceptButton = disclaimerLayout.findViewById(R.id.accept_button);
            Button declineButton = disclaimerLayout.findViewById(R.id.decline_button);
            acceptButton.setText(R.string.accept);
            declineButton.setText(R.string.decline);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(disclaimerLayout);

        discText = builder.create();

        Button acceptButton = disclaimerLayout.findViewById(R.id.accept_button);
        Button declineButton = disclaimerLayout.findViewById(R.id.decline_button);

        acceptButton.setOnClickListener(v -> {
            discText.dismiss();
        });

        declineButton.setOnClickListener(v -> {
            discText.dismiss();
            System.exit(0);
        });

        discText.setCancelable(false);
        discText.show();
    }

    private void setButtonColor(String theme, Button wifiButton, Button locationButton, Button activeButton, Button infoButton) {
        // button color and theme
        // shoud be switched to themes.xml
        switch (theme) {
            case "Dark":
            case "Sombre":
            case "Темный":
                wifiButton.setBackgroundColor(Color.parseColor("#153448"));
                locationButton.setBackgroundColor(Color.parseColor("#3C5B6F"));
                activeButton.setBackgroundColor(Color.parseColor("#948979"));
                infoButton.setBackgroundColor(Color.parseColor("#DFD0B8"));

                break;

            default:
                wifiButton.setBackgroundColor(Color.parseColor("#4535C1"));
                locationButton.setBackgroundColor(Color.parseColor("#478CCF"));
                activeButton.setBackgroundColor(Color.parseColor("#36C2CE"));
                infoButton.setBackgroundColor(Color.parseColor("#77E4C8"));
                break;
        }
    }
}
