package com.ligresoftware.solete.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import static com.ligresoftware.solete.BuildConfig.DEBUG;


public class CacheManager {
    private Context context;
    private static final String TAG = CacheManager.class.getSimpleName();

    public CacheManager(Context context) {
        this.context = context;
    }

    public void writeJson(Object object, Type type, String fileName) {
        File file = new File(this.context.getCacheDir(), fileName);
        File file2 = new File(context.getFilesDir(), fileName);

        OutputStream outputStream = null;
        OutputStream outputStream2 = null;
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
        try {

//            outputStream = this.context.openFileOutput(fileName, Context.MODE_PRIVATE);
//            outputStream.write(gson.toJson(object, type).getBytes());
//            outputStream.close();

            outputStream = new FileOutputStream(file);
            BufferedWriter bufferedWriter;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

            outputStream2 = new FileOutputStream(file2);
            BufferedWriter bufferedWriter2;
            bufferedWriter2 = new BufferedWriter(new OutputStreamWriter(outputStream2, StandardCharsets.UTF_8));
            gson.toJson(object, type, bufferedWriter2);
            bufferedWriter2.close();

            gson.toJson(object, type, bufferedWriter);
            bufferedWriter.close();

        } catch (Exception e) {
            Log.e("SOLECITO", "Error escribiendo");
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outputStream2 != null) {
                try {
                    outputStream2.flush();
                    outputStream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public Object readJson(Type type, String fileName) {
        Object jsonData = null;

        File file = new File(context.getCacheDir(), fileName);
        InputStream inputStream = null;
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
        try {
            inputStream = new FileInputStream(file);
            InputStreamReader streamReader;
            streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            jsonData = gson.fromJson(streamReader, type);
            streamReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (DEBUG) Log.e(TAG, "loadJson, FileNotFoundException e: '" + e + "'");
        } catch (IOException e) {
            e.printStackTrace();
            if (DEBUG) Log.e(TAG, "loadJson, IOException e: '" + e + "'");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    if (DEBUG) Log.e(TAG, "loadJson, finally, e: '" + e + "'");
                }
            }
        }
        return jsonData;
    }

    public void writeLog(Object object, Type type, String fileName) {
        File file2 = new File(context.getFilesDir(), fileName);

        OutputStream outputStream2 = null;
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
        try {
            outputStream2 = new FileOutputStream(file2);
            BufferedWriter bufferedWriter2;
            bufferedWriter2 = new BufferedWriter(new OutputStreamWriter(outputStream2, StandardCharsets.UTF_8));
            gson.toJson(object, type, bufferedWriter2);
            bufferedWriter2.close();
        } catch (Exception e) {
            Log.e("SOLECITO", "Error escribiendo");
            e.printStackTrace();
        } finally {
            if (outputStream2 != null) {
                try {
                    outputStream2.flush();
                    outputStream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}