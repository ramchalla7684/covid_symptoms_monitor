package com.nebuxe.cse535assignment;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nebuxe.cse535assignment.adapters.HealthRecordsAdapter;
import com.nebuxe.cse535assignment.callbacks.HTTPRequestListener;
import com.nebuxe.cse535assignment.models.SymptomsModel;
import com.nebuxe.cse535assignment.pojos.HealthRecord;
import com.nebuxe.cse535assignment.pojos.UploadResponse;
import com.nebuxe.cse535assignment.services.HTTP;
import com.nebuxe.cse535assignment.utilities.SharedPreferencesValues;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class SymptomsHistoryActivity extends AppCompatActivity {

    private LinearLayout backButton;
    private TextView tvUsername;
    private RecyclerView rvHealthRecords;
    private Button bUpload;

    private List<HealthRecord> healthRecords = new ArrayList<>();
    private HealthRecordsAdapter healthRecordsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms_history);

        bindViews();
        setEventListeners();
        setAdapters();

        healthRecords.addAll(SymptomsModel.getRecordedSymptoms(getApplicationContext()));
        Log.d("RECORDS", healthRecords.size() + "");

        healthRecordsAdapter.notifyDataSetChanged();

        tvUsername.setText("Hey, " + SharedPreferencesValues.getUsername(getApplicationContext()) + "! Here is your recorded symptoms");
    }

    private void bindViews() {
        backButton = findViewById(R.id.ll_back_button);
        tvUsername = findViewById(R.id.tv_username);
        rvHealthRecords = findViewById(R.id.rv_health_records);
        bUpload = findViewById(R.id.b_upload);

        rvHealthRecords.setNestedScrollingEnabled(true);
    }

    private void setEventListeners() {
        backButton.setOnClickListener(view -> onBackPressed());
        bUpload.setOnClickListener(view -> uploadDatabase());
    }

    private void setAdapters() {
        healthRecordsAdapter = new HealthRecordsAdapter(this, healthRecords);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvHealthRecords.setLayoutManager(linearLayoutManager);

        rvHealthRecords.setAdapter(healthRecordsAdapter);
        healthRecordsAdapter.notifyDataSetChanged();
    }

    private void uploadDatabase() {
        HTTP.uploadDatabase(getApplicationContext(), new HTTPRequestListener() {
            @Override
            public void onFailure(@NotNull IOException e) {
                Log.d("HTTP", "FAILED");
                Log.d("HTTP", e.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(boolean success, @NotNull Object object, @NotNull Response response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                    return;
                }

                UploadResponse uploadResponse = (UploadResponse) object;
                if (!uploadResponse.isSuccess()) {
                    Toast.makeText(getApplicationContext(), uploadResponse.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(getApplicationContext(), "Uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
