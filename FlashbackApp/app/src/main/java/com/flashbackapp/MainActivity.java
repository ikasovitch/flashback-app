package com.flashbackapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.flashbackapp.data.CurrentLocation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "MainActivity";
    private static final String ARG_NAME = "username";
    private DatabaseReference mDatabase;
    GoogleAccountCredential credential;
    private static final int REQUEST_AUTHORIZATION = 2;
    private static final int CALL_SMS_PERMISSION_REQUESTS=3;
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;

    // Locations properties
    protected LocationManager locationManager;

    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int METERS_THRESHOLD = 50;

    private Location currentLocation;
    private String closestLocation;
    Map.Entry<String, Object> closestLocationWithCoordinates;
    private String practiceApp;
    private DatabaseReference locationsDatabase;
    String email = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions for emergency call and SMS
        checkPhoneCallAndSmsPermissions();

        // Google Accounts
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        assert acct != null;
        email = acct.getEmail();
        credential.setSelectedAccountName(acct.getEmail());
        firebaseAuth = FirebaseAuth.getInstance();
        setTitle();

        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            Location();
        }

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
                SMSEmergencyNumbers();
            }
        });

        findViewById(R.id.ButtonLocations).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (closestLocation == null)
                    return;

                Double latitude = (Double) ((HashMap)closestLocationWithCoordinates.getValue()).get("latitude");
                Double longitude = (Double) ((HashMap)closestLocationWithCoordinates.getValue()).get("longitude");

                float[] results = new float[3];
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), latitude, longitude, results);
                float distanceInMeters = results[0];

                if (distanceInMeters <= 500){
                    Uri gmmIntentUri = Uri.parse(String.format("google.navigation:q=%s,%s&mode=walking", latitude, longitude));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                } else {
                    showToast(R.string.too_far_cant_navigate);
                }
            }
        });

        Button settingsBtn = findViewById(R.id.ButtonSetting);
        LinearLayout yourRelLay = (LinearLayout) settingsBtn.getParent();
        settingsBtn.setBackground(yourRelLay.getBackground());
        if (email.equals("shaimishaly93@gmail.com")) {
            settingsBtn.setVisibility(View.GONE);
        } else {
            settingsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchShaySettingActivity();
                }
            });
        }

        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        setRepeatingCalenderTask();

        String method = getIntent().getStringExtra("method_name");
        if (method != null && method.equals("emergency_call")) {
            CallEmergencyNumber();
            SMSEmergencyNumbers();
        }

        if (method != null && method.equals("story")) {
            launchShayStoryActivity();
        }

    }

    private void showToast(int resourceId) {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(getApplicationContext(), resourceId, duration);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(25);
        toast.show();
    }

    // Location methods
    @SuppressLint("MissingPermission")
    private void Location() {
        locationsDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl("https://shai-mfh-3586.firebaseio.com");

        if (!canAccessLocation()) {
            requestPermissions(LOCATION_PERMISSIONS, INITIAL_REQUEST);
        }

        currentLocation = new Location ("currentLocation");
        CurrentLocation.setCurrentLocation(currentLocation);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);

        updateClosestLocation();
        printClosetsLocation();
    }

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
    }

    private void updateClosestLocation() {
        DatabaseReference addressesRef = locationsDatabase.child("known_address");
        addressesRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        updateClosestLocation((Map<String, Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "listener canceled", databaseError.toException());
                    }
                }
        );
        System.out.println(closestLocation);
    }

    public void printClosetsLocation() {
        TextView txtLat = findViewById(R.id.ButtonLocations);
        if (closestLocation != null) {
            txtLat.setText("אני נמצא ב" + closestLocation);
        }
    }

    public void onLocationChanged(Location location) {
        currentLocation.setLatitude(location.getLatitude());
        currentLocation.setLongitude(location.getLongitude());
        CurrentLocation.setCurrentLocation(currentLocation);

        updateClosestLocation();
        printClosetsLocation();
    }

    private void updateClosestLocation(Map<String, Object> addresses) {
        double minimalDistance = 0;
        String minimalLocation = null;
        for (Map.Entry<String, Object> entry: addresses.entrySet()) {
            Map singleAddress = (Map) entry.getValue();
            Location addressLocation = new Location("addressLocation");
            addressLocation.setLatitude(new Double(singleAddress.get("latitude").toString()));
            addressLocation.setLongitude(new Double(singleAddress.get("longitude").toString()));
            double distance = addressLocation.distanceTo(currentLocation);
            if (minimalLocation == null || distance < minimalDistance) {
                minimalLocation = entry.getKey();
                closestLocationWithCoordinates = entry;
                minimalDistance = distance;
            }
        }
        if (minimalDistance < METERS_THRESHOLD) {
            closestLocation = minimalLocation;
        } else {
            try {
                closestLocation = getAddress(currentLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getAddress(Location location) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.forLanguageTag("he"));
        List<Address> addresses  = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(), 1);
        if(addresses.size() == 0){
            return "...";
        }
        String address = addresses.get(0).getAddressLine(0);
        return address;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    private void PracticeTime() {
        Intent intent = new Intent(getBaseContext(), AppsActivity.class);
        startActivity(intent);
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
        } else if (currentHour > 17 || currentHour < 6) {
            titleTextString = "ערב טוב שי!";
            color = Color.rgb(112, 106, 200);
        }

        titleText.setTextColor(color);
        titleText.setText(titleTextString);
    }


    // Function to convert ArrayList<String> to String[]
    public static String[] GetStringArray(ArrayList<String> arr)
    {

        // declaration and initialise String Array
        String str[] = new String[arr.size()];

        // Convert ArrayList to object array
        Object[] objArr = arr.toArray();

        // Iterating and converting to String
        int i = 0;
        for (Object obj : objArr) {
            str[i++] = (String)obj;
        }

        return str;
    }
    private void checkPhoneCallAndSmsPermissions(){
        ArrayList<String> permissions = new ArrayList<String>();
        if (!hasPermission(Manifest.permission.CALL_PHONE)) {
            permissions.add(Manifest.permission.CALL_PHONE);
        }
        if (!hasPermission(Manifest.permission.SEND_SMS)) {
            permissions.add(Manifest.permission.SEND_SMS);
        }
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!hasPermission(Manifest.permission.READ_CALENDAR)) {
            permissions.add(Manifest.permission.READ_CALENDAR);
        }
        if (permissions.size()>0) {
            requestPermissions(GetStringArray(permissions), CALL_SMS_PERMISSION_REQUESTS);
        }
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

    private void launchShayStoryActivity() {
        Intent intent = new Intent(getBaseContext(), ViewStoryActivity.class);
        startActivity(intent);
    }

    private void launchShaySettingActivity() {
        Intent intent = new Intent(getBaseContext(), SettingActivity.class);
        startActivity(intent);
    }

    private void CallEmergencyNumber() {
        DatabaseReference primary = mDatabase.child("sos_numbers").child("primary");
        primary.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String name = childSnapshot.getKey();
                    String phone_number = Objects.requireNonNull(childSnapshot.child("number").getValue()).toString();
//                    String image = Objects.requireNonNull(childSnapshot.child("picture").getValue()).toString();
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + phone_number));
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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


    private void SMSEmergencyNumbers() {
        final String content = getResources().getString(R.string.emergency_sms_content);
        DatabaseReference primary = mDatabase.child("sos_numbers").child("primary");
        DatabaseReference others = mDatabase.child("sos_numbers").child("others");

        // send SMS to primary
        primary.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    String phoneNumber = Objects.requireNonNull(childSnapshot.child("number").getValue()).toString();
                    String locationInMaps = String.format("https://www.google.com/maps/search/?api=1&query=%s,%s",
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude());
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, content + locationInMaps, null, null);
                    // ...
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "listener canceled", databaseError.toException());
            }
        });

        // send SMS to others
        others.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    String phoneNumber = Objects.requireNonNull(childSnapshot.child("number").getValue()).toString();
                    String locationInMaps = String.format("https://www.google.com/maps/search/?api=1&query=%s,%s",
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude());
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, content + locationInMaps, null, null);
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
                    DateTime startDateTime = event.getStart().getDateTime();
                    if (startDateTime == null) {
                        startDateTime = event.getStart().getDate();
                    }
                    DateTime endDateTime = event.getEnd().getDateTime();
                    if (endDateTime == null) {
                        endDateTime = event.getEnd().getDate();
                    }
                    long start = startDateTime.getValue();
                    long end = endDateTime.getValue();
                    long now = new DateTime(System.currentTimeMillis()).getValue();
                    String events_data = event.getSummary();

                    System.out.print(events_data);
                    if (now > start && now < end) {
                        long totalTimeMinutes = (end - now) / 60000;
                        String meetingText = "נותר יותר משעה ל" + events_data;
                        if (totalTimeMinutes < 60) {
                            meetingText = "נותרו עוד " + totalTimeMinutes + " דקות ל" + events_data;
                        }
                        meetingsManager.setText(meetingText);
                        is_practice_time = false;
                    } else if(now < start && ((start - now) / 60000) < 60) {
                        long totalTimeMinutes = (start - now) / 60000;
                        String meetingText = "בעוד " + totalTimeMinutes +  " דקות " + events_data;
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
