package com.example.nsgs_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;


public class Language {

    private SharedPreferences sharedPreferences;

    public void saveLanguage(Context context, String langCode){
        sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", langCode);
        editor.apply();
    }

    public static void setLanguage(Context context, String langCode){

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration conf = resources.getConfiguration();
        conf.setLocale(locale);
        resources.updateConfiguration(conf,resources.getDisplayMetrics());

    }



}
