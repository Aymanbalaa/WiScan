package com.example.nsgs_app;

import android.content.Context;
import android.content.SharedPreferences;

public class TemperaturePreference {

    public static void saveTemperatureUnit (Context context,String temperatureUnit){

        SharedPreferences sharedPreferences = context.getSharedPreferences("temperature_unit", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("temperature", temperatureUnit);
        editor.apply();
    }
}



