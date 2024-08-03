package com.example.nsgs_app;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class NetworkProviderGuesser {

    private static final Map<String, String> providers = new HashMap<>();

    static {
        // ADD MORE COMBINATIONS FOR NETWORK PROVIDERS
        providers.put("Bell", ".*Bell.*|.*BELL.*|.*bell.*");
        providers.put("Rogers", ".*Rogers.*|.*ROGERS.*|.*rogers.*");
        providers.put("Telus", ".*Telus.*|.*TELUS.*|.*telus.*");
        providers.put("Videotron", ".*Videotron.*|.*VIDEOTRON.*|.*videotron.*");
        providers.put("Shaw", ".*Shaw.*|.*SHAW.*|.*shaw.*");
        providers.put("Fido", ".*Fido.*|.*FIDO.*|.*fido.*");
        providers.put("Koodo", ".*Koodo.*|.*KOODO.*|.*koodo.*");
        providers.put("Freedom Mobile", ".*Freedom.*|.*FREEDOM.*|.*freedom.*|.*Freedom Mobile.*|.*FREEDOM MOBILE.*|.*freedom mobile.*"); // no spaces in ssid lol
        providers.put("Virgin Mobile", ".*Virgin.*|.*VIRGIN.*|.*virgin.*|.*Virgin Mobile.*|.*VIRGIN MOBILE.*|.*virgin mobile.*");
        providers.put("Public Mobile", ".*Public.*|.*PUBLIC.*|.*public.*|.*Public Mobile.*|.*PUBLIC MOBILE.*|.*public mobile.*");
        providers.put("Helix", ".*Helix.*|.*HELIX.*|.*helix.*");
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