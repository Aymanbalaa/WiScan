package com.example.nsgs_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    private Spinner languageSpinner, temperatureSpinner, dbSpinner;
    private Button shutdownButton;
    private TextView statusTextView;

    private static final String SHUTDOWN_URL = "http://217.15.171.225:5000/request_shutdown";
    private static final String CMDS_URL = "http://217.15.171.225:5000/cmds";
    private Handler handler;
    private Runnable commandPoller;
    private static final int POLL_INTERVAL = 10000; // 10 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setupUI();

        languageSelector();
        measurementSelector();
        dbSelector();

        shutdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendShutdownRequest();
            }
        });

        handler = new Handler();
        commandPoller = new Runnable() {
            @Override
            public void run() {
                pollCommandState();
                handler.postDelayed(this, POLL_INTERVAL);
            }
        };
        handler.post(commandPoller);
    }

    private void languageSelector() {
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] langCode = getResources().getStringArray(R.array.languageCode);
                String selectedLanguage = langCode[i];
                String currentLanguage = getSharedPreferences("prefs", MODE_PRIVATE).getString("language", "en");

                if (!selectedLanguage.equals(currentLanguage)) {
                    Language.setLanguage(SettingsActivity.this, selectedLanguage);
                    Language.saveLanguage(SettingsActivity.this, selectedLanguage);
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No action needed
            }
        });

        String currentLanguage = getSharedPreferences("prefs", MODE_PRIVATE).getString("language", "en");
        String[] langCode = getResources().getStringArray(R.array.languageCode);

        for (int i = 0; i < langCode.length; i++) {
            if (langCode[i].equals(currentLanguage)) {
                languageSpinner.setSelection(i);
                break;
            }
        }
    }

    private void measurementSelector() {
        temperatureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedUnit = getResources().getStringArray(R.array.measurementsArray)[i];
                SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("temperature_unit", selectedUnit);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No action needed
            }
        });

        String currentUnit = getSharedPreferences("prefs", MODE_PRIVATE).getString("temperature_unit", "Celsius");
        String[] temperatureUnits = getResources().getStringArray(R.array.measurementsArray);

        for (int i = 0; i < temperatureUnits.length; i++) {
            if (temperatureUnits[i].equals(currentUnit)) {
                temperatureSpinner.setSelection(i);
                break;
            }
        }
    }

    private void dbSelector() {
        dbSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedUnit = getResources().getStringArray(R.array.databaseArray)[i];
                SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("fetch_unit", selectedUnit);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No action needed
            }
        });

        String currentUnit = getSharedPreferences("prefs", MODE_PRIVATE).getString("fetch_unit", "10");
        String[] dbUnits = getResources().getStringArray(R.array.databaseArray);

        for (int i = 0; i < dbUnits.length; i++) {
            if (dbUnits[i].equals(currentUnit)) {
                dbSpinner.setSelection(i);
                break;
            }
        }
    }

    private void sendShutdownRequest() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SHUTDOWN_URL)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> updateStatus("Failed to send shutdown request"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> updateStatus("Shutdown request sent successfully"));
                } else {
                    runOnUiThread(() -> updateStatus("Failed to send shutdown request"));
                }
            }
        });
    }

    private void pollCommandState() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(CMDS_URL)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> updateStatus("Failed to poll command state"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String shutdownState = jsonResponse.getString("cmd_shutdown");
                        runOnUiThread(() -> handleCommandState(shutdownState));
                    } catch (JSONException e) {
                        runOnUiThread(() -> updateStatus("Error parsing command state"));
                    }
                } else {
                    runOnUiThread(() -> updateStatus("Failed to poll command state"));
                }
            }
        });
    }

    private void handleCommandState(String state) {
        switch (state) {
            case "INACTIVE":
                shutdownButton.setEnabled(true);
                updateStatus("Shutdown command inactive");
                break;
            case "ACTIVE":
                updateStatus("Shutdown command active");
                break;
            case "ACK-ACTIVE":
                updateStatus("Shutdown acknowledged by Pi");
                break;
            case "TIMEOUT-ACTIVE":
                updateStatus("Shutdown request timed out");
                break;
            default:
                updateStatus("Unknown command state");
                break;
        }
    }

    private void updateStatus(String status) {
        statusTextView.setText(status);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupUI() {
        languageSpinner = findViewById(R.id.spinnerLanguage);
        temperatureSpinner = findViewById(R.id.spinnerMetric);
        dbSpinner = findViewById(R.id.spinnerDB);
        shutdownButton = findViewById(R.id.buttonShutdown);
        statusTextView = findViewById(R.id.statusTextView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(commandPoller);
    }
}
