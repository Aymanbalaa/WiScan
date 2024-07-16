package com.example.nsgs_app;

public class Network {
    private String ssid;
    private String bssid;
    private String capabilities;
    private int frequency;
    private int level;
    private String security;
    private String coordinates;
    private String postalCode;
    private String neighborhood;

    // Constructor
    public Network(String ssid, String bssid, String capabilities, int frequency, int level, String security, String coordinates, String postalCode, String neighborhood) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.capabilities = capabilities;
        this.frequency = frequency;
        this.level = level;
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

    public String getCapabilities() {
        return capabilities;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getLevel() {
        return level;
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

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setLevel(int level) {
        this.level = level;
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
}
