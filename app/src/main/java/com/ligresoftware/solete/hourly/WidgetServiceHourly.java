package com.ligresoftware.solete.hourly;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

public class WidgetServiceHourly extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.i("SOLECITO", "Creo el WidgetServiceHourly ****************************");

        return (new HourlyListProvider(this.getApplicationContext(), intent));
    }
}
