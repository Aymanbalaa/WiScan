package com.example.nsgs_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class Language {

    //@TODO switch from locale to auto translate
    public static void saveLanguage(Context context, String code){
       SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE); // save this in shared pref for settings
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", code);
        editor.apply();
    }

    public static void setLanguage(Context context, String code){
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration conf = resources.getConfiguration();
        conf.setLocale(locale);
        resources.updateConfiguration(conf,resources.getDisplayMetrics());
    }

    public static String getLanguage(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE); // at open we get default langgg
        return sharedPreferences.getString("language", "en");
    }
// does this need null exception?


}
