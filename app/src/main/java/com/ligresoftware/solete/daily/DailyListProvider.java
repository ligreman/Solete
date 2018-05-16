package com.ligresoftware.solete.daily;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
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

import static com.ligresoftware.solete.widget.HomeWidget.FILE_CACHE_DAILY;

public class DailyListProvider implements RemoteViewsService.RemoteViewsFactory {
    private List<WeatherListItem> listItemList = new ArrayList<>();
    private Context mContext = null;
    private int appWidgetId;
    private CacheManager cacheManager;

    public DailyListProvider(Context context, Intent intent) {
        this.mContext = context;
        appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.i("SOLECITO", "Creo el DailyListProvider WidgetFactory ****************************");

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
        Log.i("SOLECITO", "onDataSetChanged daily ****************************");

        // Cojo los datos Horarios
        Type type = new TypeToken<ArrayList<WeatherListItem>>() {
        }.getType();
        List<WeatherListItem> items = (ArrayList<WeatherListItem>) cacheManager.readJson(type, FILE_CACHE_DAILY);
        if (!items.isEmpty()) {

            // Ordeno por timestamps
            Collections.sort(items);

            /*Log.i("SOLECITO", "Imprimo algunos resultados");
            for (int i = 0; i < items.size(); i++) {
                WeatherListItem item = items.get(i);
                Log.i("SOLECITO", "Time: " + item.getTimestamp());
                Log.i("SOLECITO", "Estado: " + item.getEstado());
                Log.i("SOLECITO", "Temp: " + item.getTemperatura());
            }*/

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
     */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews rw = new RemoteViews(mContext.getPackageName(), R.layout.daily_list_row);

        WeatherListItem listItem = listItemList.get(position);

        // La temperatura
        rw.setTextViewText(R.id.dailyTemperatureMin, listItem.getTemperaturaMin());
        rw.setTextViewText(R.id.dailyTemperatureMax, listItem.getTemperaturaMax());
        rw.setTextViewText(R.id.dailyDate, listItem.getFecha());
        rw.setTextViewText(R.id.dailyWind, listItem.getVientoVelocidad());
        rw.setImageViewResource(R.id.dailyStatusIcon, Utils.getStatusIcon(listItem.getEstado()));

        // En este caso la precipitación es % y no valor absoluto
        // Trato los floats
        Integer precipitacion = 0;
        if (!listItem.getPrecipitacion().equals("")) {
            try {
                precipitacion = Integer.parseInt(listItem.getPrecipitacion());
            } catch (NumberFormatException e) {
                //No es un float por lo que dejo 0
                precipitacion = 0;
            }
        }

        if (precipitacion == 0) {
            rw.setViewVisibility(R.id.dailyRainContainer, View.INVISIBLE);
        } else {
            rw.setViewVisibility(R.id.dailyRainContainer, View.VISIBLE);
            rw.setTextViewText(R.id.dailyRain, precipitacion.toString() + "%");
        }

        // No voy a pintar la cota de nieve en el widget
        rw.setViewVisibility(R.id.dailySnowContainer, View.INVISIBLE);
        // Si hay cota de nieve la pinto y si no oculto
        /*if (listItem.getCotaNieve().equals("")) {
            rw.setViewVisibility(R.id.dailySnowContainer, View.INVISIBLE);
        } else {
            rw.setViewVisibility(R.id.dailySnowContainer, View.VISIBLE);
            rw.setTextViewText(R.id.dailySnow, listItem.getCotaNieve());
        }*/

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
