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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import java.util.List;
import java.util.Objects;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link HomeWidgetConfigureActivity HomeWidgetConfigureActivity}
 */
public class HomeWidget extends AppWidgetProvider {
    // Timeout de peticiones en milisegundos
    private static final int MY_SOCKET_TIMEOUT_MS = 10000;
    public static Integer idMunicipio;

    public static final String TAG = "SoleteTag";
    public static final String FILE_CACHE_DAILY = "daily.json";
    public static final String FILE_CACHE_HOURLY = "hourly.json";
    private static final String DATA_URI = "http://solete-solete.a3c1.starter-us-west-1.openshiftapps.com/api/prediction/";

    static RequestQueue queue = null;
    static WeatherListItem today = null;

    static List<String> myLog = new ArrayList<>();

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
        Log.i("SOLECITO", "Iniciando update ****************************");

        // inicializo el log
        myLog.clear();
        myLog.add(Utils.log("Iniciando update (getPredictionsAndUpdate)"));

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
            myLog.add(Utils.log("Codigo municipio: " + idMunicipio));

            // Creo un objeto nuevo para los datos actuales
            today = new WeatherListItem();

            // Paso a obtener los datos
            getDataFromServer(context, appWidgetManager, appWidgetId, views);
        }
    }

    /**
     * Obtengo el xml de los datos diarios
     */
    public static void getDataFromServer(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views) {
        String url = DATA_URI + idMunicipio.toString();
        Log.i("SOLECITO", "Llamo al API " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Si no es un 200 algo pasó
                        try {
                            Log.i("SOLECITO", "Pido los datos");
                            Log.i("SOLECITO", response.toString());
                            myLog.add(Utils.log("Pido los datos"));
                            myLog.add(Utils.log(response.toString()));

                            List<WeatherListItem> listaDiaria = parseDailyInfo(response.getJSONArray("daily"));
                            List<WeatherListItem> listaHoraria = parseHourlyInfo(response.getJSONArray("hourly"));
                            today = parseTodayInfo(response.getJSONObject("today"));

                            // Escribo los json de diario y horario
                            // Guardo en caché los datos
                            Type type = new TypeToken<List<WeatherListItem>>() {
                            }.getType();
                            CacheManager cacheManager = new CacheManager(context);
                            cacheManager.writeJson(listaDiaria, type, FILE_CACHE_DAILY);
                            cacheManager.writeJson(listaHoraria, type, FILE_CACHE_HOURLY);

                            // Actualizo el widget
                            updateAppWidget(context, appWidgetManager, appWidgetId, views);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            myLog.add(Utils.log("JSONException: " + e.toString()));
                            Utils.writeTheLog(myLog, context, "error");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SOLECITO", error.toString());
                        myLog.add(Utils.log("ERROR: " + error.toString()));
                        Utils.writeTheLog(myLog, context, "error");
                    }
                });

        // Set the tag on the request.
        jsonObjectRequest.setTag(TAG);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    /**
     * Actualiza las vistas del widget
     */
    public static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views) {
        // Guardo en today.json los datos actuales
        if (today != null) {
            // La fecha
            Calendar calander = Calendar.getInstance();
            today.setFecha(
                    Utils.getDayFormatted("" + calander.get(Calendar.YEAR), "" + (calander.get(Calendar.MONTH) + 1), "" + calander.get(Calendar.DAY_OF_MONTH)));
            views.setTextViewText(R.id.todayDate, today.getFecha());

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
                views.setViewVisibility(R.id.todayRainSnowIcon, View.INVISIBLE);
                views.setViewVisibility(R.id.todayRainSnow, View.INVISIBLE);
            } else {
                views.setViewVisibility(R.id.todayRainSnowIcon, View.VISIBLE);
                views.setViewVisibility(R.id.todayRainSnow, View.VISIBLE);
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

        myLog.add(Utils.log("Pinto en el widget"));
        // Escribo el log
        Utils.writeTheLog(myLog, context, "log");

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
     * Parsea el JSON de los datos de Hoy
     */
    public static WeatherListItem parseTodayInfo(JSONObject dia) {
        // Los datos los guardo aquí
        WeatherListItem itemFinal = new WeatherListItem();

        // Cojo el array de días
        try {
            // Compongo el Item
            itemFinal.setEstado(dia.getString("estado"));
            itemFinal.setPrecipitacion(dia.getString("precipitacion"));
            itemFinal.setNieve(dia.getString("nieve"));
            itemFinal.setTemperatura(dia.getString("temperatura"));
            itemFinal.setTemperaturaMin(dia.getString("temperaturaMin"));
            itemFinal.setTemperaturaMax(dia.getString("temperaturaMax"));
            itemFinal.setVientoDireccion(dia.getString("vientoDireccion"));
            itemFinal.setVientoVelocidad(dia.getString("vientoVelocidad"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return itemFinal;
    }

    /**
     * Parsea el JSON de los datos Horarios
     */
    public static List<WeatherListItem> parseHourlyInfo(JSONArray data) {
        // Los datos los guardo aquí
        List<WeatherListItem> listaFinal = new ArrayList<>();

        // Timestamp actual
        Calendar calander = Calendar.getInstance();
        String currentTimestampStr = "" + calander.get(Calendar.YEAR);

        int month = calander.get(Calendar.MONTH) + 1;
        if (month < 10) {
            currentTimestampStr += '0';
        }
        currentTimestampStr += month;

        int daymonth = calander.get(Calendar.DAY_OF_MONTH);
        if (daymonth < 10) {
            currentTimestampStr += '0';
        }
        currentTimestampStr += daymonth;

        int hourday = calander.get(Calendar.HOUR_OF_DAY);
        if (hourday < 10) {
            currentTimestampStr += '0';
        }
        currentTimestampStr += hourday;

        int currentTimestamp = Integer.parseInt(currentTimestampStr);

        // Cojo el array de días
        try {
            WeatherListItem hourItem;
            JSONObject hour;

            for (int i = 0; i < data.length(); i++) {
                hour = data.getJSONObject(i);
                hourItem = new WeatherListItem();

                // Si la hora no es mayor que la actual, no lo guardo
                if (hour.getInt("timestamp") > currentTimestamp) {
                    // Compongo el Item
                    hourItem.setHora(hour.getString("hora"));
                    hourItem.setTimestamp(hour.getInt("timestamp"));
                    hourItem.setFecha(hour.getString("fecha"));
                    hourItem.setEstado(hour.getString("estado"));
                    hourItem.setPrecipitacion(hour.getString("precipitacion"));
                    hourItem.setNieve(hour.getString("nieve"));
                    hourItem.setTemperatura(hour.getString("temperatura"));
                    hourItem.setVientoDireccion(hour.getString("vientoDireccion"));
                    hourItem.setVientoVelocidad(hour.getString("vientoVelocidad"));

                    // Una vez finalizado
                    listaFinal.add(hourItem);
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
    public static List<WeatherListItem> parseDailyInfo(JSONArray data) {
        // Los datos los guardo aquí
        List<WeatherListItem> listaFinal = new ArrayList<>();

        // Cojo el array de días
        try {
            WeatherListItem dayItem;
            JSONObject dia;

            for (int i = 0; i < data.length(); i++) {
                dia = data.getJSONObject(i);
                dayItem = new WeatherListItem();

                // Compongo el dayItem
                dayItem.setTimestamp(dia.getInt("timestamp"));
                dayItem.setFecha(dia.getString("fecha"));
                dayItem.setEstado(dia.getString("estado"));
                dayItem.setPrecipitacion(dia.getString("precipitacion"));
                dayItem.setCotaNieve(dia.getString("cotaNieve"));
                dayItem.setTemperaturaMin(dia.getString("temperaturaMin"));
                dayItem.setTemperaturaMax(dia.getString("temperaturaMax"));
                dayItem.setVientoDireccion(dia.getString("vientoDireccion"));
                dayItem.setVientoVelocidad(dia.getString("vientoVelocidad"));

                // Una vez finalizado el día, lo añado a la lista final
                listaFinal.add(dayItem);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listaFinal;
    }
}

