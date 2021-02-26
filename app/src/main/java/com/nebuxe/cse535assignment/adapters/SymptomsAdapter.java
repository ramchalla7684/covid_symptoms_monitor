package com.nebuxe.cse535assignment.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nebuxe.cse535assignment.R;
import com.nebuxe.cse535assignment.callbacks.RecyclerItemEventListener;

import java.util.HashMap;

public class SymptomsAdapter extends RecyclerView.Adapter<SymptomsAdapter.ViewHolder> {

    private Context context;
    private String[] symptoms;
    private HashMap<String, Integer> symptomRatings;

    private RecyclerItemEventListener recyclerItemEventListener;

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

    public SymptomsAdapter(@NonNull Context context, String[] symptoms, HashMap<String, Integer> symptomRatings) {
        this.context = context;
        this.symptoms = symptoms;
        this.symptomRatings = symptomRatings;

        recyclerItemEventListener = (RecyclerItemEventListener) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = layoutInflater.inflate(R.layout.item_symptom, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setSymptom(symptoms[position]);
        if (symptomRatings != null && symptomRatings.containsKey(symptoms[position]) && symptomRatings.get(symptoms[position]) != 0) {
            holder.setItemBackground(R.drawable.selected_symptom_background);
            holder.setElevation(0);
        } else {
            holder.setItemBackground(backgroundResources[position % backgroundResources.length]);
            holder.setElevation(5);
        }
    }

    @Override
    public int getItemCount() {
        return symptoms.length;
    }


    protected class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvSymptom;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymptom = itemView.findViewById(R.id.tv_symptom);

            setEventListeners();
        }

        private void setEventListeners() {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerItemEventListener.onItemClick(getAdapterPosition());
                }
            });
        }

        public void setSymptom(String symptom) {
            this.tvSymptom.setText(symptom);
        }

        public void setItemBackground(int backgroundResource) {
            this.itemView.setBackgroundResource(backgroundResource);
        }

        public void setElevation(int dp) {
            this.itemView.setElevation(dp);
        }
    }
}
