package com.example.smartparking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class HomePageActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "SessionPrefs";
    private static final String KEY_LOGIN_TIME = "loginTime";
    private static final long SESSION_TIMEOUT = 15 * 60 * 1000; // 15 minutes in milliseconds

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private Button bookSlot1, bookSlot2, bookSlot3, bookSlot4;
    private ImageView menuIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize UI components
        bookSlot1 = findViewById(R.id.bookSlot1);
        bookSlot2 = findViewById(R.id.bookSlot2);
        bookSlot3 = findViewById(R.id.bookSlot3);
        bookSlot4 = findViewById(R.id.bookSlot4);
        menuIcon = findViewById(R.id.menuIcon);

        // Set click listeners
        bookSlot1.setOnClickListener(view -> openBookingScreen("A-1"));
        bookSlot2.setOnClickListener(view -> openBookingScreen("A-2"));
        bookSlot3.setOnClickListener(view -> openBookingScreen("A-3"));
        bookSlot4.setOnClickListener(view -> openBookingScreen("A-4"));
        menuIcon.setOnClickListener(this::showPopupMenu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSessionTimeout();
    }

    private void checkSessionTimeout() {
        long loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - loginTime > SESSION_TIMEOUT) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            logoutUser();
        }
    }

    private void logoutUser() {
        // Clear session data
        sharedPreferences.edit().remove(KEY_LOGIN_TIME).apply();
        mAuth.signOut();

        // Redirect to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void openBookingScreen(String slot) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("Slot", slot);
        intent.putExtra("FromTime", "10:00 AM");
        intent.putExtra("ToTime", "12:00 PM");
        startActivity(intent);
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.home_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_my_bookings) {
                startActivity(new Intent(this, BookingsPaymentActivity.class));
                return true;
            } else if (item.getItemId() == R.id.menu_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (item.getItemId() == R.id.menu_help) {
                startActivity(new Intent(this, HelpAboutActivity.class));
                return true;
            } else if (item.getItemId() == R.id.menu_logout) {
                startActivity(new Intent(this, LogoutActivity.class));
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
}