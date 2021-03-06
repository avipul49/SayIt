package com.sayit.vipulmittal.sayit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements android.location.LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker marker;
    private boolean locationSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyService.STOP);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    /**
     * ********** Called after each 3 sec *********
     */
    @Override
    public void onLocationChanged(Location location) {
        String str = "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude();
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            if (!locationSent) {
                addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.size() > 0) {
                    Intent intent = new Intent();
                    intent.setAction(MyService.LOCATION_FOUND);
                    intent.putExtra("location", addresses.get(0).getLocality());
                    sendBroadcast(intent);
                }
                locationSent = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Toast.makeText(getBaseContext(), str, Toast.LENGTH_LONG).show();
        if (marker != null)
            marker.remove();
        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
    }

    @Override
    public void onProviderDisabled(String provider) {

        /******** Called when User off Gps *********/

        Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {

        /******** Called when User on Gps  *********/

        Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("S---", "Message received");
            String action = intent.getAction();
            if (action.equals(MyService.STOP)) {
                MapsActivity.this.finish();
            }
        }
    };
}
