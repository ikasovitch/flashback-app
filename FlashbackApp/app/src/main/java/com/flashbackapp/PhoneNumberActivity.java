package com.flashbackapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhoneNumberActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private static final String TAG = "PhoneNumberActivity";
    private List<PhoneNumber> phone_number_by_name = new ArrayList<>();
    private ListView listView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_numbers);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.listView = findViewById(R.id.listApplication);

        // CHOICE_MODE_NONE: (Default)
        // (listView.setItemChecked(..) doest not work with CHOICE_MODE_NONE).
        // CHOICE_MODE_SINGLE:
        // CHOICE_MODE_MULTIPLE:
        // CHOICE_MODE_MULTIPLE_MODAL:
        this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick: " +position);
                CheckedTextView v = (CheckedTextView) view;
                boolean currentCheck = v.isChecked();
                PhoneNumber user = (PhoneNumber) listView.getItemAtPosition(position);
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

        findViewById(R.id.addNumber).setOnClickListener(new View.OnClickListener() {
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

    private void initListViewData()  {
        ArrayAdapter<PhoneNumber> arrayAdapter
                = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, phone_number_by_name);

        this.listView.setAdapter(arrayAdapter);

        for(int i=0;i< phone_number_by_name.size(); i++ )  {
            this.listView.setItemChecked(i,false);
            System.out.println(phone_number_by_name.get(i).toString());
        }
    }

    // When user click "Print Selected Items".
    public void DeleteSelectedIteams()  {
        SparseBooleanArray sp = listView.getCheckedItemPositions();
        for(int i=0;i<sp.size();i++){
            if(sp.valueAt(i)==true){
                System.out.println("Deleting");
                PhoneNumber name_phone_number = (PhoneNumber) listView.getItemAtPosition(i);
                String name = name_phone_number.getName();
                System.out.println(name_phone_number.toString());
            }
        }
       // To DO Delete from db
    }


    private void GetEmergencyNumbers() {
        DatabaseReference primary = mDatabase.child("sos_numbers");
        primary.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot primary = dataSnapshot.child("primary");
                DataSnapshot others = dataSnapshot.child("others");
                for (DataSnapshot primaryChildSnapshot : primary.getChildren()) {
                    String name = primaryChildSnapshot.getKey();
                    String phone_number = Objects.requireNonNull(primaryChildSnapshot.child("number").getValue()).toString();
                    PhoneNumber number_object = new PhoneNumber(name, phone_number, true);
                    phone_number_by_name.add(number_object);
                    System.out.println(number_object.toString());
                }
                for (DataSnapshot othersChildSnapshot : others.getChildren()) {
                    String name = othersChildSnapshot.getKey();
                    String phone_number = Objects.requireNonNull(othersChildSnapshot.child("number").getValue()).toString();
                    PhoneNumber number_object = new PhoneNumber(name, phone_number, false);
                    phone_number_by_name.add(number_object);
                    System.out.println(number_object.toString());
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
        View popupView = inflater.inflate(R.layout.add_phone_number, null);

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
    }
}
