package com.nebuxe.cse535assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nebuxe.cse535assignment.adapters.SymptomsAdapter;
import com.nebuxe.cse535assignment.callbacks.RecyclerItemEventListener;
import com.nebuxe.cse535assignment.utilities.Constants;

import java.util.HashMap;

public class SymptomsActivity extends AppCompatActivity implements RecyclerItemEventListener {

    private LinearLayout backButton;

    private RecyclerView rvSymptoms;
    private Button bSubmit;

    private LinearLayout rating1;
    private LinearLayout rating2;
    private LinearLayout rating3;
    private LinearLayout rating4;
    private LinearLayout rating5;

    private SymptomsAdapter symptomsAdapter;

    private BottomSheetDialog bottomSheetDialog;

    private HashMap<String, Integer> symptomRatings;
    private String[] symptoms;

    private int selectedSymptomIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        symptoms = getResources().getStringArray(R.array.symptoms);

        unpackBundle();
        bindViews();
        setupBottomSheetDialog();
        setAdapters();
        setEventListeners();
    }

    private void unpackBundle() {
        try {
            Bundle bundle = getIntent().getExtras();

            this.symptomRatings = (HashMap<String, Integer>) bundle.getSerializable(Constants.SYMPTOMS_RATINGS);
            if (this.symptomRatings == null) {
                symptomRatings = new HashMap<>();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void bindViews() {
        backButton = findViewById(R.id.ll_back_button);
        rvSymptoms = findViewById(R.id.rv_symptoms);
        bSubmit = findViewById(R.id.b_submit);

        rvSymptoms.setNestedScrollingEnabled(true);
    }

    private void bindViews(View view) {
        rating1 = view.findViewById(R.id.ll_rating_1);
        rating2 = view.findViewById(R.id.ll_rating_2);
        rating3 = view.findViewById(R.id.ll_rating_3);
        rating4 = view.findViewById(R.id.ll_rating_4);
        rating5 = view.findViewById(R.id.ll_rating_5);
    }

    private void setEventListeners() {

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String symptom : symptoms) {
                    if (!symptomRatings.containsKey(symptom)) {
                        symptomRatings.put(symptom, 0);
                    }
                }

                startMainActivity();
            }
        });

        rating1.setOnClickListener(view -> {
            setRating(1);
            closeRatingOptions();
        });

        rating2.setOnClickListener(view -> {
            setRating(2);
            closeRatingOptions();
        });

        rating3.setOnClickListener(view -> {
            setRating(3);
            closeRatingOptions();
        });

        rating4.setOnClickListener(view -> {
            setRating(4);
            closeRatingOptions();
        });

        rating5.setOnClickListener(view -> {
            setRating(5);
            closeRatingOptions();
        });
    }

    private void setAdapters() {
        symptomsAdapter = new SymptomsAdapter(this, symptoms, symptomRatings);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        rvSymptoms.setLayoutManager(gridLayoutManager);

        rvSymptoms.setAdapter(symptomsAdapter);
        symptomsAdapter.notifyDataSetChanged();
    }

    private void setupBottomSheetDialog() {
        bottomSheetDialog = new BottomSheetDialog(this);

        View view = getLayoutInflater().inflate(R.layout.layout_options_symptoms, null);

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        bottomSheet.setBackgroundResource(R.drawable.bottom_sheet_dialog_background);

        bindViews(view);
    }

    private void setRating(int rating) {
        symptomRatings.put(symptoms[selectedSymptomIndex], rating);
        symptomsAdapter.notifyItemChanged(selectedSymptomIndex);

    }

    private void showRatingOptions() {

        LinearLayout[] ratingLayouts = new LinearLayout[]{rating1, rating2, rating3, rating4, rating5};

        String selectedSymptom = symptoms[selectedSymptomIndex];
        int rating = 0;
        if (symptomRatings.containsKey(selectedSymptom)) {
            rating = symptomRatings.get(selectedSymptom);
        }

        for (int i = 1; i <= 5; i++) {
            if (i == rating) {
                ratingLayouts[i - 1].setBackgroundResource(R.drawable.rating_option_selected_background);
            } else {
                ratingLayouts[i - 1].setBackgroundResource(R.drawable.rating_option_background);
            }
        }

        bottomSheetDialog.show();
    }

    private void closeRatingOptions() {
        bottomSheetDialog.dismiss();
    }


    private void startMainActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FROM, Constants.SYMPTOMS_ACTIVITY);
        bundle.putSerializable(Constants.SYMPTOMS_RATINGS, symptomRatings);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    @Override
    public void onItemClick(int position) {
        selectedSymptomIndex = position;
        showRatingOptions();
    }
}