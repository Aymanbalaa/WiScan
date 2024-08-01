package com.example.nsgs_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SettingsDialogFragment extends DialogFragment {

    private Spinner languageSpinner, temperatureSpinner, dbSpinner;

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
    }

    private void setupUI(View view) {
        languageSpinner = view.findViewById(R.id.spinnerLanguage);
        temperatureSpinner = view.findViewById(R.id.spinnerMetric);
        dbSpinner = view.findViewById(R.id.spinnerDB);
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
        String[] fetchUnits = getResources().getStringArray(R.array.databaseArray);

        for (int i = 0; i < fetchUnits.length; i++) {
            if (fetchUnits[i].equals(currentUnit)) {
                dbSpinner.setSelection(i);
                break;
            }
        }
    }
}
