package com.flashbackapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppsActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private static final String TAG = "AppsActivity";
    private List<AppObject> apps = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        findViewById(R.id.BackButtonMain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMainActivity();
            }
        });
        GetPracticeAppName();
    }


    public void launchMainActivity() {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }


    View.OnClickListener getOnClickDoSomething(final AppObject app)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                PracticeTime(app.getAddress());
            }
        };
    }

    private void initListViewData() {
        LinearLayout linear = findViewById(R.id.listApplication);
        linear.removeAllViews();
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        Button[] btn = new Button[apps.size()];
        for (int i = 0; i < apps.size(); i++) {
            LinearLayout ll = new LinearLayout(this);
            ll.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
            linear.addView(ll);
            btn[i] = new Button(getApplicationContext());
            btn[i].setText(apps.get(i).getName());
            btn[i].setTextColor(Color.parseColor("#000000"));
            btn[i].setTextSize(15);
            btn[i].setHeight(150);
            btn[i].setLayoutParams(param);
            btn[i].setPadding(15, 5, 15, 5);
            ll.addView(btn[i]);
            btn[i].setOnClickListener(getOnClickDoSomething(apps.get(i)));
        }
    }

    private void GetPracticeAppName() {
        DatabaseReference practice_apps = mDatabase.child("practice_apps");
        practice_apps.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                apps.clear();
                for (DataSnapshot primaryChildSnapshot : dataSnapshot.getChildren()) {
                    String app_name = primaryChildSnapshot.getKey();
                    String app_address = Objects.requireNonNull(primaryChildSnapshot.getValue()).toString();
                    AppObject app_object = new AppObject(app_name, app_address);
                    apps.add(app_object);
                }
                initListViewData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "listener canceled", databaseError.toException());
            }
        });
    }

    private void PracticeTime(String practiceApp) {
        // This will be the practice app
        Intent intent = getPackageManager().getLaunchIntentForPackage(practiceApp);
        if (intent != null) {
            startActivity(intent);
        }
    }
}
