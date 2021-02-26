package com.nebuxe.cse535assignment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.nebuxe.cse535assignment.callbacks.CameraSessionListener;
import com.nebuxe.cse535assignment.callbacks.OnFrameListener;
import com.nebuxe.cse535assignment.utilities.Constants;
import com.nebuxe.cse535assignment.utilities.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class HeartRateActivity extends AppCompatActivity implements CameraSessionListener, OnFrameListener {

    private final int PERMISSIONS_REQUEST_CODE = 111;

    private TextureView cameraPreview;
    private RelativeLayout previewOverlay;
    private RelativeLayout recorderOverlay;

    private Button bReady;
    private Button bStopRecorder;

    private TextView tvTime;
    private LineChart lineChart;

    private CameraSession cameraSession;
    private CountDownTimer countDownTimer;

    private List<Double> colorIntensities = new ArrayList<>();
    private LineData lineData = new LineData(new LineDataSet(new ArrayList<Entry>(), null));

    private BlockingQueue<Bitmap> bufferQueue = new LinkedBlockingDeque<>();
    private Handler threadHandler;
    private Runnable runnable;

    private final int RECORD_TIME_LIMIT_IN_MILLIS = 45 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);

        bindViews();
        setEventListeners();

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

        boolean hasPermissions = checkPermissions();
        if (hasPermissions) {
            cameraSession = new CameraSession(this, cameraPreview);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (cameraSession != null && cameraSession.getCurrentPreviewStatus() == CameraSession.PreviewStatus.PAUSED) {
                cameraSession.openCamera();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (cameraSession != null) {
            cameraSession.closeCamera();
            cameraSession.setCurrentPreviewStatus(CameraSession.PreviewStatus.PAUSED);
        }

        if (threadHandler != null) {
            threadHandler.removeCallbacks(runnable);
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void bindViews() {
        cameraPreview = findViewById(R.id.fl_camera_preview);
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
                previewOverlay.setVisibility(View.GONE);
                recorderOverlay.setVisibility(View.VISIBLE);
                try {
                    cameraSession.startRecording();
                    startBackgroundThread();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        bStopRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                cameraSession.closeCamera();
            }
        });
    }

    private boolean checkPermissions() {
        if (!hasCameraPermissions()) {
            askPermissions();
            return false;
        }
        return true;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(RECORD_TIME_LIMIT_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int remainingSeconds = (int) (millisUntilFinished / 1000);
                tvTime.setText("00:" + (remainingSeconds < 10 ? "0" + remainingSeconds : remainingSeconds));
            }

            @Override
            public void onFinish() {
                cameraSession.closeCamera();
            }
        };
        countDownTimer.start();
    }

    private void startBackgroundThread() {
        HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();

        threadHandler = new Handler(looper);
        runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (bufferQueue.size() != 0) {
                        Log.d("CAMERA QUEUE SIZE", bufferQueue.size() + "");
                        try {
                            Bitmap bitmap = bufferQueue.take();
                            int width = bitmap.getWidth();
                            int height = bitmap.getHeight();
                            int[] pixels = new int[width * height];
                            bitmap.getPixels(pixels, 0, width, 0, 0, width, width);
                            double intensity = getColorIntensity(pixels);

                            pixels = null;
                            bitmap = null;
                            addToChart(intensity);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (cameraSession.getCurrentPreviewStatus() == CameraSession.PreviewStatus.CLOSED) {
                        startMainActivity();
                        break;
                    }
                }
            }
        };

        threadHandler.postDelayed(runnable, 0);
    }

    private void addToChart(double val) {
        colorIntensities.add(val);

        lineData.addEntry(new Entry(colorIntensities.size(), (float) val), 0);
        if (lineData.getEntryCount() > 40) {
            lineData.getDataSetByIndex(0).removeFirst();
        }
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private double getColorIntensity(ByteBuffer[] byteBuffers) {
        byte[] yBytes = new byte[byteBuffers[0].capacity()];
        byteBuffers[0].get(yBytes);
        byteBuffers[0].clear();

        byte[] vBytes = new byte[byteBuffers[2].capacity()];
        byteBuffers[2].get(vBytes);
        byteBuffers[2].clear();

        return getColorIntensity(yBytes, null, vBytes);
    }


    private double getColorIntensity(int[] pixels) {
        long sum = 0;
        for (int i = 0; i < pixels.length; i++) {
//            sum += ((pixels[i] >> 16) & 0xff);
            sum += Color.red(pixels[i]);
        }
        Log.d("COLOR", (double) sum / pixels.length + "");

        return (double) sum / pixels.length;
    }

    private double getColorIntensity(byte[] yBytes, byte[] uBytes, byte[] vBytes) {
        long rSum = 0;
        for (int i = 0; i < yBytes.length; i++) {
            rSum += (yBytes[i] + 128) + 1.14 * (vBytes[Math.min(i / 2, vBytes.length - 1)] + 128 - 128);
        }
        return (double) rSum / yBytes.length;
    }

    private boolean isHardwareCameraAvailable() {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private boolean hasCameraPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasStoragePermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void askPermissions() {
        String[] requiredPermissions = new String[]{Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE);
    }

    private void startMainActivity() {

        int nPeaks = Utils.countPeaks(colorIntensities, 6);

        Bundle bundle = new Bundle();
        bundle.putString(Constants.FROM, Constants.HEART_RATE_ACTIVITY);
        bundle.putInt(Constants.HEART_RATE, nPeaks * 60 / (RECORD_TIME_LIMIT_IN_MILLIS / 1000));

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "Requires camera permissions", Toast.LENGTH_LONG).show();
                onBackPressed();
                finish();
            } else {
                recreate();
            }
        }
    }

    @Override
    public void onCameraOpened() {

    }

    @Override
    public void onCameraClosed() {

    }

    @Override
    public void onRecordingStarted() {
        startTimer();
    }

    @Override
    public void onNewFrameAvailable(ByteBuffer[] byteBuffers) {

    }

    @Override
    public void onNewFrameAvailable(Bitmap bitmap) {
        try {
            bufferQueue.put(bitmap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}