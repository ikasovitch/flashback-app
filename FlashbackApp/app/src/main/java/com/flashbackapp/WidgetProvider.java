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
    private static final String PracticeClick = "PracticeClick";


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                WidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            // Create an Intent to launch LoginActivity
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

            Intent intent = new Intent(context, LoginActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.layout, pendingIntent);

            Intent EmergencyIntent = new Intent(context, LoginActivity.class);
            EmergencyIntent.putExtra("method_name", "emergency_call");
            PendingIntent EmergencyPendingIntent = PendingIntent.getActivity(context, 0, EmergencyIntent,  PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.ButtonEmergencyNumber, EmergencyPendingIntent);

//            views.setOnClickPendingIntent(R.id.ButtonEmergencyNumber, getPendingSelfIntent(context, EmergencyCallClick));
//            views.setOnClickPendingIntent(R.id.MeetingsManager, getPendingSelfIntent(context, PracticeClick));
            appWidgetManager.updateAppWidget(thisWidget, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (EmergencyCallClick.equals(intent.getAction())){
            //your onClick action is here
            Log.w("Widget", "EmergencyCallClick");
            Intent mainIntent = new Intent(context, LoginActivity.class);
            mainIntent.putExtra("methodName","emergency_call");

        }
        else if (PracticeClick.equals(intent.getAction())) {
            Log.w("Widget", "PracticeClick");
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}