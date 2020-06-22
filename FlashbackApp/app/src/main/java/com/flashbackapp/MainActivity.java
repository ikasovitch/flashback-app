package com.flashbackapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final String ARG_NAME = "username";
    private DatabaseReference mDatabase;

    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        setTitle();
        findViewById(R.id.buttonLogout).setOnClickListener(this);
        findViewById(R.id.buttonWhatAmIDoing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchWhatAmIDoingActivity();
            }
        });
        findViewById(R.id.buttonShayStory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchShayStoryActivity();
            }
        });
        findViewById(R.id.ButtonEmergencyNumber).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallEmergencyNumber();
            }
        });

        findViewById(R.id.ButtonSetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchShaySettingActivity();
            }
        });
        findViewById(R.id.ButtonLocations).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchShayLocationsActivity();
            }
        });
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        CallEmergencyNumber();
    }

    private void setTitle() {
        TextView titleText = findViewById(R.id.titleText);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String titleTextString = "בוקר טוב שי";
        int color = R.color.morning;
        if (currentHour > 12 && currentHour < 18) {
            color = R.color.noon;
            titleTextString = "צהריים טובים שי";
        } else if (currentHour > 18) {
            titleTextString = "ערב טוב שי";
            color = R.color.evening;
        }
        titleText.setText(titleTextString);
        titleText.setBackgroundColor(color);
        titleText.setTypeface(null, Typeface.BOLD);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonLogout:
                signOut();
                break;
        }
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
    private void revokeAccess() {
        // Firebase sign out
        firebaseAuth.signOut();
        // Google revoke access
        googleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Revoked Access");
                    }
                });
        launchLoginActivity();
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(intent);
    }

    private void launchWhatAmIDoingActivity() {
        Intent intent = new Intent(getBaseContext(), WhatAmIDoing.class);
        startActivity(intent);
    }

    private void launchShayStoryActivity() {
        Intent intent = new Intent(getBaseContext(), ShayStory.class);
        startActivity(intent);
    }

    private void launchShaySettingActivity() {
        Intent intent = new Intent(getBaseContext(), SettingActivity.class);
        startActivity(intent);
    }

    private void launchShayLocationsActivity() {
        Intent intent = new Intent(getBaseContext(), LocationsActivity.class);
        startActivity(intent);
    }

    private void CallEmergencyNumber() {   DatabaseReference primary = mDatabase.child("sos_numbers").child("primary");
        primary.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    String name = childSnapshot.getKey();
                    String phone_number = Objects.requireNonNull(childSnapshot.child("number").getValue()).toString();
                    String image = Objects.requireNonNull(childSnapshot.child("picture").getValue()).toString();
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+phone_number));
                    if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    startActivity(callIntent);
                    // ...
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "listener canceled", databaseError.toException());
            }
        });

    }

}
