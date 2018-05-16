package com.ligresoftware.solete.daily;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

public class WidgetServiceDaily extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.i("SOLECITO", "Creo el WidgetServiceDaily ****************************");

        return (new DailyListProvider(this.getApplicationContext(), intent));
    }
}
