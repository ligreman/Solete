package com.ligresoftware.solete.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.reflect.TypeToken;
import com.ligresoftware.solete.MainActivity;
import com.ligresoftware.solete.R;
import com.ligresoftware.solete.WeatherListItem;
import com.ligresoftware.solete.daily.WidgetServiceDaily;
import com.ligresoftware.solete.hourly.WidgetServiceHourly;
import com.ligresoftware.solete.utils.CacheManager;
import com.ligresoftware.solete.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link HomeWidgetConfigureActivity HomeWidgetConfigureActivity}
 */
public class HomeWidget extends AppWidgetProvider {
    public static Integer idMunicipio;

    public static final String TAG = "SoleteTag";
    public static final String FILE_CACHE_TODAY = "today.json";
    public static final String FILE_CACHE_DAILY = "daily.json";
    public static final String FILE_CACHE_HOURLY = "hourly.json";
    private static final String OPENDATA_URI = "https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/";
    private static final String OPENDATA_DAILY = "diaria/";
    private static final String OPENDATA_HOURLY = "horaria/";
    private static final String API_KEY = "?api_key=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsb3ZlaGluYWVzcEBnbWFpbC5jb20iLCJqdGkiOiIwMDlmYzZlNy1iZmMxLTRhZDItOTA2ZC0wZmUzODUzZDg1OTkiLCJpc3MiOiJBRU1FVCIsImlhdCI6MTUyMTg5OTcyOCwidXNlcklkIjoiMDA5ZmM2ZTctYmZjMS00YWQyLTkwNmQtMGZlMzg1M2Q4NTk5Iiwicm9sZSI6IiJ9.WIFq7E8N6Q2ZisfTvbxJfd9ZiVNrUKQlQ_y99aQ_Vrs";

    static RequestQueue queue = null;
    static WeatherListItem today = null;

    // Codigos provincia y municipio
    // http://www.ine.es/daco/daco42/codmun/codmunmapa.htm
    // https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/diaria/24089
    // https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/horaria/24089

    /**
     * Cuando se actualiza el widget, cada vez que salta el updatePeriodMillis
     */
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        Log.i("SOLECITO", "************** onUpdate - hay estos ids de widget: " + appWidgetIds.length);
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            getPredictionsAndUpdate(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Al eliminar el widget
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            HomeWidgetConfigureActivity.deleteCProvPref(context, appWidgetId);
            HomeWidgetConfigureActivity.deleteCMunPref(context, appWidgetId);
        }
    }

    /**
     * Cuando recibe un evento (por ejemplo enviado desde el receiver de AndroidManifest.
     * El AppWidgetProvider es un BroadcastReceiver
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("SOLECITO", "************** onReceive");
        // Si la acción del intent es la de terminado el boot del móvil
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            Log.i("SOLECITO", "************** onReceive - Boot completed");

            // Cojo el manager de widgets
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            // Lista de los widgets que tengo en home
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

            // Por cada uno, lanzo la actualización como si fuera el onUpdate
            for (int appWidgetId : appWidgetIds) {
                getPredictionsAndUpdate(context, appWidgetManager, appWidgetId);
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Log.i("SOLECITO", "************** onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Log.i("SOLECITO", "************** onDisabled");
    }

    /**
     * Consulta las predicciones y actualiza el widget. Son 4 consultas:
     * XMLs diarios -> datos diarios
     * XMLs horarios -> datos horarios
     */
    static void getPredictionsAndUpdate(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        Log.i("SOLECITO", "Toy updateando ****************************");

        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.home_widget);

        // Click sobre el widget, voy a la app
        Intent launchAppIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingActivity = PendingIntent.getActivity(context, 0, launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.currentLayout, pendingActivity);

        // Preferencias del widget
        CharSequence cprovText = HomeWidgetConfigureActivity.loadCProvPref(context, appWidgetId);
        CharSequence cmunText = HomeWidgetConfigureActivity.loadCMunvPref(context, appWidgetId);

        // Petición API. Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(context);

        // Pido datos y luego actualizaré
        if (!cprovText.equals("") && !cmunText.equals("")) {
            idMunicipio = Integer.parseInt("" + cprovText + cmunText);
            Log.i("SOLECITO", "Codigo municipio: " + idMunicipio);

            // Creo un objeto nuevo para los datos actuales
            today = new WeatherListItem();

            // Paso a obtener el xml diario
            getDailyXML(context, appWidgetManager, appWidgetId, views);
        }
    }

    /**
     * Obtengo el xml de los datos diarios
     */
    public static void getDailyXML(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views) {
        String url = OPENDATA_URI + OPENDATA_DAILY + idMunicipio.toString() + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Si no es un 200 algo pasó
                        try {
                            Log.i("SOLECITO", "Pido los XML diarios");
                            Log.i("SOLECITO", response.toString());
                            if (response.getInt("estado") == 200) {
                                // Recojo la url a llamar
                                String urlData = response.getString("datos");

                                Log.i("SOLECITO", "URL obtenida: " + urlData);

                                // Cojo estos datos
                                getDailyData(context, appWidgetManager, appWidgetId, views, urlData);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SOLECITO", error.toString());
                    }
                });

        // Set the tag on the request.
        jsonObjectRequest.setTag(TAG);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    /**
     * Obtengo los datos diarios
     */
    public static void getDailyData(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views, final String urlData) {
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, urlData, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    // Cojo el objeto json que incluye el array
                    JSONObject myJson = response.getJSONObject(0);

                    Log.i("SOLECITO", "Pido los datos diarios");
//                    Log.i("SOLECITO", myJson.toString());
                    if (myJson.getInt("id") == idMunicipio) {
                        // Recojo la información diaria
                        List<WeatherListItem> listaDiaria = parseDailyInfo(myJson.getJSONObject("prediccion"));

                        // Guardo en caché los datos
                        Type type = new TypeToken<List<WeatherListItem>>() {
                        }.getType();
                        CacheManager cacheManager = new CacheManager(context);
                        cacheManager.writeJson(listaDiaria, type, FILE_CACHE_DAILY);

                        // Cojo los datos de horas
                        getHourlyXML(context, appWidgetManager, appWidgetId, views);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SOLECITO", error.toString());
                    }
                });

        // Set the tag on the request.
        jsonObjectRequest.setTag(TAG);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    /**
     * Obtengo el xml de datos horarios
     */
    public static void getHourlyXML(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views) {
        String url = OPENDATA_URI + OPENDATA_HOURLY + idMunicipio.toString() + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Si no es un 200 algo pasó
                        try {
                            Log.i("SOLECITO", "Pido los XML horarios");
                            Log.i("SOLECITO", response.toString());
                            if (response.getInt("estado") == 200) {
                                // Recojo la url a llamar
                                String urlData = response.getString("datos");
                                Log.i("SOLECITO", "URL obtenida: " + urlData);

                                // Cojo estos datos
                                getHourlyData(context, appWidgetManager, appWidgetId, views, urlData);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SOLECITO", error.toString());
                    }
                });

        // Set the tag on the request.
        jsonObjectRequest.setTag(TAG);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    /**
     * Obtengo los datos horarios
     */
    public static void getHourlyData(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views, final String urlData) {
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, urlData, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                // Si no es un 200 algo pasó
                try {
                    // Cojo el objeto json que incluye el array
                    JSONObject myJson = response.getJSONObject(0);

                    Log.i("SOLECITO", "Pido los datos horarios");
//                    Log.i("SOLECITO", myJson.toString());
                    if (myJson.getInt("id") == idMunicipio) {
                        // Recojo la información horaria
                        List<WeatherListItem> listaHoraria = parseHourlyInfo(myJson.getJSONObject("prediccion"));

                        // Guardo en caché los datos
                        Type type = new TypeToken<List<WeatherListItem>>() {
                        }.getType();
                        CacheManager cacheManager = new CacheManager(context);
                        cacheManager.writeJson(listaHoraria, type, FILE_CACHE_HOURLY);

                        // Actualizo el widget
                        updateAppWidget(context, appWidgetManager, appWidgetId, views);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SOLECITO", error.toString());
                    }
                });

        // Set the tag on the request.
        jsonObjectRequest.setTag(TAG);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    /**
     * Actualiza las vistas del widget
     */
    public static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views) {
        // Guardo en today.json los datos actuales
        if (today != null) {
            // Pinto los datos actuales
            views.setTextViewText(R.id.todayTempMin, today.getTemperaturaMin() + "º");
            views.setTextViewText(R.id.todayTempMax, today.getTemperaturaMax() + "º");
            views.setTextViewText(R.id.todayTemp, today.getTemperatura() + "º");
            views.setImageViewResource(R.id.todayStatusIcon, Utils.getStatusIcon(today.getEstado()));

            // Viento
            views.setTextViewText(R.id.todayWind, today.getVientoVelocidad());
            views.setImageViewResource(R.id.todayWindIcon, Utils.getWindDirectionIcon(today.getVientoDireccion()));

            // Trato los floats
            Float precipitacion = 0f;
            Float nieve = 0f;
            if (!today.getPrecipitacion().equals("")) {
                try {
                    precipitacion = Float.parseFloat(today.getPrecipitacion());
                } catch (NumberFormatException e) {
                    //No es un float por lo que dejo 0
                    precipitacion = 0f;
                }
            }

            if (!today.getNieve().equals("")) {
                try {
                    nieve = Float.parseFloat(today.getNieve());
                } catch (NumberFormatException e) {
                    //No es un float por lo que dejo 0
                    nieve = 0f;
                }
            }

            // Es visible o no la precipitación / nieve
            if (precipitacion == 0 && nieve == 0) {
                views.setViewVisibility(R.id.todayRainSnowContainer, View.INVISIBLE);
            } else {
                views.setViewVisibility(R.id.todayRainSnowContainer, View.VISIBLE);
                // Si hay más nieve que lluvia o viceversa pongo un valor u otro
                if (precipitacion >= nieve) {
                    views.setTextViewText(R.id.todayRainSnow, precipitacion.toString());
                    views.setImageViewResource(R.id.todayRainSnowIcon, R.drawable.ic_drop);
                } else {
                    views.setTextViewText(R.id.todayRainSnow, nieve.toString());
//TODO            views.setImageViewResource(R.id.todayRainSnowIcon, R.drawable.ic_flake);
                }
            }
        }

        //RemoteViews Service for Hourly
        Intent svcIntentHourly = new Intent(context, WidgetServiceHourly.class);
        //passing app widget id to that RemoteViews Service
        svcIntentHourly.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        // when intents are compared, the extras are ignored, so we need to
        // embed the extras into the data so that the extras will not be ignored
        svcIntentHourly.setData(Uri.parse(svcIntentHourly.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        views.setRemoteAdapter(R.id.listViewHourly, svcIntentHourly);
        views.setEmptyView(R.id.listViewHourly, android.R.id.empty);

        //RemoteViews Service for Daily
        Intent svcIntentDaily = new Intent(context, WidgetServiceDaily.class);
        svcIntentDaily.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntentDaily.setData(Uri.parse(svcIntentDaily.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.listViewDaily, svcIntentDaily);
        views.setEmptyView(R.id.listViewDaily, android.R.id.empty);

        // Actualizo el widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        // Actualizo el ListView de horas
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listViewHourly);
        // Actualizo el ListView de días
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listViewDaily);
    }

    /**
     * Parsea el JSON de los datos Horarios
     */
    public static List<WeatherListItem> parseHourlyInfo(JSONObject data) {
        // Los datos los guardo aquí
        List<WeatherListItem> listaFinal = new ArrayList<>();

        JSONArray dias;
        Calendar calander = Calendar.getInstance();
        int currentHour = calander.get(Calendar.HOUR_OF_DAY);

        // Cojo el array de días
        try {
            dias = data.getJSONArray("dia");
            Log.i("SOLECITO", "HourlyInfo");
            Log.i("SOLECITO", dias.toString());

            // Voy recorriendo cada día
            JSONObject dia, tempObj;
            Hashtable<Integer, WeatherListItem> dayList = new Hashtable<>();

            for (int i = 0; i < dias.length(); i++) {
                // Limpio la lista
                dayList.clear();

                // ¿Es el primer día?
                boolean isToday = i == 0;

                // El objeto JSON de datos del día
                dia = dias.getJSONObject(i);

                // Fecha
                String[] parts = dia.getString("fecha").split("-");
                String dYear = parts[0];
                String dMonth = parts[1];
                String dDay = parts[2];

                // Estado del cielo
                JSONArray estadoCielo = dia.getJSONArray("estadoCielo");
                for (int j = 0; j < estadoCielo.length(); j++) {
                    // Cojo cada cielo
                    tempObj = estadoCielo.getJSONObject(j);

                    // Si es la hora actual, lo guardo en ahora
                    String hourString = tempObj.getString("periodo");
                    int hora = Integer.parseInt(hourString);

                    // Si es la misma hora que ahora mismo y es el primer día = hoy
                    if (hora == currentHour && isToday) {
                        today.setEstado(tempObj.getString("value"));
                    }
                    // Si es mayor a la hora actual pa la lista o si no es hoy
                    else if (hora > currentHour || !isToday) {
                        // Como todavía la lista está vacía voy creando los elementos
                        dayList.put(hora, new WeatherListItem(
                                hourString,
                                Utils.getDateFormatted(dMonth, dDay),
                                Integer.parseInt(dYear + dMonth + dDay + hourString),
                                tempObj.getString("value")
                        ));
                    }
                }

                // Precipitacion
                JSONArray precipitacion = dia.getJSONArray("precipitacion");
                for (int j = 0; j < precipitacion.length(); j++) {
                    // Cojo cada cielo
                    tempObj = precipitacion.getJSONObject(j);

                    // Si es la hora actual, lo guardo en ahora
                    int hora = Integer.parseInt(tempObj.getString("periodo"));
                    if (hora == currentHour && isToday) {
                        today.setPrecipitacion(tempObj.getString("value"));
                    }
                    // Si es mayor a la hora actual pa la lista
                    else if (hora > currentHour || !isToday) {
                        // Cojo el elemento
                        WeatherListItem tempItem = dayList.get(hora);
                        tempItem.setPrecipitacion(tempObj.getString("value"));
                        dayList.put(hora, tempItem);
                    }
                }

                // Nieve
                JSONArray nieve = dia.getJSONArray("nieve");
                for (int j = 0; j < nieve.length(); j++) {
                    // Cojo cada cielo
                    tempObj = nieve.getJSONObject(j);

                    // Si es la hora actual, lo guardo en ahora
                    int hora = Integer.parseInt(tempObj.getString("periodo"));
                    if (hora == currentHour && isToday) {
                        today.setNieve(tempObj.getString("value"));
                    }
                    // Si es mayor a la hora actual pa la lista
                    else if (hora > currentHour || !isToday) {
                        // Cojo el elemento
                        WeatherListItem tempItem = dayList.get(hora);
                        tempItem.setNieve(tempObj.getString("value"));
                        dayList.put(hora, tempItem);
                    }
                }

                // Temperatura
                JSONArray temperatura = dia.getJSONArray("temperatura");
                for (int j = 0; j < temperatura.length(); j++) {
                    // Cojo cada cielo
                    tempObj = temperatura.getJSONObject(j);

                    // Si es la hora actual, lo guardo en ahora
                    int hora = Integer.parseInt(tempObj.getString("periodo"));
                    if (hora == currentHour && isToday) {
                        today.setTemperatura(tempObj.getString("value"));
                    }
                    // Si es mayor a la hora actual pa la lista
                    else if (hora > currentHour || !isToday) {
                        // Cojo el elemento
                        WeatherListItem tempItem = dayList.get(hora);
                        tempItem.setTemperatura(tempObj.getString("value"));
                        dayList.put(hora, tempItem);
                    }
                }

                // vientoAndRachaMax
                JSONArray viento = dia.getJSONArray("vientoAndRachaMax");
                for (int j = 0; j < viento.length(); j++) {
                    // Cojo cada cielo
                    tempObj = viento.getJSONObject(j);

                    // Si este elemento no tiene velocidad me lo salto
                    if (!tempObj.has("velocidad")) {
                        continue;
                    }

                    // Si es la hora actual, lo guardo en ahora
                    int hora = Integer.parseInt(tempObj.getString("periodo"));
                    if (hora == currentHour && isToday) {
                        today.setVientoDireccion(tempObj.getJSONArray("direccion").getString(0));
                        today.setVientoVelocidad(tempObj.getJSONArray("velocidad").getString(0));
                    }
                    // Si es mayor a la hora actual pa la lista
                    else if (hora > currentHour || !isToday) {
                        // Cojo el elemento
                        WeatherListItem tempItem = dayList.get(hora);
                        tempItem.setVientoDireccion(tempObj.getJSONArray("direccion").getString(0));
                        tempItem.setVientoVelocidad(tempObj.getJSONArray("velocidad").getString(0));
                        dayList.put(hora, tempItem);
                    }
                }

                // Una vez finalizado el día, lo añado a la lista final
                for (Integer key : dayList.keySet()) {
                    listaFinal.add(dayList.get(key));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listaFinal;
    }

    /**
     * Parsea el JSON de los datos Diarios
     */
    public static List<WeatherListItem> parseDailyInfo(JSONObject data) {
        // Los datos los guardo aquí
        List<WeatherListItem> listaFinal = new ArrayList<>();

        JSONArray dias;

        // Cojo el array de días
        try {
            dias = data.getJSONArray("dia");
            Log.i("SOLECITO", "DailyInfo");
            Log.i("SOLECITO", dias.toString());

            // Voy recorriendo cada día
            JSONObject dia, tempObj;
            WeatherListItem dayItem;

            for (int i = 0; i < dias.length(); i++) {
                // ¿Es el primer día?
                boolean isToday = i == 0;

                // El objeto JSON de datos del día
                dia = dias.getJSONObject(i);

                // Fecha
                String[] parts = dia.getString("fecha").split("-");
                String dYear = parts[0];
                String dMonth = parts[1];
                String dDay = parts[2];

                // Objeto para este día
                dayItem = new WeatherListItem();
                dayItem.setTimestamp(Integer.parseInt(dYear + dMonth + dDay));
                dayItem.setFecha(Utils.getDateFormatted(dMonth, dDay));

                // Estado del cielo
                JSONArray estadoCielo = dia.getJSONArray("estadoCielo");
                for (int j = 0; j < estadoCielo.length(); j++) {
                    // Cojo cada cielo
                    tempObj = estadoCielo.getJSONObject(j);

                    String period = tempObj.getString("periodo");

                    // Si no es today y el periodo es 00-24 me interesa
                    if (!isToday && period.equals("00-24")) {
                        dayItem.setEstado(tempObj.getString("value"));
                    }
                }

                // Precipitacion
                JSONArray precipitacion = dia.getJSONArray("probPrecipitacion");
                for (int j = 0; j < precipitacion.length(); j++) {
                    // Cojo cada precipitacion
                    tempObj = precipitacion.getJSONObject(j);

                    String period = tempObj.getString("periodo");

                    // Si no es today y el periodo es 00-24 me interesa
                    if (!isToday && period.equals("00-24")) {
                        dayItem.setPrecipitacion(tempObj.getString("value"));
                    }
                }

                // Nieve
                JSONArray nieve = dia.getJSONArray("cotaNieveProv");
                for (int j = 0; j < nieve.length(); j++) {
                    // Cojo cada nieve
                    tempObj = nieve.getJSONObject(j);

                    String period = tempObj.getString("periodo");

                    // Si no es today y el periodo es 00-24 me interesa
                    if (!isToday && period.equals("00-24")) {
                        dayItem.setCotaNieve(tempObj.getString("value"));
                    }
                }

                // Temperatura
                JSONObject temperatura = dia.getJSONObject("temperatura");
                //Min y Max para el actual y diario
                if (isToday) {
                    today.setTemperaturaMin(temperatura.getString("minima"));
                    today.setTemperaturaMax(temperatura.getString("maxima"));
                } else {
                    dayItem.setTemperaturaMin(temperatura.getString("minima"));
                    dayItem.setTemperaturaMax(temperatura.getString("maxima"));
                }

                // vientoAndDireccion
                JSONArray viento = dia.getJSONArray("viento");
                for (int j = 0; j < viento.length(); j++) {
                    // Cojo cada viento
                    tempObj = viento.getJSONObject(j);

                    String period = tempObj.getString("periodo");

                    // Si no es today y el periodo es 00-24 me interesa
                    if (!isToday && period.equals("00-24")) {
                        dayItem.setVientoVelocidad(tempObj.getString("velocidad"));
                        dayItem.setVientoDireccion(tempObj.getString("direccion"));
                    }
                }

                // Una vez finalizado el día, lo añado a la lista final si no es hoy
                if (!isToday) {
                    listaFinal.add(dayItem);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listaFinal;
    }
}

