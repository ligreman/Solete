package com.ligresoftware.solete.hourly;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.reflect.TypeToken;
import com.ligresoftware.solete.R;
import com.ligresoftware.solete.utils.CacheManager;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.ligresoftware.solete.widget.HomeWidget.FILE_CACHE_HOURLY;

public class HourlyListProvider implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<HourlyListItem> listItemList = new ArrayList<>();
    private Context mContext = null;
    private int appWidgetId;
    private CacheManager cacheManager;

    public HourlyListProvider(Context context, Intent intent) {
        this.mContext = context;
        appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.i("SOLECITO", "Creo el HourlyListProvider WidgetFactory ****************************");

        if (cacheManager == null) {
            cacheManager = new CacheManager(context);
        }

//        populateListItem(null);
    }

    private void populateListItem(final ArrayList<HourlyListItem> items) {
        Log.i("SOLECITO", "Toy populateListItem ****************************");

        if (!items.isEmpty()) {
            listItemList.clear();

            // Simplemente guardo cada item en la lista de items
            listItemList.addAll(items);
        }
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        Log.i("SOLECITO", "onDataSetChanged ****************************");

        // Cojo los datos
        Type type = new TypeToken<ArrayList<HourlyListItem>>() {
        }.getType();
        ArrayList<HourlyListItem> items = (ArrayList<HourlyListItem>) cacheManager.readJson(type, FILE_CACHE_HOURLY);

        populateListItem(items);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                mContext.getPackageName(), R.layout.hourly_list_row);

        HourlyListItem listItem = (HourlyListItem) listItemList.get(position);
//        remoteView.setTextViewText(R.id.heading, listItem.heading);
//        remoteView.setTextViewText(R.id.content, listItem.content);

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
