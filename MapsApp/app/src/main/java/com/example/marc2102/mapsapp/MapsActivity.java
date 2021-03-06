package com.example.marc2102.mapsapp;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;
    private Location myLocation = null;
    private LatLng userLocation = null;
    private static final float MY_LOCATION_ZOOM_FACTORY = 17;
    private boolean isTracked = false;
    boolean type = true;
    Button searchButton;


    EditText Search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Search = (EditText) findViewById(R.id.editText_searcher);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sanDiego = new LatLng(33, -117);
        mMap.addMarker(new MarkerOptions().position(sanDiego).title("Born Here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sanDiego));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //mMap.setMyLocationEnabled(true);

    }

    public void setToggle(View v) {

        if (type == true) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            type = false;
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            type = true;
        }
    }

    public void clearAll(View view) {
        mMap.clear();
    }


    public void stoptracking() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(locationListenerGps);
        locationManager.removeUpdates(locationListenerNetwork);
        locationManager = null;

    }

    public void tracking() {
        try {
            Toast.makeText(this, "ENABLING TRACKING", Toast.LENGTH_SHORT).show();
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(GPS_PROVIDER);
            if (isGPSEnabled)
                Log.d("MyMaps", "getLocation: GPS is enabled");

            isNetworkEnabled = locationManager.isProviderEnabled(NETWORK_PROVIDER);
            if (isNetworkEnabled)
                Log.d("MyMaps", "getLocation: Network is enabled");

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("MyMapsh", "getLocation: No Provider is Enabled");
                return;
            } else {
                isTracked = true;
                if (isGPSEnabled) {
                    Log.d("MyMaps", "getLocation: GPS ENABLED; REQUESTION LOCATION UPDATES");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("MyMaps", "Failed coarse permission check");
                        Log.d("MyMaps", Integer.toString(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)));
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                    }
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("MyMaps", "Failed fine permission check");
                        Log.d("MyMaps", Integer.toString(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)));
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                    }
                    locationManager.requestLocationUpdates(GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGps);
                    Log.d("MyMaps", "getLocation: Network GPS update request successful");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT);

                }
                if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: NETWORK ENABLED; REQUESTION LOCATION UPDATES");
                    locationManager.requestLocationUpdates(NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    Log.d("MyMaps", "getLocation: Network GPS update request suCCESsFULL");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT);
                }
            }
        } catch (Exception e) {
            Log.d("MyMaps", "uh oh");
            e.printStackTrace();
        }
    }


    android.location.LocationListener locationListenerGps = new android.location.LocationListener() {
        @Override

        public void onLocationChanged(Location location) {
            if (isTracked) {
                //Log.d and Toast that GPS is enabled and working

                //set myLocation
                myLocation = location;

                //Drop a marker
                dropMarker(location.getProvider());
                //Remove the network location updates
                try {
                    locationManager.removeUpdates(locationListenerNetwork);
                } catch (SecurityException e) {

                }

            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            //setup a switch statement on status
            //case: where location provider is available (output a Log.D or toast)
            //case: location LocationProvider.OUT_OF_SERvIce-> request updates from network provider
            //case: locationProvider.TEMPORARILY_UNAVAILABLE --> request updates from network provider

            switch (status) {
                case LocationProvider.AVAILABLE:

                    Log.d("MyMaps", "LocationProvider is available");
                    break;
                case LocationProvider.OUT_OF_SERVICE:

                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
                default:
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };


    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Log.d and Toast that GPS is enabled and working

            //set myLocation
            myLocation = location;

            //Drop a marker on map
            dropMarker(location.getProvider());
            //Relaunch the network provider, request location Updates (NETWORK)
            try{
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }
            catch(SecurityException e) {

            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            //out message of Log.d and/or Toast
            Log.d("MyMaps", "Status change in the network");
            Toast.makeText(MapsActivity.this, "Network Location has changed", Toast.LENGTH_SHORT).show();


        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };


    public void dropMarker(String provider) {


        LatLng userLocation = null;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMaps", "Failed coarse permission check");
            Log.d("MyMaps", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMaps", "Failed fine permission check");
            Log.d("MyMaps", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        myLocation = null;
        if(locationManager != null) {
            myLocation = locationManager.getLastKnownLocation(provider);
        }
        if(myLocation != null) {
            userLocation = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOCATION_ZOOM_FACTORY);
            if(provider.equals(GPS_PROVIDER)) {
                mMap.addCircle(new CircleOptions().center(userLocation).radius(2).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.BLUE));
                //mMap.addMarker(new MarkerOptions().position(userLocation).title("Last Marked Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
            else {
                mMap.addCircle(new CircleOptions().center(userLocation).radius(2).strokeColor(Color.MAGENTA).strokeWidth(2).fillColor(Color.GREEN));
                //mMap.addMarker(new MarkerOptions().position(userLocation).title("Last Marked Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }
            mMap.animateCamera(update);
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
        }

    }


    public void trackMe(View view) {
        if (isTracked) {
            Toast.makeText(this, "Tracking", Toast.LENGTH_SHORT).show();
            tracking();
            isTracked = false;
        } else {
            isTracked = true;
        }
    }


    public void searchPOI(View view) {
        mMap.clear();
        Search = (EditText) findViewById(R.id.editText_searcher);
        searchButton = (Button) findViewById(R.id.button_3);

        String location = Search.getText().toString();
        List<Address> addressList = new ArrayList<>();
        List<Address> distanceList = new ArrayList<>();

        //checks to see if nothing is entered in the search so the app doesn't crash
        if (location.equals("")) {
            Toast.makeText(MapsActivity.this, "No Search Entered", Toast.LENGTH_SHORT).show();
            return;
        } else if (myLocation == null) {
            //if there is no location within the radius and the app needs a fallback
            Toast.makeText(MapsActivity.this, "No known location; please press 'Track Me' then try searching again", Toast.LENGTH_SHORT).show();
            return;
        } else if (location != null || !location.equals("")) {
            Log.d("MyMaps", "search feature started");
            Geocoder geocoder = new Geocoder(this);


            try {
                addressList = geocoder.getFromLocationName(location, 1000, (myLocation.getLatitude() - (5.0 / 60)), (myLocation.getLongitude() - (5.0 / 60)), (myLocation.getLatitude() + (5.0 / 60)), (myLocation.getLongitude() + (5.0 / 60)));

                Log.d("MyMaps", "made a max 100 entry search result");
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < addressList.size(); i++) {
                Address address = addressList.get(i);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title("Search Results"));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }

    }








