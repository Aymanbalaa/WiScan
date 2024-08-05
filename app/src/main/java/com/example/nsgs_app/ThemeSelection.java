package com.example.nsgs_app;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class ThemeSelection {

    public static String themeInitializer(ViewGroup viewGroup, Context context, Activity activity) {

        String currentTheme = context.getSharedPreferences("prefs", MODE_PRIVATE).getString("Theme", "Light");
        switch (currentTheme) {
            case "Dark":
            case "Sombre":
            case "Темный":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                if (activity.getActionBar() != null) {
                    Objects.requireNonNull(activity.getActionBar()).setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.receiver_text_color)));
                }
                break;

            case "Light":
            case "Clair":
            case "Свет":
            default:
                //viewGroup.setBackgroundResource(R.drawable.gradient_background_light);
                if (activity.getActionBar() != null) {
                    Objects.requireNonNull(activity.getActionBar()).setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.colorPrimary)));
                }
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
        return currentTheme;
    }
}