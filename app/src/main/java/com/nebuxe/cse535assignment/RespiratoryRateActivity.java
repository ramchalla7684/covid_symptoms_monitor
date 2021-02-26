package com.nebuxe.cse535assignment;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.nebuxe.cse535assignment.utilities.Constants;
import com.nebuxe.cse535assignment.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class RespiratoryRateActivity extends AppCompatActivity implements SensorEventListener {

    private RelativeLayout previewOverlay;
    private RelativeLayout recorderOverlay;

    private Button bReady;
    private Button bStopRecorder;

    private TextView tvTime;
    private LineChart lineChart;

    private SensorManager sensorManager;

    private CountDownTimer countDownTimer;

    private List<Double> deltaAlongZ = new ArrayList<>();
    private LineData lineData;

    private final int RECORD_TIME_LIMIT_IN_MILLIS = 45 * 1000;

    protected enum SensorStatus {
        READY,
        ACTIVE,
        PAUSED
    }

    private SensorStatus currentSensorStatus = SensorStatus.READY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respiratory_rate);

        bindViews();
        setEventListeners();
        initLineChart();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentSensorStatus == SensorStatus.PAUSED) {
            attachSensor();
            initLineChart();
            if (countDownTimer != null) {
                countDownTimer.start();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeSensor();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void attachSensor() {
        sensorManager.registerListener(this, this.sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
        currentSensorStatus = SensorStatus.ACTIVE;
    }

    private void removeSensor() {
        this.sensorManager.unregisterListener(this);
        currentSensorStatus = SensorStatus.PAUSED;
    }

    private void bindViews() {
        previewOverlay = findViewById(R.id.rl_preview_overlay);
        recorderOverlay = findViewById(R.id.rl_recorder_overlay);

        bReady = findViewById(R.id.b_ready);
        bStopRecorder = findViewById(R.id.b_stop_recorder);

        tvTime = findViewById(R.id.tv_time);
        lineChart = findViewById(R.id.line_chart);
    }

    private void setEventListeners() {

        bReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimerWithDelay(3 * 1000);
            }
        });

        bStopRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                removeSensor();
                startMainActivity();
            }
        });
    }

    private void initLineChart() {
        lineData = new LineData(new LineDataSet(new ArrayList<Entry>(), null));
        lineChart.getDescription().setEnabled(false);

        lineChart.setDrawBorders(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawMarkers(false);
        lineChart.getLegend().setEnabled(false);

        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setEnabled(false);
        ((LineDataSet) lineData.getDataSetByIndex(0)).setColor(getApplicationContext().getResources().getColor(R.color.light_red, null));
        ((LineDataSet) lineData.getDataSetByIndex(0)).setLineWidth(2);
        ((LineDataSet) lineData.getDataSetByIndex(0)).setDrawCircles(false);
        ((LineDataSet) lineData.getDataSetByIndex(0)).setDrawValues(false);

        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void startTimerWithDelay(final int delay) {

        bReady.setText("Preparing...");

        countDownTimer = new CountDownTimer(RECORD_TIME_LIMIT_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int remainingSeconds = (int) (millisUntilFinished / 1000);
                tvTime.setText("00:" + (remainingSeconds < 10 ? "0" + remainingSeconds : remainingSeconds));
            }

            @Override
            public void onFinish() {
                removeSensor();
                vibrate();
                startMainActivity();
            }
        };

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                attachSensor();
                previewOverlay.setVisibility(View.GONE);
                recorderOverlay.setVisibility(View.VISIBLE);
                countDownTimer.start();
            }
        }, delay);
    }

    private void addToChart(double val) {
        deltaAlongZ.add(val);

        lineData.addEntry(new Entry(deltaAlongZ.size(), (float) val), 0);
        if (lineData.getEntryCount() > 25) {
            lineData.getDataSetByIndex(0).removeFirst();
        }
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private void startMainActivity() {
        int nPeaks = Utils.countPeaks(deltaAlongZ, 4);

        Bundle bundle = new Bundle();
        bundle.putString(Constants.FROM, Constants.RESPIRATORY_RATE_ACTIVITY);
        bundle.putInt(Constants.RESPIRATORY_RATE, nPeaks * 60 / (RECORD_TIME_LIMIT_IN_MILLIS / 1000));

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(800);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        addToChart(event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}