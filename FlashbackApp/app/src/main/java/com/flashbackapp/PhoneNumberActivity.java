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
import android.widget.Button;
import android.widget.CheckBox;
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

public class PhoneNumberActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private static final String TAG = "PhoneNumberActivity";
    private List<PhoneNumber> phone_number_by_name = new ArrayList<>();
    private ListView listView;
    ArrayAdapter<PhoneNumber> arrayAdapter;
    boolean is_prime_number_exist = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_numbers);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.listView = findViewById(R.id.listApplication);
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
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, phone_number_by_name);
        this.listView.setAdapter(arrayAdapter);

        for(int i=0;i< phone_number_by_name.size(); i++ )  {
            this.listView.setItemChecked(i,false);
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
                DatabaseReference mPostReference;
                if (name_phone_number.isPrimary()) {
                    mPostReference = mDatabase.child("sos_numbers").child("primary").child(name);
                }
                else {
                    mPostReference = mDatabase.child("sos_numbers").child("others").child(name);
                }
                mPostReference.removeValue();
            }
        }
    }


    private void GetEmergencyNumbers() {
        DatabaseReference primary = mDatabase.child("sos_numbers");
        primary.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                phone_number_by_name.clear();
                is_prime_number_exist = false;

                DataSnapshot primary = dataSnapshot.child("primary");
                DataSnapshot others = dataSnapshot.child("others");
                for (DataSnapshot primaryChildSnapshot : primary.getChildren()) {
                    String name = primaryChildSnapshot.getKey();
                    String phone_number = Objects.requireNonNull(primaryChildSnapshot.child("number").getValue()).toString();
                    PhoneNumber number_object = new PhoneNumber(name, phone_number, true);
                    phone_number_by_name.add(number_object);
                    is_prime_number_exist = true;
                }
                for (DataSnapshot othersChildSnapshot : others.getChildren()) {
                    String name = othersChildSnapshot.getKey();
                    String phone_number = Objects.requireNonNull(othersChildSnapshot.child("number").getValue()).toString();
                    PhoneNumber number_object = new PhoneNumber(name, phone_number, false);
                    phone_number_by_name.add(number_object);
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
        final View popupView = inflater.inflate(R.layout.add_phone_number, null);

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
        Button btnNewAddPhoneNumber = (Button)popupView.findViewById(R.id.addANewPhoneNumber);
        btnNewAddPhoneNumber.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                EditText phoneNameEditText = popupView.findViewById(R.id.phoneName);
                EditText phoneNumberEditText = popupView.findViewById(R.id.phoneNumber);
                CheckBox primeNumberCheckBox = popupView.findViewById(R.id.PrimeNumber);
                String phoneName = phoneNameEditText.getText().toString();
                String phoneNumber = phoneNumberEditText.getText().toString();
                boolean isPrime = primeNumberCheckBox.isChecked();
                if (isPrime) {
                    if (is_prime_number_exist) {
                        showToast(R.string.invalid_prime_number);
                        return;
                    }
                    mDatabase.child("sos_numbers").child("primary").child(phoneName).child("number").setValue(phoneNumber);
                } else {
                    mDatabase.child("sos_numbers").child("others").child(phoneName).child("number").setValue(phoneNumber);
                }
                popupWindow.dismiss();
            }
        });
    }
    private void showToast(int resourceId) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(), resourceId, duration);
        toast.show();
    }

}
