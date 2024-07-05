package com.example.nsgs_app;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class NetworkProviderGuesser {

    private static final Map<String, String> providers = new HashMap<>();

    static {
        // ADD MORE HERE ADD MORE HERE ADD MORE HERE JUST HERE
        providers.put("Bell", ".*Bell.*|.*bell.*");
        providers.put("Rogers", ".*Rogers.*|.*rogers.*");
        providers.put("Telus", ".*Telus.*|.*telus.*");
        providers.put("Videotron", ".*Videotron.*|.*videotron.*");

    }

    public static String getNetworkProvider(String ssid) {
        for (Map.Entry<String, String> entry : providers.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getValue());
            Matcher matcher = pattern.matcher(ssid);
            if (matcher.matches()) {
                return entry.getKey();
            }
        }
        return "Unknown Provider";
    }
}