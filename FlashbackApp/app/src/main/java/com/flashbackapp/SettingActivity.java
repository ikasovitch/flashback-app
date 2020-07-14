package com.flashbackapp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private static final String TAG = "SettingActivity";
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        findViewById(R.id.buttonEditEmeNumber).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetEmergencyNumbers();
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        findViewById(R.id.buttonLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { signOut();
            }
        });
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        // Firebase sign out
        firebaseAuth.signOut();
        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Signed out of google");
                    }
                });
        launchLoginActivity();
    }

    private void GetEmergencyNumbers() {
        DatabaseReference primary = mDatabase.child("sos_numbers");
        primary.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot primary = dataSnapshot.child("primary");
                DataSnapshot others = dataSnapshot.child("others");
                HashMap<String, String> phone_number_by_name = new HashMap<>();
                for (DataSnapshot primaryChildSnapshot : primary.getChildren()) {
                    String name = primaryChildSnapshot.getKey();
                    String phone_number = Objects.requireNonNull(primaryChildSnapshot.child("number").getValue()).toString();
                    phone_number_by_name.put(name, phone_number);
                }
                for (DataSnapshot othersChildSnapshot : others.getChildren()) {
                    String name = othersChildSnapshot.getKey();
                    String phone_number = Objects.requireNonNull(othersChildSnapshot.child("number").getValue()).toString();
                    phone_number_by_name.put(name, phone_number);
                    // ...
                }
                setContentView(R.layout.activity_phone_numbers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "listener canceled", databaseError.toException());
            }
        });
    }
}
