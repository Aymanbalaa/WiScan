package com.example.nsgs_app;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class SystemStats {
    @SerializedName("cpuTemp")
    private String temperatureString;

    private  int temperature;

    @SerializedName("cpuTime")
    private String time;

    @SerializedName("scannerStatus")
    private int scannerStatus;

    @SerializedName("id")
    private int id;

    public String getTemperatureString(){
        return temperatureString;
    }

    public String getTime(){
        return time;
    }

    public int getStatus(){
        return scannerStatus;
    }

    public int getId(){
        return id;
    }


    // metric will be fetched from sharedPref or something similar from the settings page!!!
    public String getTemperature(String metric){
        double temperatureCelsius = Double.parseDouble(getTemperatureString().replace("'C",""));

        if (Objects.equals(metric, "Fahrenheit"))
        {
            return (temperatureCelsius*(1.8)+32) + "°F";  // formula is (0°C × 9/5) + 32 = 32°F
        }
        else
        {
            return temperatureCelsius + "°C";
        }
    }
}
