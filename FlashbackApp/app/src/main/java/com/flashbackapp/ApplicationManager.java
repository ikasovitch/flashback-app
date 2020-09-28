package com.flashbackapp;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApplicationManager extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private static final String TAG = "ApplicationManagerActivity";
    private List<AppObject> apps = new ArrayList<>();
    private ListView listView;
    ArrayAdapter<AppObject> arrayAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.listView = findViewById(R.id.listApplication);
        this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick: " + position);
                CheckedTextView v = (CheckedTextView) view;
                boolean currentCheck = v.isChecked();
                AppObject user = (AppObject) listView.getItemAtPosition(position);
                user.setActive(!currentCheck);
            }
        });
        findViewById(R.id.ReturnToMainFromEditStory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchSettingsActivity();
            }
        });

        findViewById(R.id.deleteNumbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteSelectedIteams();
            }
        });

        findViewById(R.id.addAppAddress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonAddNumberWindowClick(v);
            }
        });
        GetEmergencyNumbers();
    }

    public void launchSettingsActivity() {
        Intent intent = new Intent(getBaseContext(), SettingActivity.class);
        startActivity(intent);
    }

    private void initListViewData() {
        arrayAdapter = new ArrayAdapter<AppObject>(this, android.R.layout.simple_list_item_checked, apps);
        this.listView.setAdapter(arrayAdapter);

        for (int i = 0; i < apps.size(); i++) {
            this.listView.setItemChecked(i, false);
        }
    }

    // When user click "Print Selected Items".
    public void DeleteSelectedIteams() {
        SparseBooleanArray sp = listView.getCheckedItemPositions();
        for (int i = 0; i < sp.size(); i++) {
            if (sp.valueAt(i) == true) {
                System.out.println("Deleting");
                AppObject apps = (AppObject) listView.getItemAtPosition(i);
                String name = apps.getName();
                DatabaseReference mPostReference;
                mPostReference = mDatabase.child("practice_apps").child(name);
                mPostReference.removeValue();
            }
        }
    }


    private void GetEmergencyNumbers() {
        DatabaseReference primary = mDatabase.child("practice_apps");
        primary.addValueEventListener(new ValueEventListener() {
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

    public void onButtonAddNumberWindowClick(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.app_popup, null);

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
        Button btnAppAddress = popupView.findViewById(R.id.addANewApp);
        btnAppAddress.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText appAddress = popupView.findViewById(R.id.appAddress);
                String address = appAddress.getText().toString();
                EditText appName = popupView.findViewById(R.id.appName);
                String name = appName.getText().toString();
                if (!isPackageExisted(address)) {
                    showToast(R.string.invalid_app);
                    return;
                }
                mDatabase.child("practice_apps").child(name).setValue(address);
                popupWindow.dismiss();
            }
        });
    }

    private void showToast(int resourceId) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(), resourceId, duration);
        toast.show();
    }

    public boolean isPackageExisted(String targetPackage){
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;
    }
}