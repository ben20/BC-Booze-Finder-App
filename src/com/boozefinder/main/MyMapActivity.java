package com.boozefinder.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayGestureDetector;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;
import de.android1.overlaymanager.ZoomEvent;

public class MyMapActivity extends MapActivity implements LocationListener {

    private static final String TAG = "MyMapActivity";
    private static final String STATE = "MyMapActivityState";
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";

    private List<LiquorStoreLocation> privateLiquorStores = new ArrayList<LiquorStoreLocation>();
    private List<LiquorStoreLocation> BCLiquorStores = new ArrayList<LiquorStoreLocation>();

    private MapView map;
    private LocationManager locationManager;
    private OverlayManager overlayManager;

    private double starting_lat = 49.223451;
    private double starting_lng = -122.998514;
    private GeoPoint start_point = new LatLngPoint(starting_lat, starting_lng);

    private ConcreteItemizedOverlay privateLiquorStoreOverlay;
    private ConcreteItemizedOverlay BCLiquorStoreOverlay;
    private ConcreteItemizedOverlay currentPositionOverlay;

    private MapController mapController;
    private List<Overlay> mapOverlays;

    private LinearLayout mapButtonBar;
    private ImageButton mapInfoButton;

    private String towers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.my_map_activity);
        
        mapButtonBar = (LinearLayout) findViewById(R.id.map_button);
        map = (MapView) findViewById(R.id.mapview);
        mapInfoButton = (ImageButton) findViewById(R.id.map_info_button);

        mapController = map.getController();
        mapOverlays = map.getOverlays();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        overlayManager = new OverlayManager(getApplication(), map);

        Drawable red_drawable = this.getResources().getDrawable(R.drawable.ic_action_location_red);
        Drawable blue_drawable = this.getResources()
                .getDrawable(R.drawable.ic_action_location_blue);
        Drawable green_drawable = this.getResources().getDrawable(
                R.drawable.ic_action_location_green);
        privateLiquorStoreOverlay = new ConcreteItemizedOverlay(green_drawable, this);
        currentPositionOverlay = new ConcreteItemizedOverlay(blue_drawable, this);
        BCLiquorStoreOverlay = new ConcreteItemizedOverlay(red_drawable, this);

        map.setBuiltInZoomControls(true);
        gotoGeopointOnMap(start_point);
        mapController.setZoom(13);

        mapOverlays.add(privateLiquorStoreOverlay);
        mapOverlays.add(BCLiquorStoreOverlay);
        mapOverlays.add(currentPositionOverlay);

        createTouchEventOverlay();

        parsePrivateLiquorStoreFile();
        parseBCLiquorStoreFile();
        initMapButtonBarListeners();

        towers = locationManager.getBestProvider(new Criteria(), false);
        locationManager.requestLocationUpdates(towers, 500, 10, this);
    }

    @Override
    public void onResume() {

        SharedPreferences state = getSharedPreferences(STATE, MODE_PRIVATE);
        if (state.contains(LATITUDE) == true) {
            GeoPoint point = new GeoPoint(state.getInt(LATITUDE, 0), state.getInt(LONGITUDE, 0));
            plotLocationOfInterestAndClosest(point);
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    /**
     * Parses in the liquor store file and stores the values in an
     * ArrayList<LiquorStore> as a class member.
     * 
     * @return
     */
    private void parsePrivateLiquorStoreFile() {

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
                privateLiquorStores.add(ls);
            }

        } catch (IOException e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
    }

    private void parseBCLiquorStoreFile() {

        String line;
        String dataLine[];
        LiquorStoreLocation ls;

        try {
            AssetManager am = getAssets();
            InputStream is = am.open("ls-latlng_BCL.txt");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                dataLine = line.split("\t");
                String address = dataLine[0];
                double lat = Double.parseDouble(dataLine[2]);
                double lng = Double.parseDouble(dataLine[3]);

                ls = new LiquorStoreLocation(address, lat, lng);
                ls.setCity(dataLine[1]);
                ls.setStoreNumber(dataLine[4]);
                ls.setName(dataLine[5]);
                ls.setPostalCode(dataLine[6]);
                ls.setPhoneNumber(dataLine[7]);

                BCLiquorStores.add(ls);
            }
        } catch (IOException e) {
            Log.d(TAG, "Error" + e.getMessage());
        }
    }

    private void clearAllOverlays() {
        currentPositionOverlay.clearOverlays();
        privateLiquorStoreOverlay.clearOverlays();
        BCLiquorStoreOverlay.clearOverlays();
    }

    private void plotLocationOfInterestAndClosest(GeoPoint point) {

        SharedPreferences state = getSharedPreferences(STATE, MODE_PRIVATE);
        SharedPreferences.Editor editor = state.edit();
        editor.putInt(LATITUDE, point.getLatitudeE6());
        editor.putInt(LONGITUDE, point.getLongitudeE6());
        editor.commit();

        clearAllOverlays();

        plotGeopointLocaton(currentPositionOverlay, "Current Location", "", point);
        gotoGeopointOnMap(point);

        List<LiquorStoreLocation> private_ls = findXClosest(5, point, privateLiquorStores);
        List<LiquorStoreLocation> BC_ls = findXClosest(5, point, BCLiquorStores);

        try {
            for (LiquorStoreLocation lsl : private_ls)
                plotPrivateLiquorStore(lsl);

            for (LiquorStoreLocation lsl : BC_ls)
                plotBCLiquorStore(lsl);
        } catch (NullPointerException e) {
            Log.d(TAG, e.getMessage());
        }

        map.invalidate();
    }

    /**
     * 
     * @param overlay
     *            overlay to plot this geopoint
     * @param information
     *            information stored in the dialog message field
     * @param point
     *            is the geopoint to be plotted
     * @param type
     *            is the type of liquor store and is stored as the dialog title
     * 
     */
    private void plotGeopointLocaton(ConcreteItemizedOverlay overlay, String type,
            String information, GeoPoint point) {
        OverlayItem overlayItem = new OverlayItem(point, type, information);
        overlay.addOverlay(overlayItem);
    }

    private void plotPrivateLiquorStore(LiquorStoreLocation ls) {
        String address = ls.getAddress();
        GeoPoint point = new LatLngPoint(ls.getLat(), ls.getLng());
        plotGeopointLocaton(privateLiquorStoreOverlay, "Private Liquor Store", address, point);
    }

    private void plotBCLiquorStore(LiquorStoreLocation ls) {
        String information = ls.getInformation();
        GeoPoint point = new LatLngPoint(ls.getLat(), ls.getLng());
        plotGeopointLocaton(BCLiquorStoreOverlay, " BC Liquor Store", information, point);
    }

    private void gotoGeopointOnMap(GeoPoint point) {
        mapController.animateTo(point);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private LiquorStoreLocation findClosest(GeoPoint location, List<LiquorStoreLocation> listOfLs) {

        if (location != null) {
            LiquorStoreLocation closest = null;
            double approxLat = listOfLs.get(0).getLat();
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
                    listOfLs);

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

    private void initMapButtonBarListeners() {
        mapButtonBar.setClickable(true);

        mapButtonBar.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Location location = locationManager.getLastKnownLocation(towers);

                if (location != null) {
                    plotLocationOfInterestAndClosest(new LatLngPoint(location.getLatitude(),
                            location.getLongitude()));
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Location cannot be found.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        
        mapInfoButton.setOnClickListener(new android.view.View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyMapActivity.this, HelpActivity.class));
            }
        });
    }

    public void createTouchEventOverlay() {
        ManagedOverlay managedOverlay = overlayManager.createOverlay("touchOverlay");

        managedOverlay
                .setOnOverlayGestureListener(new ManagedOverlayGestureDetector.OnOverlayGestureListener() {

                    @Override
                    public boolean onZoom(ZoomEvent arg0, ManagedOverlay arg1) {
                        return false;
                    }

                    @Override
                    public boolean onSingleTap(MotionEvent arg0, ManagedOverlay arg1,
                            GeoPoint arg2, ManagedOverlayItem arg3) {
                        return false;
                    }

                    @Override
                    public boolean onScrolled(MotionEvent arg0, MotionEvent arg1, float arg2,
                            float arg3, ManagedOverlay arg4) {
                        return false;
                    }

                    @Override
                    public void onLongPressFinished(MotionEvent arg0, ManagedOverlay arg1,
                            GeoPoint arg2, ManagedOverlayItem arg3) {

                    }

                    @Override
                    public void onLongPress(MotionEvent arg0, ManagedOverlay arg1) {

                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent arg0, ManagedOverlay arg1,
                            final GeoPoint arg2, ManagedOverlayItem arg3) {

                        HoloAlertDialogBuilder alertBuilder = new HoloAlertDialogBuilder(
                                MyMapActivity.this);
                        alertBuilder.setCancelable(true);
                        alertBuilder.setTitle("Plot");
                        alertBuilder.setMessage("Find liquor stores near this location");
                        alertBuilder.setPositiveButton("Plot", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                plotLocationOfInterestAndClosest(arg2);
                            }
                        });
                        alertBuilder.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert = alertBuilder.create();
                        alert.show();
                        return false;
                    }
                });
        overlayManager.populate();
    }

    @Override
    public void onLocationChanged(Location arg0) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
