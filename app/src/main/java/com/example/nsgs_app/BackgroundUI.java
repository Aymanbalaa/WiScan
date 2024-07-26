package com.example.nsgs_app;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatDelegate;

public class BackgroundUI {

    private static AnimationDrawable animationDrawable;

    public static void backgroundUI(Context context, View view){

        animationDrawable = (AnimationDrawable) view.getBackground();

        view.setBackground(animationDrawable);
        if(animationDrawable != null){
            animationDrawable.setEnterFadeDuration(2500);
            animationDrawable.setExitFadeDuration(5000);
            animationDrawable.start();
        } else{
            System.out.println("AnimationDrawable is null");
        }
    }

    public static void backgroundPage(ViewGroup viewGroup, Context context){

        String currentBackground = context.getSharedPreferences("prefs", MODE_PRIVATE).getString("Theme", "Light");
        switch(currentBackground){
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Warm":
                viewGroup.setBackgroundResource(R.drawable.gradient_list);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Cool":
                viewGroup.setBackgroundResource(R.drawable.gradient_background_4);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                viewGroup.setBackgroundResource(R.drawable.gradient_background_light);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }


    public static void setBackground(Context context, String backgroundTheme){
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("backgroundTheme", backgroundTheme);
        editor.apply();
    }

}
