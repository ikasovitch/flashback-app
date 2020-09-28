package com.flashbackapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

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
    private ListView listView;
    private static final String TAG = "AppsActivity";
    private List<AppObject> apps = new ArrayList<>();
    ArrayAdapter<AppObject> arrayAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.listView = findViewById(R.id.listApplication);
        this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick: " +position);
                AppObject app = (AppObject) listView.getItemAtPosition(position);
                PracticeTime(app.getAddress());
            }
        });
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

    private void initListViewData()  {
        arrayAdapter = new ArrayAdapter<AppObject>(this, android.R.layout.simple_list_item_checked, apps);
        this.listView.setAdapter(arrayAdapter);

        for(int i=0;i< apps.size(); i++ )  {
            this.listView.setItemChecked(i,false);
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
