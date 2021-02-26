package com.nebuxe.cse535assignment.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.nebuxe.cse535assignment.R;
import com.nebuxe.cse535assignment.pojos.HealthRecord;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HealthRecordsAdapter extends RecyclerView.Adapter<HealthRecordsAdapter.ViewHolder> {

    private Context context;
    private List<HealthRecord> healthRecords;

    public HealthRecordsAdapter(@NonNull Context context, List<HealthRecord> healthRecords) {
        this.context = context;
        this.healthRecords = healthRecords;
    }

    @NonNull
    @Override
    public HealthRecordsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = layoutInflater.inflate(R.layout.item_health_record, parent, false);

        return new HealthRecordsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthRecordsAdapter.ViewHolder holder, int position) {
        holder.setDate(healthRecords.get(position).getDate());
        holder.setHeartRate(healthRecords.get(position).getHeartBeatRate());
        holder.setRespiratoryRate(healthRecords.get(position).getRespiratoryRate());
        holder.setSymptomRatings(healthRecords.get(position).getSymptomRatings());
    }

    @Override
    public int getItemCount() {
        return healthRecords.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDate;
        private TextView tvHeartRate;
        private TextView tvRespiratoryRate;
        private RecyclerView rvSymptomRatings;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvHeartRate = itemView.findViewById(R.id.tv_heart_rate);
            tvRespiratoryRate = itemView.findViewById(R.id.tv_respiratory_rate);
            rvSymptomRatings = itemView.findViewById(R.id.rv_symptom_ratings);
        }

        public void setDate(long date) {
            String d = new SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(date);
            this.tvDate.setText(d);
        }

        public void setHeartRate(int bpm) {
            this.tvHeartRate.setText(bpm + " BPM");
        }

        public void setRespiratoryRate(int bpm) {
            this.tvRespiratoryRate.setText(bpm + " BPM");
        }

        public void setSymptomRatings(HashMap<String, Integer> symptomRatings) {

            FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(context);
            rvSymptomRatings.setLayoutManager(flexboxLayoutManager);


            HealthRecordSymptomRatingsAdapter healthRecordSymptomRatingsAdapter = new HealthRecordSymptomRatingsAdapter(context, symptomRatings);
            rvSymptomRatings.setAdapter(healthRecordSymptomRatingsAdapter);

            healthRecordSymptomRatingsAdapter.notifyDataSetChanged();
        }
    }
}
