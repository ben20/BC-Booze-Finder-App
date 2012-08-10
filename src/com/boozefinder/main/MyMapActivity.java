package com.boozefinder.main;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MyMapActivity extends MapActivity implements LocationListener {

    private static final String TAG = "MyMapActivity";

    private List<LiquorStoreLocation> addresses = new ArrayList<LiquorStoreLocation>();

    private MapView map;
    private LocationManager locationManager;

    // set the starting point at Metrotown in Burnaby BC
    private double starting_lat = 49.223451;
    private double starting_lng = -122.998514;
    private GeoPoint start_point = new LatLngPoint(starting_lat, starting_lng);

    private ConcreteItemizedOverlay itemizedoverlay;
    private ConcreteItemizedOverlay currentPositionOverlay;
    private Overlay touchOverlay;

    private MapController mapController;
    private GeoPoint currentPoint;

    private LinearLayout mapButtonBar;

    private long startPress;
    private long stopPress;

    private String towers;

    private List<Overlay> mapOverlays;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.my_map_activity);

        mapButtonBar = (LinearLayout) findViewById(R.id.map_button_bar);
        initMapButtonBarListener();

        map = (MapView) findViewById(R.id.mapview);
        mapController = map.getController();
        mapOverlays = map.getOverlays();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        map.setBuiltInZoomControls(true);

        Drawable red_drawable = this.getResources().getDrawable(R.drawable.ic_action_pin_red);
        Drawable blue_drawable = this.getResources()
                .getDrawable(R.drawable.ic_action_location_blue);

        itemizedoverlay = new ConcreteItemizedOverlay(red_drawable, this);
        currentPositionOverlay = new ConcreteItemizedOverlay(blue_drawable, this);

        gotoGeopointOnMap(start_point);
        // initPlotCurrentLocation();

        touchOverlay = new TouchEventOverlay(this);

        mapOverlays.add(itemizedoverlay);
        mapOverlays.add(currentPositionOverlay);
        mapOverlays.add(touchOverlay);

        parseLiquorStoreFile();
    }

    @Override
    public void onResume() {
        super.onResume();
        Criteria criteria = new Criteria();
        towers = locationManager.getBestProvider(criteria, false);

        Log.d(TAG, "Towers: " + towers);
        locationManager.requestLocationUpdates(towers, 500, 1, this);
    }

    private void plotCurrentLocation(Location location) {

        double lat = location.getLatitude();
        double lng = location.getLongitude();
        currentPoint = new LatLngPoint(lat, lng);

        currentPositionOverlay.clearOverlays();
        OverlayItem overlayItem = new OverlayItem(currentPoint, "Current Location", "");
        currentPositionOverlay.addOverlay(overlayItem);
        gotoGeopointOnMap(currentPoint);

        GeoPoint point = new LatLngPoint(location.getLatitude(), location.getLongitude());

        itemizedoverlay.clearOverlays();
        List<LiquorStoreLocation> lsls = findXClosest(10, point, addresses);

        try {
            for (LiquorStoreLocation lsl : lsls)
                plotLiquorStore(lsl);
        } catch (NullPointerException e) {
        }
    }

    /**
     * Parses in the liquor store file and stores the values in an
     * ArrayList<LiquorStore> as a class member.
     * 
     * @return
     */
    private void parseLiquorStoreFile() {

        String line;
        String dataLine[];
        LiquorStoreLocation ls;

        try {
            AssetManager am = getAssets();
            InputStream is = am.open("ls-latlng.txt");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                dataLine = line.split("\t");
                String address = dataLine[0];
                double lat = Double.parseDouble(dataLine[1]);
                double lng = Double.parseDouble(dataLine[2]);

                ls = new LiquorStoreLocation(address, lat, lng);
                addresses.add(ls);
            }

        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }

        List<LiquorStoreLocation> lsls = findXClosest(10, currentPoint, addresses);

        try {
            for (LiquorStoreLocation lsl : lsls)
                plotLiquorStore(lsl);
        } catch (NullPointerException e) {

        }
    }

    /**
     * Plots the given lat/lng
     */
    private void plotGeopoint(String address, double lat, double lng) {
        try {
            GeoPoint geopoint = new LatLngPoint(lat, lng);

            OverlayItem overlayitem = new OverlayItem(geopoint, "PRIVATE", address);

            itemizedoverlay.addOverlay(overlayitem);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void plotGeoPoint(GeoPoint point) {
        try {
            currentPositionOverlay.clearOverlays();
            OverlayItem overlayItem = new OverlayItem(point, "Location", "");
            currentPositionOverlay.addOverlay(overlayItem);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void plotLiquorStore(LiquorStoreLocation ls) {
        String address = ls.getAddress();
        double lat = ls.getLat();
        double lng = ls.getLng();
        plotGeopoint(address, lat, lng);
    }

    private void gotoGeopointOnMap(GeoPoint point) {
        mapController.setCenter(point);
        mapController.setZoom(13);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private LiquorStoreLocation findClosest(GeoPoint location, List<LiquorStoreLocation> listOfLs) {

        if (location != null) {
            LiquorStoreLocation closest = null;
            double approxLat = listOfLs.get(0).getLat();
            // initialize the mindist value to the maximum possible value
            double minDistValue = Double.MAX_VALUE;

            double loc_lat = location.getLatitudeE6() / 1E6;
            double loc_lng = location.getLongitudeE6() / 1E6;
            LatLng location_latLng = new LatLng(loc_lat, loc_lng);

            for (LiquorStoreLocation ls : listOfLs) {
                double distValue = getDistanceValue(approxLat, location_latLng, ls.getLatLng());
                if (distValue < minDistValue) {
                    minDistValue = distValue;
                    closest = ls;
                }
            }
            return closest;
        }
        return null;
    }

    private List<LiquorStoreLocation> findXClosest(int x, GeoPoint location,
            List<LiquorStoreLocation> listOfLs) {

        if (location != null) {
            List<LiquorStoreLocation> closest = new ArrayList<LiquorStoreLocation>();
            List<LiquorStoreLocation> liquorStoreLocations = new ArrayList<LiquorStoreLocation>(
                    addresses);
            //
            // for (LiquorStoreLocation ls : addresses) {
            // liquorStoreLocations.add(ls);
            // }

            for (int i = 0; i < x; i++) {
                LiquorStoreLocation lsl = findClosest(location, liquorStoreLocations);

                if (lsl != null) {
                    closest.add(lsl);
                    liquorStoreLocations.remove(lsl);
                }
            }

            return closest;
        }
        return null;
    }

    private double getDistanceValue(double approxLatitude, LatLng from, LatLng to) {
        double latAdjust = Math.cos(Math.PI * approxLatitude / 180);
        double latDiff = from.getLat() - to.getLat();
        double lngDiff = from.getLng() - to.getLng();

        double diff = Math.pow(latDiff, 2) + Math.pow(latAdjust * lngDiff, 2);
        return diff;
    }

    private void initMapButtonBarListener() {
        mapButtonBar.setClickable(true);

        mapButtonBar.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Criteria criteria = new Criteria();
                towers = locationManager.getBestProvider(criteria, false);

                Location location = locationManager.getLastKnownLocation(towers);

                System.out.println("clicked first");
                if (location != null) {
                    System.out.println("clicked inside: " + location.getLatitude());
                    plotCurrentLocation(location);
                }

                // ie location is null.
                else {
                    Location location1 = locationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    plotCurrentLocation(location1);
                }

                System.out.println("clicked");
            }
        });
    }

    class TouchEventOverlay extends Overlay {

        int x;
        int y;
        MyMapActivity mapActivity;
        GeoPoint point;
        MapView map;

        public TouchEventOverlay(MyMapActivity ma) {
            super();
            this.mapActivity = ma;
        }

        @Override
        public boolean onTouchEvent(MotionEvent e, final MapView map) {

            this.map = map;
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                startPress = e.getEventTime();
                x = (int) e.getX();
                y = (int) e.getY();

                point = map.getProjection().fromPixels(x, y);
            }

            if (e.getAction() == MotionEvent.ACTION_UP)
                stopPress = e.getEventTime();

            if ((stopPress - startPress) > 1000) {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MyMapActivity.this);
                alertBuilder.setCancelable(true);
                alertBuilder.setMessage("Show liquor stores at this location");
                alertBuilder.setPositiveButton("Plot", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mapActivity.plotGeoPoint(point);

                        mapActivity.itemizedoverlay.clearOverlays();
                        List<LiquorStoreLocation> lsls = mapActivity.findXClosest(10, point,
                                addresses);

                        try {
                            for (LiquorStoreLocation lsl : lsls)
                                plotLiquorStore(lsl);
                        } catch (NullPointerException e) {
                        }
                        map.invalidate();
                    }
                });
                alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alert = alertBuilder.create();
                alert.show();

            }
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }
}
