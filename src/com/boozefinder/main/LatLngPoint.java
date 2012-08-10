package com.boozefinder.main;

import com.google.android.maps.GeoPoint;

public class LatLngPoint extends GeoPoint {

    public LatLngPoint(double latitude, double longitude) {
        super((int) (latitude * 1E6), (int) (longitude * 1E6));
    }
}
