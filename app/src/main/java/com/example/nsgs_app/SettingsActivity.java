package com.example.nsgs_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends AppCompatActivity {

    private Spinner languageSpinner, temperatureSpinner, dbSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        backgroundUI();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setupUI();

        languageSelector();
        measurementSelector();
        dbSelector();
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
                    Toast.makeText(SettingsActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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

    private void backgroundUI() {

        View view = findViewById(R.id.settingsBackground);
        BackgroundUI.backgroundUI(this,view);
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

            }
        });

        String currentUnit = getSharedPreferences("prefs", MODE_PRIVATE).getString("fetch_unit", "10");
        String[] temperatureUnits = getResources().getStringArray(R.array.measurementsArray);

        for (int i = 0; i < temperatureUnits.length; i++) {
            if (temperatureUnits[i].equals(currentUnit)) {
                temperatureSpinner.setSelection(i);
                break;
            }
        }
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
    }
}
