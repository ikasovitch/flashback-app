package com.flashbackapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.api.client.json.JsonFactory;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String ARG_NAME = "username";
    final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private DatabaseReference mDatabase;
    GoogleAccountCredential credential;
    private static final int REQUEST_AUTHORIZATION = 2;
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

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

        // Google Accounts
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        assert acct != null;
        credential.setSelectedAccountName(acct.getEmail());
        firebaseAuth = FirebaseAuth.getInstance();
        setTitle();

        setRepeatingCalenderTask();
        findViewById(R.id.buttonShayStory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchShayStoryActivity();
            }
        });

        findViewById(R.id.ButtonEmergencyNumber).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallInbar();
            }
        });

        Button settingsBtn = findViewById(R.id.ButtonSetting);
        LinearLayout yourRelLay = (LinearLayout) settingsBtn.getParent();
        settingsBtn.setBackground(yourRelLay.getBackground());
        settingsBtn.setOnClickListener(new View.OnClickListener() {
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
    }

    private void PracticeTime() {
        // This will be the practice app
        String packageName = "com.google.android.apps.maps";
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if(intent != null) {
            startActivity(intent);
        }
    }

    private void CalenderTime() {
        String packageName = "com.google.android.calendar";
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if(intent != null) {
            startActivity(intent);
        }
    }

    private void setTitle() {
        TextView titleText = findViewById(R.id.titleText);
        int currentHour = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem")).get(Calendar.HOUR_OF_DAY);
        String titleTextString = "בוקר טוב שי!";
        int color = Color.rgb(136, 211, 211);
        if (currentHour > 12 && currentHour < 18) {
            titleTextString = "צהריים טובים שי!";
            color = Color.rgb(146, 210, 132);
        } else if (currentHour > 18 || currentHour < 6) {
            titleTextString = "ערב טוב שי!";
            color = Color.rgb(112, 106, 200);
        }

        titleText.setTextColor(color);
        titleText.setText(titleTextString);
//        titleText.setTypeface(null, Typeface.BOLD);
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
//
//    private void revokeAccess() {
//        // Firebase sign out
//        firebaseAuth.signOut();
//        // Google revoke access
//        googleSignInClient.revokeAccess().addOnCompleteListener(this,
//                new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        // Google Sign In failed, update UI appropriately
//                        Log.w(TAG, "Revoked Access");
//                    }
//                });
//        launchLoginActivity();
//    }

    private void launchShayStoryActivity() {
        Intent intent = new Intent(getBaseContext(), ViewStoryActivity.class);
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

    private void CallInbar() {
        SMSInbar();
        String phoneNumber = "+972545789677";
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+phoneNumber));
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }

    private void SMSInbar() {
        String phoneNumber = "+972545789677";
        String content = "Hi!";
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, content, null, null);
        Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show();
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


    private void setRepeatingCalenderTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            CalenderWorker calenderTask = new CalenderWorker();
                            calenderTask.execute();
                        } catch (Exception e) {
                            // error, do something
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, 60*1000);  // interval of one minute
    }

    private class CalenderWorker extends AsyncTask<Void, Void, List<Event>> {
        // List the next 1 events from the primary calendar.
            @Override
            protected List<Event> doInBackground(Void... params) {
                final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
                com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, jsonFactory, credential)
                        .setApplicationName("FlashbackApp")
                        .build();
                DateTime now = new DateTime(System.currentTimeMillis());
                List<Event> items = null;
                try {
                    Events events = service.events().list("primary")
                            .setMaxResults(1)
                            .setTimeMin(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                    items = events.getItems();
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return items;
            }

            @Override
            protected void onPostExecute(List<Event> items) {
                Button meetingsManager = findViewById(R.id.MeetingsManager);
                boolean is_practice_time = true;
                if (items != null && !items.isEmpty()) {
                    Event event = items.get(0);
                    long start = event.getStart().getDateTime().getValue();
                    long end = event.getEnd().getDateTime().getValue();
                    long now = new DateTime(System.currentTimeMillis()).getValue();
                    String events_data = event.getSummary();

                    System.out.print(events_data);
                    if (now > start && now < end) {
                        long totalTimeMinutes = (end - now) / 60000;
                        String meetingText = "נותרו עוד " +  totalTimeMinutes + " דקות לפגישת " + events_data;
                        meetingsManager.setText(meetingText);
                        is_practice_time = false;
                    } else if(now < start && ((start - now) / 60000) < 60) {
                        long totalTimeMinutes = (start - now) / 60000;
                        String meetingText = "בעוד " + totalTimeMinutes +  " פגישת דקות " + events_data;
                        meetingsManager.setText(meetingText);
                        is_practice_time = false;
                    }
                }

                if (is_practice_time) {
                    meetingsManager.setText("זמן טוב להתאמן");
                    findViewById(R.id.MeetingsManager).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PracticeTime();
                        }
                    });
                } else {
                    findViewById(R.id.MeetingsManager).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CalenderTime();
                        }
                    });
                }
            }
        }
}
