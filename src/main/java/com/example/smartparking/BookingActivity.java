// BookingActivity.java
package com.example.smartparking;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {
    TextView tvSlotNumber, tvDuration, tvPrice;
    EditText etFromTime, etToTime;
    ImageView btnBack;
    Button btn10, btn20, btn30, btn40, btn50, btn60, btnConfirm;
    EditText etName, etMobile, etVehicleNumber;
    RadioGroup rgPayment;
    CheckBox cbTerms;
    Calendar fromCal = Calendar.getInstance();
    Calendar toCal = Calendar.getInstance();
    String slot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        tvSlotNumber = findViewById(R.id.tvSlotNumber);
        slot = getIntent().getStringExtra("Slot");
        tvSlotNumber.setText(slot != null ? "Slot: " + slot : "Slot: Not Found");

        etFromTime = findViewById(R.id.etFromTime);
        etToTime = findViewById(R.id.etToTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvPrice = findViewById(R.id.tvPrice);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etFromTime.setOnClickListener(v -> showTimePicker(fromCal, etFromTime));
        etToTime.setOnClickListener(v -> showTimePicker(toCal, etToTime));

        btn10 = findViewById(R.id.btn10min);
        btn20 = findViewById(R.id.btn20min);
        btn30 = findViewById(R.id.btn30min);
        btn40 = findViewById(R.id.btn40min);
        btn50 = findViewById(R.id.btn50min);
        btn60 = findViewById(R.id.btn60min);

        setDurationButton(btn10, 10);
        setDurationButton(btn20, 20);
        setDurationButton(btn30, 30);
        setDurationButton(btn40, 40);
        setDurationButton(btn50, 50);
        setDurationButton(btn60, 60);

        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        rgPayment = findViewById(R.id.rgPayment);
        cbTerms = findViewById(R.id.cbTerms);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String mobile = etMobile.getText().toString();
            String vehicle = etVehicleNumber.getText().toString();
            String price = tvPrice.getText().toString().replace("Total: ₹ ", "");
            String fromTime = etFromTime.getText().toString();
            String toTime = etToTime.getText().toString();

            if (name.isEmpty() || mobile.isEmpty() || vehicle.isEmpty()
                    || fromTime.isEmpty() || toTime.isEmpty() || !cbTerms.isChecked()) {
                Toast.makeText(this, "Please fill all fields and agree to terms", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = rgPayment.getCheckedRadioButtonId();
            if (selectedId == R.id.rbOnline) {
                openUpiPayment(price, name);
            } else {
                Intent intent = new Intent(this, ReceiptActivity.class);
                intent.putExtra("receipt", generateReceiptString(name, mobile, vehicle, fromTime, toTime, price));
                startActivity(intent);
            }
        });
    }

    private void openUpiPayment(String amount, String name) {
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", "7745887608@ybl")
                .appendQueryParameter("pn", "Pay & Park")
                .appendQueryParameter("tn", "Booking of Slot - " + slot)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.setPackage("com.google.android.apps.nbu.paisa.user"); // Try GPay directly first

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Intent chooser = Intent.createChooser(new Intent(Intent.ACTION_VIEW, uri), "Pay with UPI");
            startActivity(chooser);
        }
    }

    private void showTimePicker(Calendar calendar, EditText target) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute1);
            target.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.getTime()));
            calculateDurationAndPrice();
        }, hour, minute, false).show();
    }

    private void setDurationButton(Button btn, int durationMinutes) {
        btn.setOnClickListener(v -> {
            fromCal = Calendar.getInstance();
            toCal = (Calendar) fromCal.clone();
            toCal.add(Calendar.MINUTE, durationMinutes);

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            etFromTime.setText(sdf.format(fromCal.getTime()));
            etToTime.setText(sdf.format(toCal.getTime()));

            calculateDurationAndPrice();
        });
    }

    private void calculateDurationAndPrice() {
        if (etFromTime.getText().toString().isEmpty() || etToTime.getText().toString().isEmpty()) return;

        long diff = toCal.getTimeInMillis() - fromCal.getTimeInMillis();
        if (diff <= 0) {
            tvDuration.setText("Invalid time range");
            tvPrice.setText("Total: ₹ 0.00");
            return;
        }

        long minutes = diff / (60 * 1000);
        tvDuration.setText("Duration: " + minutes + " minutes");

        double price = Math.ceil(minutes / 30.0) * 10; // ₹10 per 30 mins
        tvPrice.setText(String.format(Locale.getDefault(), "Total: ₹ %.2f", price));
    }

    private String generateReceiptString(String name, String mobile, String vehicle, String from, String to, String price) {
        return "\nName: " + name +
                "\nMobile: " + mobile +
                "\nVehicle: " + vehicle +
                "\nSlot: " + slot +
                "\nFrom: " + from +
                "\nTo: " + to +
                "\nDuration: " + tvDuration.getText().toString().replace("Duration: ", "") +
                "\nTotal: ₹" + price;
    }
}
