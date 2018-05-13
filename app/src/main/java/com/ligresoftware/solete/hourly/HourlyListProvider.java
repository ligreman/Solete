package com.ligresoftware.solete.hourly;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.reflect.TypeToken;
import com.ligresoftware.solete.R;
import com.ligresoftware.solete.WeatherListItem;
import com.ligresoftware.solete.utils.CacheManager;
import com.ligresoftware.solete.utils.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ligresoftware.solete.widget.HomeWidget.FILE_CACHE_HOURLY;

public class HourlyListProvider implements RemoteViewsService.RemoteViewsFactory {
    private List<WeatherListItem> listItemList = new ArrayList<>();
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

    /*private void populateListItem(final List<WeatherListItem> items) {
        Log.i("SOLECITO", "Toy populateListItem ****************************");

        if (!items.isEmpty()) {
            listItemList.clear();

            // Simplemente guardo cada item en la lista de items
            listItemList.addAll(items);
        }
    }*/

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        Log.i("SOLECITO", "onDataSetChanged ****************************");

        // Cojo los datos Horarios
        Type type = new TypeToken<ArrayList<WeatherListItem>>() {
        }.getType();
        List<WeatherListItem> items = (ArrayList<WeatherListItem>) cacheManager.readJson(type, FILE_CACHE_HOURLY);
        if (!items.isEmpty()) {

            // Ordeno por timestamps
            Collections.sort(items);

            Log.i("SOLECITO", "Imprimo algunos resultados");
            for (int i = 0; i < items.size(); i++) {
                WeatherListItem item = items.get(i);
                Log.i("SOLECITO", "Time: " + item.getTimestamp());
                Log.i("SOLECITO", "Estado: " + item.getEstado());
                Log.i("SOLECITO", "Temp: " + item.getTemperatura());
            }

            listItemList.clear();

            // Simplemente guardo cada item en la lista de items
            listItemList.addAll(items);
        }
//        populateListItem(items);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    /**
     * Construye la vista con cada elemento
     *
     * @param position
     * @return
     */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews rw = new RemoteViews(mContext.getPackageName(), R.layout.hourly_list_row);

        WeatherListItem listItem = (WeatherListItem) listItemList.get(position);

        // La temperatura
        rw.setTextViewText(R.id.hourlyTemperature, listItem.getTemperatura());
        rw.setTextViewText(R.id.hourlyHour, listItem.getHora());
        rw.setTextViewText(R.id.hourlyDate, listItem.getFecha());
        rw.setTextViewText(R.id.hourlyWind, listItem.getVientoVelocidad());
        rw.setImageViewResource(R.id.hourlyStatusIcon, Utils.getStatusIcon(listItem.getEstado()));

        // Si hay más nieve que lluvia o viceversa pongo un valor u otro
        if (Integer.parseInt(listItem.getPrecipitacion()) >= Integer.parseInt(listItem.getNieve())) {
            rw.setTextViewText(R.id.hourlyRainSnow, listItem.getPrecipitacion());
            rw.setImageViewResource(R.id.hourlyRainSnowIcon, R.drawable.ic_drop);
        } else {
            rw.setTextViewText(R.id.hourlyRainSnow, listItem.getNieve());
//            rw.setImageViewResource(R.id.hourlyRainSnowIcon, R.drawable.ic_flake);
        }

        /*if (Integer.parseInt(listItem.getTemperatura()) > 11) {

            rw.setImageViewResource(R.id.hourlyRainSnowIcon, R.drawable.id_lluvia_w);
        } else {
            rw.setImageViewResource(R.id.hourlyRainSnowIcon, R.drawable.ic_clear);
        }*/
//        remoteView.setTextViewText(R.id.heading, listItem.heading);
//        remoteView.setTextViewText(R.id.content, listItem.content);

        return rw;
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
