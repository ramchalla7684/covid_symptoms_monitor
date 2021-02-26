package com.nebuxe.cse535assignment.models.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.nebuxe.cse535assignment.R;
import com.nebuxe.cse535assignment.models.SymptomsModel;
import com.nebuxe.cse535assignment.utilities.SharedPreferencesValues;

public class SQLiteManager extends SQLiteOpenHelper {

    private static SQLiteManager sqLiteManagerInstance;

    private Context context;

    public SQLiteManager(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version) {
        super(context, name, cursorFactory, version);

        this.context = context;
    }

    public static SQLiteManager getInstance(Context context, @Nullable String dbname) {
        if (sqLiteManagerInstance == null) {
            if (dbname == null) {
                dbname = SharedPreferencesValues.getUsername(context);
            }
//            sqLiteManagerInstance = new SQLiteManager(context, context.getResources().getString(R.string.app_name), null, 1);
            sqLiteManagerInstance = new SQLiteManager(context, dbname, null, 1);
        }
        sqLiteManagerInstance.context = context;

        return sqLiteManagerInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        createSymptomsTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void createSymptomsTable() {
        SQLiteDatabase db = getWritableDatabase();
        String[] symptoms = context.getResources().getStringArray(R.array.symptoms);

        StringBuilder queryStringBuilder = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(SymptomsModel.getTableName(context) + " ")
                .append("(")
                .append(SymptomsModel.DATE + " BIGINT PRIMARY KEY, ")
                .append(SymptomsModel.HEART_BEAT_RATE + " INT DEFAULT 0, ")
                .append(SymptomsModel.RESPIRATORY_RATE + " INT DEFAULT 0, ")
                .append(SymptomsModel.LATITUDE + " NUMERIC DEFAULT 0.0, ")
                .append(SymptomsModel.LONGITUDE + " NUMERIC DEFAULT 0.0, ");
        for (int i = 0; i < symptoms.length; i++) {
            if (i != symptoms.length - 1) {
                queryStringBuilder = queryStringBuilder.append(SymptomsModel.getSymptomColumnName(symptoms[i]) + " INT DEFAULT 0, ");
            } else {
                queryStringBuilder = queryStringBuilder.append(SymptomsModel.getSymptomColumnName(symptoms[i]) + " INT DEFAULT 0");
            }
        }
        queryStringBuilder = queryStringBuilder.append(")");

        db.execSQL(queryStringBuilder.toString());
    }
}
