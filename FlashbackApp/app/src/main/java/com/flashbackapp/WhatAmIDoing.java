package com.flashbackapp;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class WhatAmIDoing extends Fragment {
    private static final String DEBUG_TAG = "MyActivity";
    private static final String[] INSTANCE_PROJECTION = new String[] {
            CalendarContract.Instances.EVENT_ID,      // 0
            CalendarContract.Instances.BEGIN,         // 1
            CalendarContract.Instances.TITLE          // 2
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_BEGIN_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.what_am_i_doing, container, false);
    }

    public void syncWithGoogleCalendar() {

    }

    @SuppressLint("Recycle")
    private void getEventFromGoogleCalendar() {

        // Specify the date range you want to search for recurring
        // event instances
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2020, 3, 12, 8, 0);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2020, 3, 20, 8, 0);
        long endMillis = endTime.getTimeInMillis();

        ContentResolver cr = Objects.requireNonNull(getActivity()).getContentResolver();

        // The ID of the recurring event whose instances you are searching
        // for in the Instances table
        String selection = CalendarContract.Instances.EVENT_ID + " = ?";
        String[] selectionArgs = new String[] {"207"};

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // Submit the query
        Cursor cur =  cr.query(builder.build(),
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                null);

        assert cur != null;
        while (cur.moveToNext()) {
            String title = null;
            long eventID = 0;
            long beginVal = 0;

            // Get the field values
            eventID = cur.getLong(PROJECTION_ID_INDEX);
            beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
            title = cur.getString(PROJECTION_TITLE_INDEX);

            // Do something with the values.
            Log.i(DEBUG_TAG, "Event:  " + title);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(beginVal);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            Log.i(DEBUG_TAG, "Date: " + formatter.format(calendar.getTime()));
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.get_events_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEventFromGoogleCalendar();
            }
        });
    }
}
