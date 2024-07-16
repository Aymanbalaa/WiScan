package com.example.nsgs_app;

public class Network {
    private String ssid;
    private String bssid;
    private String security;
    private String coordinates;
    private String postalCode;
    private String neighborhood;

    // Constructor
    public Network(String ssid, String bssid, String security, String coordinates, String postalCode, String neighborhood) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.security = security;
        this.coordinates = coordinates;
        this.postalCode = postalCode;
        this.neighborhood = neighborhood;
    }

    // Getter methods
    public String getSsid() {
        return ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSecurity() {
        return security;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    // Setter methods (if needed)
    public void setSsid(String ssid) {
        this.ssid = ssid;
    }


    public void setSecurity(String security) {
        this.security = security;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    //blablabla bs string filtering
    public double getLatitude() {
        if (coordinates != null && !coordinates.isEmpty()) {
            String[] parts = coordinates.split(",");
            if (parts.length == 2) {
                try {
                    return Double.parseDouble(parts[0].trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0.0;
    }

    public double getLongitude() {
        if (coordinates != null && !coordinates.isEmpty()) {
            String[] parts = coordinates.split(",");
            if (parts.length == 2) {
                try {
                    return Double.parseDouble(parts[1].trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0.0;
    }
}
