package com.example.smartparking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "SessionPrefs";
    private static final String KEY_LOGIN_TIME = "loginTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnYesLogout = findViewById(R.id.btnYesLogout);
        Button btnNoStay = findViewById(R.id.btnNoStay);

        btnBack.setOnClickListener(v -> finish());

        btnYesLogout.setOnClickListener(v -> {
            // Clear session data
            sharedPreferences.edit().remove(KEY_LOGIN_TIME).apply();
            FirebaseAuth.getInstance().signOut();

            // Redirect to login
            Intent intent = new Intent(LogoutActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnNoStay.setOnClickListener(v -> {
            // Return to home page
            startActivity(new Intent(LogoutActivity.this, HomePageActivity.class));
            finish();
        });
    }
}