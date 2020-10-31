package com.flashbackapp;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;
    private DatabaseReference mDatabase;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        findViewById(R.id.buttonEditEmeNumber).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPhoneNumberActivity();
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        findViewById(R.id.buttonLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { signOut();
            }
        });

        findViewById(R.id.BackButtonSetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMainActivity();
            }
        });

        findViewById(R.id.ButtonEditStory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchEditStoryActivity();
            }
        });

        findViewById(R.id.buttonAddAddress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newAddressIntent = new Intent(v.getContext(), AddressManagerActivity.class);
                startActivity(newAddressIntent);
            }
        });

        findViewById(R.id.buttonEditPracticeApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newAddressIntent = new Intent(v.getContext(), ApplicationManager.class);
                startActivity(newAddressIntent);
            }
        });
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(intent);
    }

    private void launchMainActivity() {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }

    private void launchPhoneNumberActivity() {
        Intent intent = new Intent(getBaseContext(), PhoneNumberActivity.class);
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


    private void launchEditStoryActivity() {
        String FileID = "1Qspkh5AJ19JwyXmpFS44eTY5DROiijTUfdnZsklKZCM";
        String url = "https://docs.google.com/document/d/"+FileID;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
