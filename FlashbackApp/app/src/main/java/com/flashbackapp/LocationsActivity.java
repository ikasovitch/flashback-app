package com.flashbackapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class LocationsActivity extends AppCompatActivity implements LocationListener {
    protected LocationManager locationManager;

    private DatabaseReference mDatabase;
    private static final String TAG = "SettingActivity";
    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int METERS_THRESHOLD = 500;

    private Location currentLocation;
    private String closestLocation;

    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        mDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl("https://shai-mfh-3586.firebaseio.com");

        if (!canAccessLocation()) {
            requestPermissions(LOCATION_PERMISSIONS, INITIAL_REQUEST);
        }

        currentLocation = new Location ("currentLocation");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);

        updateClosestLocation();
        PrintClosetsLocation();
    }

    public void onLocationChanged(Location location) {
        currentLocation.setLatitude(location.getLatitude());
        currentLocation.setLongitude(location.getLongitude());
        updateClosestLocation();
        PrintClosetsLocation();
    }

    public void PrintClosetsLocation() {
        TextView txtLat = findViewById(R.id.LocationText);
        if (closestLocation != null) {
            txtLat.setText("אני נמצא ב" + closestLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
    }

    private void updateClosestLocation() {
        DatabaseReference addressesRef = mDatabase.child("known_address");
        addressesRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        updateClosestLocation((Map<String, Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "listener canceled", databaseError.toException());
                    }
                }
        );
    }

    private void updateClosestLocation(Map<String, Object> addresses) {
        double minimalDistance = 0;
        String minimalLocation = null;
        for (Map.Entry<String, Object> entry: addresses.entrySet()) {
            Map singleAddress = (Map) entry.getValue();
            Location addressLocation = new Location("addressLocation");
            addressLocation.setLatitude(new Double(singleAddress.get("latitude").toString()));
            addressLocation.setLongitude(new Double(singleAddress.get("longitude").toString()));
            double distance = addressLocation.distanceTo(currentLocation);
            if (minimalLocation == null || distance < minimalDistance) {
                minimalLocation = entry.getKey();
                minimalDistance = distance;
            }
        }
        if (minimalDistance < METERS_THRESHOLD) {
            closestLocation = minimalLocation;
        } else {
            try {
                closestLocation = getAddress(currentLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getAddress(Location location) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.forLanguageTag("he"));
        List<Address> addresses  = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(), 1);
        String address = addresses.get(0).getAddressLine(0);
        return address;
    }
}