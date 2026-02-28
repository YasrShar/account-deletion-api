package com.batteryinsightpro.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.batteryinsightpro.R;

public class BatteryWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_small);
            views.setTextViewText(R.id.widget_level, "--%");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
