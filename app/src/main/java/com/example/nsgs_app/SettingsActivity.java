package com.example.nsgs_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    // NO HARDCODED STRINGS THAT WILL BE DISPLAY SHOULD BE HERE
    // EVERYTHING SHOULD BE IN STRINGS.XML


    private Spinner languageSpinner, temperatureSpinner, dbSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        setupUI();

        languageSelector();
        measurementSelector();

    }


    private void languageSelector() {

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] langCode = getResources().getStringArray(R.array.languageCode);

                String selectedLanguage = langCode[i];
                String currentLanguage = getSharedPreferences("prefs", MODE_PRIVATE).getString("language","en");


                if (!selectedLanguage.equals(currentLanguage)){
                    Language.setLanguage(SettingsActivity.this, selectedLanguage);
                    Language.saveLanguage(SettingsActivity.this, selectedLanguage);

                    if (selectedLanguage == "en") {
                        Toast.makeText(SettingsActivity.this, "Language set to English", Toast.LENGTH_SHORT).show();
                    } else if (selectedLanguage == "fr") {
                        Toast.makeText(SettingsActivity.this, "Language set to French", Toast.LENGTH_SHORT).show();
                    }

                    recreate();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        String currentLanguage = getSharedPreferences("prefs", MODE_PRIVATE).getString("language", "en");
        String [] langCode = getResources().getStringArray(R.array.languageCode);

        for (int i = 0; i < langCode.length; i++) {
            if (langCode[i].equals(currentLanguage)){
                languageSpinner.setSelection(i);
                break;
            }
        }
    }

    private void measurementSelector() {





    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button press
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setupUI(){

        languageSpinner = findViewById(R.id.spinnerLanguage);
       // temperatureSpinner = findViewById(R.id.spinnerMetric);
        dbSpinner = findViewById(R.id.spinnerDB);
    }




}
