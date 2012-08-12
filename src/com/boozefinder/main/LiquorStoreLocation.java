package com.boozefinder.main;

public class LiquorStoreLocation {

    private String city;
    private String address;
    private LatLng latLng;
    private String phoneNumber;
    private String storeNumber;
    private String name;
    private String postalCode;
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(String storeNumber) {
        this.storeNumber = storeNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
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
    
    public String getInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append(name + ", " + storeNumber + "\n\n");
        sb.append(address + ", " + city + "\n");
        sb.append(phoneNumber + "\n");
        return sb.toString();
        
    }

}
