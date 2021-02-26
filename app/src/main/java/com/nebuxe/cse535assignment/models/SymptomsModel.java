package com.nebuxe.cse535assignment.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nebuxe.cse535assignment.R;
import com.nebuxe.cse535assignment.models.sqlite.SQLiteManager;
import com.nebuxe.cse535assignment.pojos.HealthRecord;
import com.nebuxe.cse535assignment.utilities.SharedPreferencesValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymptomsModel {
    public static final String DATE = "date";

    public static final String HEART_BEAT_RATE = "heart_beat_rate";
    public static final String RESPIRATORY_RATE = "respiratory_rate";

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    public static String getTableName(Context context) {
//        return SharedPreferencesValues.getUsername(context).replace(" ", "_").toLowerCase();
        return "symptom_ratings";
    }

    public static String getSymptomColumnName(String symptom) {
        return symptom.replace(" ", "_").toLowerCase();
    }

    public static void createNewRecordIfNotExists(Context context, long date) {
        SQLiteDatabase db = SQLiteManager.getInstance(context, SharedPreferencesValues.getUsername(context)).getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DATE, date);

        db.insertWithOnConflict(getTableName(context), null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static void saveHeartBeatRate(Context context, long date, int bpm) {
        SQLiteDatabase db = SQLiteManager.getInstance(context, SharedPreferencesValues.getUsername(context)).getWritableDatabase();
        createNewRecordIfNotExists(context, date);

        String whereClause = new StringBuilder()
                .append(DATE + " = " + date)
                .toString();

        ContentValues contentValues = new ContentValues();
        contentValues.put(HEART_BEAT_RATE, bpm);

        db.updateWithOnConflict(getTableName(context), contentValues, whereClause, null, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void saveRespiratoryRate(Context context, long date, int bpm) {
        SQLiteDatabase db = SQLiteManager.getInstance(context, SharedPreferencesValues.getUsername(context)).getWritableDatabase();
        createNewRecordIfNotExists(context, date);

        String whereClause = new StringBuilder()
                .append(DATE + " = " + date)
                .toString();

        ContentValues contentValues = new ContentValues();
        contentValues.put(RESPIRATORY_RATE, bpm);
        db.updateWithOnConflict(getTableName(context), contentValues, whereClause, null, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void saveSymptomsRatings(Context context, long date, HashMap<String, Integer> symptomRatings) {

        SQLiteDatabase db = SQLiteManager.getInstance(context, null).getWritableDatabase();
        createNewRecordIfNotExists(context, date);

        String whereClause = new StringBuilder()
                .append(DATE + " = " + date)
                .toString();

        ContentValues contentValues = new ContentValues();
        for (Map.Entry<String, Integer> entry : symptomRatings.entrySet()) {
            contentValues.put(getSymptomColumnName(entry.getKey()), entry.getValue());
        }

        db.updateWithOnConflict(getTableName(context), contentValues, whereClause, null, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void saveLocation(Context context, long date, double latitude, double longitude) {

        SQLiteDatabase db = SQLiteManager.getInstance(context, null).getWritableDatabase();
        createNewRecordIfNotExists(context, date);

        String whereClause = new StringBuilder()
                .append(DATE + " = " + date)
                .toString();

        ContentValues contentValues = new ContentValues();
        contentValues.put(LATITUDE, latitude);
        contentValues.put(LONGITUDE, longitude);
        db.updateWithOnConflict(getTableName(context), contentValues, whereClause, null, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static List<HealthRecord> getRecordedSymptoms(Context context) {
        SQLiteDatabase db = SQLiteManager.getInstance(context, SharedPreferencesValues.getUsername(context)).getReadableDatabase();

        String[] symptoms = context.getResources().getStringArray(R.array.symptoms);

        String query = "SELECT * FROM " + getTableName(context) + " ORDER BY " + DATE + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        List<HealthRecord> recordedSymptoms = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            HealthRecord healthRecord = new HealthRecord();

            healthRecord.setDate(cursor.getLong(cursor.getColumnIndex(DATE)));
            healthRecord.setHeartBeatRate(cursor.getInt(cursor.getColumnIndex(HEART_BEAT_RATE)));
            healthRecord.setRespiratoryRate(cursor.getInt(cursor.getColumnIndex(RESPIRATORY_RATE)));

            HashMap<String, Integer> symptomRatings = new HashMap<>();
            for (String symptom : symptoms) {
                int rating = cursor.getInt(cursor.getColumnIndex(getSymptomColumnName(symptom)));
                if (rating != 0) {
                    symptomRatings.put(symptom, rating);
                }
            }

            healthRecord.setSymptomRatings(symptomRatings);

            recordedSymptoms.add(healthRecord);
        }

        cursor.close();
        return recordedSymptoms;
    }
}
