package com.ligresoftware.solete.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.reflect.TypeToken;
import com.ligresoftware.solete.R;
import com.ligresoftware.solete.WidgetServiceHourly;
import com.ligresoftware.solete.daily.DailyListItem;
import com.ligresoftware.solete.hourly.HourlyListItem;
import com.ligresoftware.solete.utils.CacheManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link HomeWidgetConfigureActivity HomeWidgetConfigureActivity}
 */
public class HomeWidget extends AppWidgetProvider {
    public static Integer idMunicipio;

    public static final String TAG = "SoleteTag";
    public static final String FILE_CACHE_DAILY = "daily.json";
    public static final String FILE_CACHE_HOURLY = "hourly.json";
    static RequestQueue queue = null;
    private static final String OPENDATA_URI = "https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/";
    private static final String OPENDATA_DAILY = "diaria/";
    private static final String OPENDATA_HOURLY = "horaria/";
    private static final String API_KEY = "?api_key=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsb3ZlaGluYWVzcEBnbWFpbC5jb20iLCJqdGkiOiIwMDlmYzZlNy1iZmMxLTRhZDItOTA2ZC0wZmUzODUzZDg1OTkiLCJpc3MiOiJBRU1FVCIsImlhdCI6MTUyMTg5OTcyOCwidXNlcklkIjoiMDA5ZmM2ZTctYmZjMS00YWQyLTkwNmQtMGZlMzg1M2Q4NTk5Iiwicm9sZSI6IiJ9.WIFq7E8N6Q2ZisfTvbxJfd9ZiVNrUKQlQ_y99aQ_Vrs";

    // Codigos provincia y municipio
    // http://www.ine.es/daco/daco42/codmun/codmunmapa.htm
    // https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/diaria/24089
    // https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/horaria/24089

    /**
     * Consulta las predicciones y actualiza el widget. Son 4 consultas:
     * XMLs diarios -> datos diarios
     * XMLs horarios -> datos horarios
     */
    static void getPredictionsAndUpdate(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        Log.i("SOLECITO", "Toy updateando ****************************");

        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.home_widget);

        // Preferencias del widget
        CharSequence cprovText = HomeWidgetConfigureActivity.loadCProvPref(context, appWidgetId);
        CharSequence cmunText = HomeWidgetConfigureActivity.loadCMunvPref(context, appWidgetId);

        // Petición API
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(context);

        // Pido datos y luego actualizaré
        if (!cprovText.equals("") && !cmunText.equals("")) {
            idMunicipio = Integer.parseInt("" + cprovText + cmunText);
            getDiaryXML(context, appWidgetManager, appWidgetId, views);
        }


     /*   //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, WidgetServiceHourly.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        views.setRemoteAdapter(R.id.listViewHourly, svcIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listViewHourly);*/
    }

    public static void getDiaryXML(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views) {
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
                                getDiaryData(context, appWidgetManager, appWidgetId, views, urlData);
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

    public static void getDiaryData(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views, final String urlData) {
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, urlData, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    // Cojo el objeto json que incluye el array
                    JSONObject myJson = response.getJSONObject(0);

                    Log.i("SOLECITO", "Pido los datos diarios");
                    Log.i("SOLECITO", myJson.toString());
                    if (myJson.getInt("id") == idMunicipio) {
                        // Recojo la información diaria
                        ArrayList<DailyListItem> listaDiaria = parseDailyInfo(myJson.getJSONObject("prediccion"));

                        // Guardo en caché los datos
                        Type type = new TypeToken<ArrayList<DailyListItem>>() {
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

    public static void getHourlyData(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views, final String urlData) {
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, urlData, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                // Si no es un 200 algo pasó
                try {
                    // Cojo el objeto json que incluye el array
                    JSONObject myJson = response.getJSONObject(0);

                    Log.i("SOLECITO", "Pido los datos horarios");
                    Log.i("SOLECITO", myJson.toString());
                    if (myJson.getInt("id") == idMunicipio) {
                        // Recojo la información horaria
                        ArrayList<HourlyListItem> listaHoraria = parseHourlyInfo(myJson.getJSONObject("prediccion"));

                        // Guardo en caché los datos
                        Type type = new TypeToken<ArrayList<HourlyListItem>>() {
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

    public static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final RemoteViews views) {

//                        Log.i("SOLECITO", response.toString());
        // Display the first 500 characters of the response string.
//                        mTextView.setText("Response is: " + response.substring(0, 500));
                       /* try {
                            views.setTextViewText(R.id.textView9, response.getString("title"));
                            views.setTextViewText(R.id.textView10, response.getString("userId"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/
        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, WidgetServiceHourly.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        // when intents are compared, the extras are ignored, so we need to
        // embed the extras into the data so that the extras will not be ignored
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        views.setRemoteAdapter(R.id.listViewHourly, svcIntent);
        views.setEmptyView(R.id.listViewHourly, android.R.id.empty);

        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listViewHourly);
    }

    /*
    05-09 21:28:46.623 16448-16448/com.ligresoftware.solete I/SOLECITO: Toy updateando ****************************
05-09 21:28:48.667 16448-16448/com.ligresoftware.solete I/SOLECITO: Toy updateando ****************************
05-09 21:28:49.379 16448-16448/com.ligresoftware.solete I/SOLECITO: Creo el WidgetService ****************************
05-09 21:28:49.380 16448-16448/com.ligresoftware.solete I/SOLECITO: Creo el HourlyListProvider WidgetFactory ****************************
    Toy populateListItem ****************************
05-09 21:28:49.382 16448-16468/com.ligresoftware.solete I/SOLECITO: onDataSetChanged ****************************
    Toy populateListItem ****************************
05-09 21:59:19.134 16448-16448/com.ligresoftware.solete I/SOLECITO: Toy updateando ****************************
05-09 21:59:19.144 16448-16459/com.ligresoftware.solete I/SOLECITO: onDataSetChanged ****************************
    Toy populateListItem ****************************
05-09 22:32:41.161 16448-16448/com.ligresoftware.solete I/SOLECITO: Toy updateando ****************************
05-09 22:32:41.168 16448-16460/com.ligresoftware.solete I/SOLECITO: onDataSetChanged ****************************
    Toy populateListItem ****************************

    Y se actualiza la lista :D
     */

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            getPredictionsAndUpdate(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            HomeWidgetConfigureActivity.deleteCProvPref(context, appWidgetId);
            HomeWidgetConfigureActivity.deleteCMunPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    public static ArrayList<HourlyListItem> parseHourlyInfo(JSONObject data) {
        ArrayList<HourlyListItem> lista = new ArrayList<>();
        JSONArray dias;
        Calendar calander = Calendar.getInstance();
        int cDay = calander.get(Calendar.DAY_OF_MONTH);
        int cMonth = calander.get(Calendar.MONTH) + 1;
        int cHour = calander.get(Calendar.HOUR);

        // Cojo el array de días
        try {
            dias = data.getJSONArray("dia");
            Log.i("SOLECITO", "Horaria");
            Log.i("SOLECITO", dias.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Voy recorriendo cada día

        return lista;
    }


    public static ArrayList<DailyListItem> parseDailyInfo(JSONObject data) {
        ArrayList<DailyListItem> lista = new ArrayList<>();
        JSONArray dias;
        Calendar calander = Calendar.getInstance();
        int cDay = calander.get(Calendar.DAY_OF_MONTH);
        int cMonth = calander.get(Calendar.MONTH) + 1;
        int cHour = calander.get(Calendar.HOUR);

        // Cojo el array de días
        try {
            dias = data.getJSONArray("dia");
            Log.i("SOLECITO", "Diaria");
            Log.i("SOLECITO", dias.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Voy recorriendo cada día sacando la hora actual y el resto de horas

        return lista;
    }
}

