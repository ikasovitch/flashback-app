package com.flashbackapp;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.firebase.auth.FirebaseUser;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class WhatAmIDoing extends AppCompatActivity {
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private static final int REQUEST_AUTHORIZATION = 2;
    GoogleAccountCredential credential;
    private static final String ARG_NAME = "username";
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_what_am_i_doing);

        // Google Accounts
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        assert acct != null;
        credential.setSelectedAccountName(acct.getEmail());

        findViewById(R.id.ReturnToMain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                launchMainActivity(user);
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();

        try {
            getEventFromGoogleCalendar();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private void getEventFromGoogleCalendar() throws IOException, GeneralSecurityException {
        // List the next 10 events from the primary calendar.
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, List<Event>> task = new AsyncTask<Void, Void, List<Event>>() {
            @Override
            protected List<Event> doInBackground(Void... params) {
                final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
                Calendar service = new Calendar.Builder(HTTP_TRANSPORT, jsonFactory, credential)
                        .setApplicationName("FlashbackApp")
                        .build();
                DateTime now = new DateTime(System.currentTimeMillis());
                List<Event> items = null;
                try {
                    Events events = service.events().list("primary")
                            .setMaxResults(10)
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
                if (!items.isEmpty()) {
                    TextView events_data_text = findViewById(R.id.events_data_text);
                    System.out.println("Upcoming events");
                    for (Event event : items) {
                        DateTime start = event.getStart().getDateTime();
                        DateTime end = event.getEnd().getDateTime();
                        if (start == null) {
                            start = event.getStart().getDate();
                        }
                        if (end == null) {
                            end = event.getEnd().getDate();
                        }
                        String events_data = "Title: " + event.getSummary() + "\nStart: " + start + "\nEnd: " + end;
                        events_data_text.setText(events_data);
                        events_data_text.setVisibility(View.VISIBLE);
                        System.out.print(events_data);
                    }
                }
            }
        };
        task.execute();
    }

    public void launchMainActivity(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra(ARG_NAME, user.getDisplayName());
            startActivity(intent);
        }
    }
}

