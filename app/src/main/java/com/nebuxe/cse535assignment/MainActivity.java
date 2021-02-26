package com.nebuxe.cse535assignment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.nebuxe.cse535assignment.callbacks.FusedLocationListener;
import com.nebuxe.cse535assignment.callbacks.HTTPRequestListener;
import com.nebuxe.cse535assignment.models.SymptomsModel;
import com.nebuxe.cse535assignment.pojos.UploadResponse;
import com.nebuxe.cse535assignment.services.HTTP;
import com.nebuxe.cse535assignment.services.LocationService;
import com.nebuxe.cse535assignment.utilities.Constants;
import com.nebuxe.cse535assignment.views.DialogView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_CODE = 222;

    private LinearLayout menuOptions;
    private LinearLayout heartRateOption;
    private LinearLayout respiratoryRateOption;
    private Button bSymptoms;
    private Button bUpload;

    private ImageView icHeartRate;
    private ImageView icRespiratoryRate;
    private TextView tvHeartRate;
    private TextView tvRespiratoryRate;

    private int heartRate = 0;
    private int respiratoryRate = 0;
    private HashMap<String, Integer> symptomRatings;

    private boolean hasGPSConsent = false;

    private boolean isLocationSet = false;
    private double latitude;
    private double longitude;

    private Dialog gpsConsentDialog;
    private Button bConsent;

    private long date = 0;
    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupDialogs();
        bindViews();
        setEventListeners();

        date = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasGPSConsent && (!hasGPSPermissions() || !isGPSEnabled())) {
            gpsConsentDialog.show();
        } else if (hasGPSPermissions() && isGPSEnabled()) {
            fetchLocationData();
        }

        updateActionStyles();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (locationService != null) {
            locationService.removeLocationUpdates();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        unpackBundle(intent);
    }

    private void unpackBundle(Intent intent) {
        try {
            Bundle bundle = intent.getExtras();

            String from = bundle.getString(Constants.FROM);
            if (from.equals(Constants.HEART_RATE_ACTIVITY)) {
                this.heartRate = bundle.getInt(Constants.HEART_RATE);
            } else if (from.equals(Constants.RESPIRATORY_RATE_ACTIVITY)) {
                this.respiratoryRate = bundle.getInt(Constants.RESPIRATORY_RATE);
            } else if (from.equals(Constants.SYMPTOMS_ACTIVITY)) {
                this.symptomRatings = (HashMap<String, Integer>) bundle.getSerializable(Constants.SYMPTOMS_RATINGS);
            }

            Log.d("BUNDLE", heartRate + " " + respiratoryRate + " " + symptomRatings);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void openGPSSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private boolean hasGPSPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void askGPSPermissions() {
        String[] requiredPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE);
    }


    private void updateActionStyles() {
        if (heartRate > 0) {
            showHeartBeatRateValue();
            SymptomsModel.saveHeartBeatRate(getApplicationContext(), date, heartRate);
        } else {
            tvHeartRate.setVisibility(View.GONE);
            icHeartRate.setVisibility(View.VISIBLE);
        }

        if (respiratoryRate > 0) {
            showRespiratoryRateValue();
            SymptomsModel.saveRespiratoryRate(getApplicationContext(), date, respiratoryRate);
        } else {
            tvRespiratoryRate.setVisibility(View.GONE);
            icRespiratoryRate.setVisibility(View.VISIBLE);
        }

        if (symptomRatings != null) {
            SymptomsModel.saveSymptomsRatings(getApplicationContext(), date, symptomRatings);
        }

        if (heartRate > 0 && respiratoryRate > 0 && symptomRatings != null && isLocationSet) {
            SymptomsModel.saveLocation(getApplicationContext(), date, latitude, longitude);
            showUploadButton();
            Toast.makeText(this, "Record saved", Toast.LENGTH_LONG).show();
        } else if (heartRate > 0 || respiratoryRate > 0 || symptomRatings != null) {
            Toast.makeText(this, "Record saved", Toast.LENGTH_LONG).show();
        }
    }

    private void bindViews() {
        menuOptions = findViewById(R.id.ll_menu_options);

        heartRateOption = findViewById(R.id.ll_heart_rate_option);
        respiratoryRateOption = findViewById(R.id.ll_respiratory_rate_option);
        bSymptoms = findViewById(R.id.b_symptoms);
        bUpload = findViewById(R.id.b_upload);

        icHeartRate = findViewById(R.id.ic_heart_rate);
        icRespiratoryRate = findViewById(R.id.ic_respiratory_rate);

        tvHeartRate = findViewById(R.id.tv_heart_rate);
        tvRespiratoryRate = findViewById(R.id.tv_respiratory_rate);

        bConsent = (Button) gpsConsentDialog.findViewById(R.id.b_consent);
    }

    private void setupDialogs() {
        gpsConsentDialog = new DialogView(this).getDialog(R.layout.layout_dialog_gps_consent, false);
    }

    private void startNewActivity(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void setEventListeners() {
        menuOptions.setOnClickListener(this::showPopupMenu);
        heartRateOption.setOnClickListener(view -> startNewActivity(new Intent(getApplicationContext(), HeartRateActivity.class)));
        respiratoryRateOption.setOnClickListener(view -> startNewActivity(new Intent(getApplicationContext(), RespiratoryRateActivity.class)));

        bSymptoms.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SymptomsActivity.class);

            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.SYMPTOMS_RATINGS, symptomRatings);
            intent.putExtras(bundle);

            startNewActivity(intent);
        });

        bUpload.setOnClickListener(view -> {
            uploadDatabase();
        });

        bConsent.setOnClickListener(view -> {
            gpsConsentDialog.dismiss();

            hasGPSConsent = true;
            if (!hasGPSPermissions()) {
                askGPSPermissions();
            } else if (!isGPSEnabled()) {
                openGPSSettings();
            }
        });
    }

    private void showHeartBeatRateValue() {
        icHeartRate.setVisibility(View.GONE);
        tvHeartRate.setVisibility(View.VISIBLE);
        tvHeartRate.setText(heartRate + " BPM");
    }

    private void showRespiratoryRateValue() {
        icRespiratoryRate.setVisibility(View.GONE);
        tvRespiratoryRate.setVisibility(View.VISIBLE);
        tvRespiratoryRate.setText(respiratoryRate + " BPM");
    }

    private void showUploadButton() {
        bUpload.setVisibility(View.VISIBLE);
    }

    private void fetchLocationData() {
        if (locationService == null) {
            locationService = new LocationService(getApplicationContext());
        }

        FusedLocationListener fusedLocationListener = (location -> {
            if (location == null) {
                Log.d("LOCT", "null");
                return;
            } else {
                Log.d("LOCT", location.getLatitude() + " " + location.getLongitude());
                locationService.removeLocationUpdates();

                latitude = location.getLatitude();
                longitude = location.getLongitude();
                isLocationSet = true;
            }
        });

        locationService.getLocation(fusedLocationListener);
        locationService.requestLocationUpdates(fusedLocationListener);
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

                startNewActivity(new Intent(getApplicationContext(), FarewellActivity.class));
                finish();
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());

        popupMenu.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "Requires GPS permissions", Toast.LENGTH_LONG).show();
                finish();
            } else {
                if (!isGPSEnabled()) {
                    openGPSSettings();
                }
            }
        }
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_health_records) {
            startNewActivity(new Intent(getApplicationContext(), SymptomsHistoryActivity.class));
        } else if (item.getItemId() == R.id.menu_contact_tracing) {
            startNewActivity(new Intent(getApplicationContext(), ContactTracingActivity.class));
        } else {
            return false;
        }

        return true;
    }
}