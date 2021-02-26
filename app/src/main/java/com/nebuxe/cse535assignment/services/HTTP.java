package com.nebuxe.cse535assignment.services;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.nebuxe.cse535assignment.R;
import com.nebuxe.cse535assignment.callbacks.HTTPRequestListener;
import com.nebuxe.cse535assignment.pojos.SubjectsResponse;
import com.nebuxe.cse535assignment.pojos.UploadResponse;
import com.nebuxe.cse535assignment.utilities.SharedPreferencesValues;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HTTP {
    public static void uploadDatabase(Context context, HTTPRequestListener httpRequestListener) {

        Handler mainHandler = new Handler(context.getMainLooper());

        String baseURL = context.getString(R.string.base_url);
        String url = baseURL + "/api/v1/uploads";

        String userID = SharedPreferencesValues.getUsername(context);
        Log.d("HTTP", userID);
        File file = new File("/data/data/" + context.getPackageName() + "/databases/" + userID);
        MediaType mediaType = MediaType.parse("application/vnd.sqlite3");

        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("subject_id", userID)
                .addFormDataPart("db_file", userID, RequestBody.create(file, mediaType))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        httpRequestListener.onFailure(e);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        UploadResponse uploadResponse = null;
                        try {
                            uploadResponse = gson.fromJson(response.body().string(), UploadResponse.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        httpRequestListener.onResponse(response.isSuccessful(), uploadResponse, response);
                    }
                });
            }
        });
    }

    public static void getContactTraces(Context context, String subjectID, String endDate, HTTPRequestListener httpRequestListener) {

        Handler mainHandler = new Handler(context.getMainLooper());

        String baseURL = context.getString(R.string.base_url);
        String url = HttpUrl.parse(baseURL).newBuilder()
                .addPathSegment("/api/v1/contact_traces")
                .addQueryParameter("subject_id", subjectID)
                .addQueryParameter("end_date", endDate)
                .build()
                .toString();

        Log.d("URLL", url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        httpRequestListener.onFailure(e);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String responseText = "";
                        try {
                            responseText = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        httpRequestListener.onResponse(response.isSuccessful(), responseText, response);
                    }
                });
            }
        });
    }

    public static void getAllSubjects(Context context, HTTPRequestListener httpRequestListener) {

        Handler mainHandler = new Handler(context.getMainLooper());

        String url = context.getString(R.string.base_url) + "/api/v1/subjects";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        httpRequestListener.onFailure(e);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        SubjectsResponse subjectsResponse = new SubjectsResponse();
                        try {
                            subjectsResponse = gson.fromJson(response.body().string(), SubjectsResponse.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        httpRequestListener.onResponse(response.isSuccessful(), subjectsResponse, response);
                    }
                });
            }
        });
    }
}
