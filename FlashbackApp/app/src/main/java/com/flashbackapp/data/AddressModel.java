package com.flashbackapp.data;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class AddressModel {

    public static Address create(DataSnapshot snapshot, String addressParts, float longitude, float latitude) {
        final Address address = new Address(addressParts, longitude, latitude);
        snapshot.getRef().setValue(address);
        return address;
    }

    public static DatabaseReference getByKey(String addressName) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference addressTable = firebaseDatabase.getReference().child("known_address");
        return addressTable.child(addressName);
    }

    public interface AddressReadEventListener {
        void onAddressRead(String addressName, Address address);
        void onFinish();
    }

    public static void readKnownAddresses(final AddressReadEventListener readListener) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference knownAddressTable = firebaseDatabase.getReference().child("known_address");
        knownAddressTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        String addressName = child.getKey();
                        Address address = child.getValue(Address.class);
                        readListener.onAddressRead(addressName, address);
                    }
                    readListener.onFinish();
                }
                else {
                    readListener.onFinish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
