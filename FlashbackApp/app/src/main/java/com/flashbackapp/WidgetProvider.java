package com.flashbackapp;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
    private static final String EmergencyCallClick = "EmergencyCallClick";
    private static final String StoryClick = "StoryClick";
    private static final String OpenApp = "OpenApp";


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context,
                WidgetProvider.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setOnClickPendingIntent(R.id.ButtonEmergencyNumber, getPendingSelfIntent(context, EmergencyCallClick));
        views.setOnClickPendingIntent(R.id.buttonShayStory, getPendingSelfIntent(context, StoryClick));
        views.setOnClickPendingIntent(R.id.titleLayout, getPendingSelfIntent(context, OpenApp));
        appWidgetManager.updateAppWidget(thisWidget, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (EmergencyCallClick.equals(intent.getAction())){
            Log.w("Widget", "EmergencyCallClick");
            Intent mainIntent = new Intent(context, LoginActivity.class);
            mainIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
            mainIntent.putExtra("method_name","emergency_call");
            context.startActivity(mainIntent);
        }
        else if (StoryClick.equals(intent.getAction())) {
            Log.w("Widget", "StoryClick");
            Intent mainIntent = new Intent(context, LoginActivity.class);
            mainIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
            mainIntent.putExtra("method_name","story");
            context.startActivity(mainIntent);
        }
        else if (OpenApp.equals(intent.getAction())) {
            Log.w("Widget", "OpenApp");
            Intent mainIntent = new Intent(context, LoginActivity.class);
            mainIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainIntent);
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}