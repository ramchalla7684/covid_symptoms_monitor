package com.nebuxe.cse535assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nebuxe.cse535assignment.models.sqlite.SQLiteManager;
import com.nebuxe.cse535assignment.utilities.SharedPreferencesValues;

import java.util.regex.Pattern;

public class EntryActivity extends AppCompatActivity {

    private EditText etUsername;
    private TextView tvErrorMessage;
    private ImageButton bSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String username = SharedPreferencesValues.getUsername(getApplicationContext());
        if (username.length() > 0) {
            startMainActivity();
        }

        setContentView(R.layout.activity_entry);

        bindViews();
        setEventListeners();
    }

    private void bindViews() {
        etUsername = findViewById(R.id.et_username);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        bSubmit = findViewById(R.id.b_submit);
    }

    private void setEventListeners() {
        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                if (isUsernameValid(username)) {
                    SharedPreferencesValues.setUsername(getApplicationContext(), username.replace(" ", "_"));
                    SQLiteManager.getInstance(getApplicationContext(), username).createSymptomsTable();
                    startMainActivity();
                }
            }
        });
    }


    private boolean isUsernameValid(String username) {
        boolean isValid = true;

        if (username.isEmpty()) {
            tvErrorMessage.setText("Username cannot be empty");
            isValid = false;
        } else if (username.length() < 3) {
            tvErrorMessage.setText("Too short");
            isValid = false;
        } else if (username.length() > 20) {
            tvErrorMessage.setText("Too long");
            isValid = false;
        } else if (!Pattern.matches("[\\p{L} ]+$", username)) {
            tvErrorMessage.setText("Username shouldn't contain any special character");
            isValid = false;
        }

        return isValid;
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}