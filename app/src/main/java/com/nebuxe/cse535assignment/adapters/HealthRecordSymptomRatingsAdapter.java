package com.nebuxe.cse535assignment.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nebuxe.cse535assignment.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HealthRecordSymptomRatingsAdapter extends RecyclerView.Adapter<HealthRecordSymptomRatingsAdapter.ViewHolder> {

    private Context context;
    private HashMap<String, Integer> symptomRatings;
    private List<String> symptoms = new ArrayList<>();

    private final int[] backgroundResources = new int[]{
            R.drawable.symptom_background_1,
            R.drawable.symptom_background_3,
            R.drawable.symptom_background_5,
            R.drawable.symptom_background_7,
            R.drawable.symptom_background_9,
            R.drawable.symptom_background_2,
            R.drawable.symptom_background_4,
            R.drawable.symptom_background_6,
            R.drawable.symptom_background_8,
            R.drawable.symptom_background_10
    };

    private final int[] emoticons = new int[]{
            R.drawable.ic_smiling,
            R.drawable.ic_smile,
            R.drawable.ic_neutral,
            R.drawable.ic_sad,
            R.drawable.ic_sick
    };

    public HealthRecordSymptomRatingsAdapter(@NonNull Context context, HashMap<String, Integer> symptomRatings) {
        this.context = context;
        this.symptomRatings = symptomRatings;
        for (String symptom : symptomRatings.keySet()) {
            this.symptoms.add(symptom);
        }
    }

    @NonNull
    @Override
    public HealthRecordSymptomRatingsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = layoutInflater.inflate(R.layout.item_health_record_symptom_rating, parent, false);

        return new HealthRecordSymptomRatingsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthRecordSymptomRatingsAdapter.ViewHolder holder, int position) {
        holder.setSymptom(symptoms.get(position));
        holder.setRating(symptomRatings.get(symptoms.get(position)));

        holder.setItemBackground(backgroundResources[position % backgroundResources.length]);
    }

    @Override
    public int getItemCount() {
        return symptomRatings.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvSymptom;
        private ImageView ivRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymptom = itemView.findViewById(R.id.tv_symptom);
            ivRating = itemView.findViewById(R.id.iv_rating);
        }

        public void setSymptom(String symptom) {
            this.tvSymptom.setText(symptom);
        }

        private void setRating(int rating) {
            if (rating == 0) {
                rating = 1;
            }
            ivRating.setBackgroundResource(emoticons[rating - 1]);
        }

        public void setItemBackground(int backgroundResource) {
            this.itemView.setBackgroundResource(backgroundResource);
        }
    }
}
