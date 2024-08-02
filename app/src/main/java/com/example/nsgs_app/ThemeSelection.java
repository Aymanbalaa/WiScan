package com.example.nsgs_app;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeSelection {

    public static String themeInitializer(ViewGroup viewGroup, Context context) {

        String currentTheme = context.getSharedPreferences("prefs", MODE_PRIVATE).getString("Theme", "Light");
        switch (currentTheme) {
            case "Dark":
            case "Sombre":
            case "Темный":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;

            case "Warm":
            case "Amical":
            case "Теплый":
                viewGroup.setBackgroundResource(R.drawable.warm_background);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                //viewGroup.setBackgroundResource(R.drawable.item_background);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
        return currentTheme;
    }
}