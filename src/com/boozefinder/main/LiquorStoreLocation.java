package com.boozefinder.main;

public class LiquorStoreLocation {

    private String city;
    private String address;

    private LatLng latLng;

    public LiquorStoreLocation(String city, String address) {
        this.city = city;
        this.address = address;
    }
    
    public LiquorStoreLocation(String address, double lat, double lng) {
        this.address = address;
        latLng = new LatLng(lat, lng);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
    
    public double getLat() {
        return latLng.getLat();
    }
    public double getLng() {
        return latLng.getLng();
    }

}
