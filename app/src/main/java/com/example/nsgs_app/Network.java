package com.example.nsgs_app;

public class Network {
    private int id;
    private String ssid;
    private String bssid;
    private String postalCode;
    private String security;
    private String neighborhood;

    //used to get and store the network details
    // class offers a better interface

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

    public void setBssid(String bssid) {
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

    private String coordinates;

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
