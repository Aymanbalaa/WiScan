package com.example.nsgs_app;

public class Network {
    private int id;
    private String ssid;
    private String bssid;
    private String postalCode;
    private String security;
    private String neighborhood;
    private String coordinates;
    private String provider;

    //Cosntructor only for testing filtering function can be removed later
    public Network(int id, String ssid, String security) {
        this.id = id;
        this.ssid = ssid;
        this.security = security;
    }

    //Cosntructor only for testing Details activity can be removed later


    public Network(int id, String ssid, String bssid, String postalCode, String security, String neighborhood, String coordinates, String provider) {
        this.id = id;
        this.ssid = ssid;
        this.bssid = bssid;
        this.postalCode = postalCode;
        this.security = security;
        this.neighborhood = neighborhood;
        this.coordinates = coordinates;
        this.provider = provider;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String ssid) {
        this.bssid = bssid;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
