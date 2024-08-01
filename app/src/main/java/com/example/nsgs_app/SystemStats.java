package com.example.nsgs_app;

import com.google.gson.annotations.SerializedName;


public class SystemStats {
    @SerializedName("cpuTemp")
    private String temperatureString;

    private  int temperature;

    @SerializedName("cpuTime")
    private String time;

    @SerializedName("scannerStatus")
    private int scannerStatus;

    public String getTemperatureString(){
        return temperatureString;
    }

    public String getTime() {
        if (time == null || time.isEmpty()) {
            return "Time data unavailable";
        }

        return time;
    }

    public int getStatus(){
        return scannerStatus;
    }

    public String getTemperature(String metric) {
        String temperatureString = getTemperatureString().replace("'C", "");
        double temperatureCelsius;

        try {
            temperatureCelsius = Double.parseDouble(temperatureString);
        } catch (NumberFormatException e) {
            return String.valueOf(R.string.unknown_values);
        }

        if (metric == null) {
            return temperatureCelsius + "°C"; // Default to Celsius if metric is null
        }

        switch (metric.toLowerCase()) {
            case "fahrenheit":
                return String.format("%.2f°F", (temperatureCelsius * 1.8) + 32);
            case "celsius":
                return String.format("%.2f°C", temperatureCelsius);
            default:
                return "Unknown metric";
        }
    }
}
