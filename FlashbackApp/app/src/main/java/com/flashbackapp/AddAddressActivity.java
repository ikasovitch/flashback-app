package com.flashbackapp;

import android.content.Intent;
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
                EditText longitudeEditText = findViewById(R.id.longitudeText);
                EditText latitudeEditText = findViewById(R.id.latitudeText);

                final String addressName = addressNameEditText.getText().toString();
                final String postalAddress = postalAddressEditText.getText().toString();
                final float longitude = Float.parseFloat(longitudeEditText.getText().toString());
                final float latitude = Float.parseFloat(latitudeEditText.getText().toString());

                final DatabaseReference databaseReference = AddressModel.getByKey(addressName);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            try {
                                AddressModel.create(dataSnapshot, postalAddress, longitude, latitude);
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
}
