package com.example.nsgs_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SettingsDialogFragment extends DialogFragment {

    private Spinner languageSpinner, temperatureSpinner, dbSpinner;
    private Button shutdownButton;
    private TextView statusTextView;

    private static final String SHUTDOWN_URL = "http://217.15.171.225:5000/request_shutdown";
    private static final String CMDS_URL = "http://217.15.171.225:5000/cmds";
    private Handler handler;
    private Runnable commandPoller;
    private static final int POLL_INTERVAL = 1000; // 1 second

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUI(view);

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

    private void setupUI(View view) {
        languageSpinner = view.findViewById(R.id.spinnerLanguage);
        temperatureSpinner = view.findViewById(R.id.spinnerMetric);
        dbSpinner = view.findViewById(R.id.spinnerDB);
        shutdownButton = view.findViewById(R.id.buttonShutdown);
        statusTextView = view.findViewById(R.id.statusTextView);
    }

    private void languageSelector() {
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] langCode = getResources().getStringArray(R.array.languageCode);
                String selectedLanguage = langCode[i];
                String currentLanguage = getActivity().getSharedPreferences("prefs", getActivity().MODE_PRIVATE).getString("language", "en");

                if (!selectedLanguage.equals(currentLanguage)) {
                    Language.setLanguage(getActivity(), selectedLanguage);
                    Language.saveLanguage(getActivity(), selectedLanguage);

                    String toastMessage;
                    switch (selectedLanguage) {
                        case "fr":
                            toastMessage = "Language set to French";
                            break;
                        case "ru":
                            toastMessage = "Language set to Russian";
                            break;
                        default:
                            toastMessage = "Language set to English";
                    }
                    Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
                    getActivity().recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        String currentLanguage = getActivity().getSharedPreferences("prefs", getActivity().MODE_PRIVATE).getString("language", "en");
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
                SharedPreferences preferences = getActivity().getSharedPreferences("prefs", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("temperature_unit", selectedUnit);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        String currentUnit = getActivity().getSharedPreferences("prefs", getActivity().MODE_PRIVATE).getString("temperature_unit", "Celsius");
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
                SharedPreferences preferences = getActivity().getSharedPreferences("prefs", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("fetch_unit", selectedUnit);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        String currentUnit = getActivity().getSharedPreferences("prefs", getActivity().MODE_PRIVATE).getString("fetch_unit", "10");
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
                getActivity().runOnUiThread(() -> updateStatus("Failed to send shutdown request"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    getActivity().runOnUiThread(() -> updateStatus("Shutdown request sent successfully"));
                } else {
                    getActivity().runOnUiThread(() -> updateStatus("Failed to send shutdown request"));
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
                getActivity().runOnUiThread(() -> updateStatus("Failed to poll command state"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String shutdownState = jsonResponse.getString("cmd_shutdown");
                        getActivity().runOnUiThread(() -> handleCommandState(shutdownState));
                    } catch (JSONException e) {
                        getActivity().runOnUiThread(() -> updateStatus("Error parsing command state"));
                    }
                } else {
                    getActivity().runOnUiThread(() -> updateStatus("Failed to poll command state"));
                }
            }
        });
    }

    private void handleCommandState(String state) {
        switch (state) {
            case "INACTIVE":
                shutdownButton.setEnabled(true); // Default you can click now/ Safe
                updateStatus(getString(R.string.shutdown_command) + getString(R.string.is_currently_inactive_you_can_safely_click_the_button));
                break;
            case "ACTIVE":
                shutdownButton.setEnabled(false);
                updateStatus(getString(R.string.shutdown_command)+getString(R.string.in_progress)); // Don't want to click on button , shutdowennnnn in progress
                break;
            case "ACTIVE-ACK":
                shutdownButton.setEnabled(false);
                updateStatus(getString(R.string.shutdown_command)+getString(R.string.acknowledged_by_device_and_is_in_progress));
                break;
            case "TIMEOUT-ACTIVE":
                shutdownButton.setEnabled(false);
                updateStatus(getString(R.string.shutdown_command)+getString(R.string.timed_out));
                break;
            default:
                shutdownButton.setEnabled(false);
                updateStatus(getString(R.string.unknown) + getString(R.string.shutdown_command) +getString(R.string.state));
                break;
        }
    }

    private void updateStatus(String status) {
        statusTextView.setText(status);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(commandPoller);
    }
}
