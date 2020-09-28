package com.flashbackapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
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


public class AddressManagerActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private static final String TAG = "AddressActivity";
    private List<AddressObject> address = new ArrayList<>();
    private ListView listView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address_manager);
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
                AddressObject known_address = (AddressObject) listView.getItemAtPosition(position);
                known_address.setActive(!currentCheck);
            }
        });
        findViewById(R.id.ReturnToMainFromEditStory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchSettingsActivity();
            }
        });

        findViewById(R.id.deleteAddress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteSelectedIteams();
            }
        });

        findViewById(R.id.addAddress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddAddressActivity();
            }
        });

        GetAddress();
    }


    public void launchSettingsActivity() {
        Intent intent = new Intent(getBaseContext(), SettingActivity.class);
        startActivity(intent);
    }

    public void launchAddAddressActivity() {
        Intent intent = new Intent(getBaseContext(), AddAddressActivity.class);
        startActivity(intent);
    }

    private void initListViewData()  {
        ArrayAdapter<AddressObject> arrayAdapter
                = new ArrayAdapter<AddressObject>(this, android.R.layout.simple_list_item_checked, address);

        this.listView.setAdapter(arrayAdapter);

        for(int i=0;i< address.size(); i++ )  {
            this.listView.setItemChecked(i,false);
        }
    }

    // When user click "Print Selected Items".
    public void DeleteSelectedIteams()  {
        SparseBooleanArray sp = listView.getCheckedItemPositions();
        for(int i=0;i<sp.size();i++){
            if(sp.valueAt(i)==true){
                System.out.println("Deleting");
                AddressObject address_object = (AddressObject) listView.getItemAtPosition(i);
                String address = address_object.getAddress();
                System.out.println(address_object.toString());
            }
        }
        // TODO: Delete from db
    }

    private void GetAddress() {
        DatabaseReference known_address = mDatabase.child("known_address");
        known_address.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot primaryChildSnapshot : dataSnapshot.getChildren()) {
                    String known_address = primaryChildSnapshot.getKey();
                    AddressObject known_address_obj = new AddressObject(known_address);
                    address.add(known_address_obj);
                    System.out.println(known_address_obj.toString());
                }
                initListViewData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "listener canceled", databaseError.toException());
            }
        });
    }
}
