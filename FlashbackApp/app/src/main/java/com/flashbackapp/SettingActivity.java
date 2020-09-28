package com.flashbackapp;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;


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
                onButtonSaveAppWindowClick(v);
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
        Intent intent = new Intent(getBaseContext(), EditStoryActivity.class);
        startActivity(intent);
    }

    private void launchEditPracticeApp() {
        Intent intent = new Intent(getBaseContext(), EditStoryActivity.class);
        startActivity(intent);
    }

    public void onButtonSaveAppWindowClick(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.practice_app, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
        Button btnNewAddPhoneNumber = (Button)popupView.findViewById(R.id.EditApp);
        btnNewAddPhoneNumber.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                // TODO: Save to DB
            }
        });
    }
}
