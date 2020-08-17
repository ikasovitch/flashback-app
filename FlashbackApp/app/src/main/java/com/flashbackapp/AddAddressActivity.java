package com.flashbackapp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flashbackapp.data.AddressModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

public class AddAddressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.enter_address_title);
        setContentView(R.layout.activity_add_address);

        Button createNewAddressButton = (Button)findViewById(R.id.btnNewAddress);
        createNewAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                EditText addressNameEditText = findViewById(R.id.addressNameText);
                EditText postalAddressEditText = findViewById(R.id.postalAddressText);

                final String addressName = addressNameEditText.getText().toString();
                final String postalAddress = postalAddressEditText.getText().toString();
                final Location location;
                try {
                    location = getLocationFromAddress(postalAddress);
                } catch (IOException e) {
                    showToast(R.string.invalid_address);
                    return;
                }
                final DatabaseReference databaseReference = AddressModel.getByKey(addressName);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            try {
                                AddressModel.create(dataSnapshot, postalAddress, (float)location.getLongitude(), (float)location.getLatitude());
                                showToast(R.string.address_create_successfully);
                                Intent intent = new Intent(getBaseContext(), SettingActivity.class);
                                startActivity(intent);
                            } catch (IllegalArgumentException iae) {
                                showToast(R.string.invalid_address);
                            }

                        } else {
                            showToast(R.string.address_already_exists);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void showToast(int resourceId) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(), resourceId, duration);
        toast.show();
    }

    public Location getLocationFromAddress(String strAddress) throws IOException {
        Geocoder coder = new Geocoder(this);
        List<Address> addresses;
        Location location = new Location(LocationManager.GPS_PROVIDER);

        addresses = coder.getFromLocationName(strAddress,1);
        if (addresses==null) {
            showToast(R.string.invalid_address);
            return location;
        }
        Address address = addresses.get(0);
        location.setLatitude(address.getLatitude());
        location.setLongitude(address.getLongitude());
        return location;
    }
}
