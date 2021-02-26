package com.nebuxe.cse535assignment;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nebuxe.cse535assignment.callbacks.HTTPRequestListener;
import com.nebuxe.cse535assignment.pojos.SubjectsResponse;
import com.nebuxe.cse535assignment.services.HTTP;
import com.nebuxe.cse535assignment.utilities.Utils;
import com.nebuxe.cse535assignment.views.DialogView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class ContactTracingActivity extends AppCompatActivity {

    private LinearLayout backButton;

    private Spinner spinnerSubjects;
    private Button bDatePicker;
    private Button bSubmit;

    private DatePicker datePicker;
    private Button bDateOk;

    private Dialog datePickerDialog;

    private String subjectID = "";
    private String endDate = "";

    private List<String> subjects = new ArrayList<>();
    private ArrayAdapter<String> subjectsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_tracing);

        setupDialogs();
        bindViews();
        setEventListeners();
        setAdapters();
        showSelectedDate();
        getAllSubjects();
    }

    private void setupDialogs() {
        datePickerDialog = new DialogView(this).getDialog(R.layout.layout_dialog_date_picker, true);
    }

    private void bindViews() {
        backButton = findViewById(R.id.ll_back_button);
        spinnerSubjects = findViewById(R.id.spinner_subjects);
        bDatePicker = findViewById(R.id.b_date_picker);
        bSubmit = findViewById(R.id.b_submit);

        datePicker = datePickerDialog.findViewById(R.id.date_picker);
        bDateOk = datePickerDialog.findViewById(R.id.b_date_ok);
    }

    private void setEventListeners() {
        backButton.setOnClickListener(view -> onBackPressed());
        bDatePicker.setOnClickListener(view -> showDatePicker());

        spinnerSubjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                subjectID = subjects.get(position);
                showSubmitButton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showSubmitButton();
            }
        });

        bDateOk.setOnClickListener(view -> {
            datePickerDialog.dismiss();

            int year = datePicker.getYear();
            int month = datePicker.getMonth() + 1;
            int day = datePicker.getDayOfMonth();

            endDate = "";
            endDate += year;
            endDate += month < 10 ? "0" + month : month;
            endDate += day < 10 ? "0" + day : day;

            showSelectedDate();
            showSubmitButton();
        });

        bSubmit.setOnClickListener(view -> getContactTraces());

    }

    private void setAdapters() {
        subjectsAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.layout_spinner_item, subjects);
        spinnerSubjects.setAdapter(subjectsAdapter);
    }

    private void showDatePicker() {
        datePickerDialog.show();
    }

    private void showSelectedDate() {
        String buttonText = "     " + getResources().getString(R.string.pick_a_date);
        if (!endDate.isEmpty()) {
            buttonText += "   " + "( " + endDate.substring(0, 4) + "/" + endDate.substring(4, 6) + "/" + endDate.substring(6, 8) + " )";
        }
        bDatePicker.setText(buttonText);
    }

    private void showSubmitButton() {
        if (!subjectID.isEmpty() && !endDate.isEmpty()) {
            bSubmit.setVisibility(View.VISIBLE);
        } else {
            bSubmit.setVisibility(View.GONE);
        }
    }

    private void getAllSubjects() {
        HTTP.getAllSubjects(getApplicationContext(), new HTTPRequestListener() {
            @Override
            public void onFailure(@NotNull IOException e) {
                Log.d("HTTP", "FAILED");
                Log.d("HTTP", e.getLocalizedMessage());
                Toast.makeText(ContactTracingActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(boolean success, @NotNull Object object, @NotNull Response response) {
                if (!success) {
                    Toast.makeText(ContactTracingActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    return;
                }

                SubjectsResponse subjectsResponse = (SubjectsResponse) object;
                if (!subjectsResponse.isSuccess()) {
                    Toast.makeText(ContactTracingActivity.this, subjectsResponse.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                subjects.clear();
                subjects.addAll(subjectsResponse.getData().getSubjects());
                subjectsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void getContactTraces() {
        HTTP.getContactTraces(getApplicationContext(), subjectID, endDate, new HTTPRequestListener() {
            @Override
            public void onFailure(@NotNull IOException e) {
                Log.d("HTTP", "FAILED");
                Log.d("HTTP", e.getLocalizedMessage());
                Toast.makeText(ContactTracingActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(boolean success, @NotNull Object object, @NotNull Response response) {
                if (!success) {
                    Toast.makeText(ContactTracingActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    return;
                }

                String adjacencyMatrix = (String) object;
                Log.d("HTTP", adjacencyMatrix);
                if (adjacencyMatrix == null || adjacencyMatrix.isEmpty()) {
                    Toast.makeText(ContactTracingActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    return;
                }

                boolean adjacencyMatrixSaved = Utils.saveAdjacencyMatrixText(getApplicationContext(), adjacencyMatrix);
                if (adjacencyMatrixSaved) {
                    Toast.makeText(getApplicationContext(), "Adjacency matrix is saved to internal storage", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot store the adjacency matrix", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}