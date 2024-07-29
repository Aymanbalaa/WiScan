package com.example.nsgs_app;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatDelegate;

public class BackgroundUI {

    public static String backgroundPage(ViewGroup viewGroup, Context context){

        String currentBackground = context.getSharedPreferences("prefs", MODE_PRIVATE).getString("Theme", "Light");
        switch(currentBackground){
            case "Dark":
            case "Sombre":
            case "Темный":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;

            case "Warm":
            case "Amical":
            case "Теплый":
                viewGroup.setBackgroundResource(R.drawable.gradient_background_2);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Cool":
            case "Calme":
            case "Прохладный":
                viewGroup.setBackgroundResource(R.drawable.gradient_background);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                viewGroup.setBackgroundResource(R.drawable.gradient_background_light);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
        return currentBackground;
    }
}
