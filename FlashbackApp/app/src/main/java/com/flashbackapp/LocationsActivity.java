package com.flashbackapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
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

import java.util.Map;


public class LocationsActivity extends AppCompatActivity implements LocationListener {
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;

    private DatabaseReference mDatabase;
    private static final String TAG = "SettingActivity";
    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private static final int INITIAL_REQUEST=1337;

    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        mDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl("https://shai-mfh-3586.firebaseio.com");

        if (!canAccessLocation()) {
            requestPermissions(LOCATION_PERMISSIONS, INITIAL_REQUEST);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);

        GetKnownAddresses();
    }

    public void onLocationChanged(Location location) {
        TextView txtLat = findViewById(R.id.LocationText);
        Log.d(TAG, String.valueOf(location.getLatitude()));
        Log.d(TAG, String.valueOf(location.getLongitude()));
        txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
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

    private void GetKnownAddresses() {
        DatabaseReference addressesRef = mDatabase.child("known_address");
        addressesRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        collectLocations((Map<String, Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "listener canceled", databaseError.toException());
                    }
                }
        );
    }

    private void collectLocations(Map<String, Object> addresses) {
        for (Map.Entry<String, Object> entry: addresses.entrySet()) {
            Map singleAddress = (Map) entry.getValue();
            System.out.println("Latitude:"  + singleAddress.get("latitude"));
            System.out.println("Longitude:"  + singleAddress.get("longitude"));
        }
    }
}