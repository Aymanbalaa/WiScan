package com.example.nsgs_app;

public class TemperatureConversion {

    public Double celsiusToFarenheit(double temperature){
        return (temperature * 9/5) + 32;
    }

    public Double farenheitToCelsius(double temperature){
        return (temperature - 32) * 9/5;
    }

}
