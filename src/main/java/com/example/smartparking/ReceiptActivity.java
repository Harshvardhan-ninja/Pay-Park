package com.example.smartparking;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReceiptActivity extends AppCompatActivity {

    TextView tvReceiptDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        tvReceiptDetails = findViewById(R.id.tvReceiptDetails);

        // Get intent data
        String receiptText = getIntent().getStringExtra("receipt");
        if (receiptText == null) {
            Toast.makeText(this, "No receipt found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvReceiptDetails.setText(receiptText);

        // Generate PDF after a short delay to allow layout rendering
        tvReceiptDetails.postDelayed(this::generatePdfFromUI, 500);
    }

    private void generatePdfFromUI() {
        View contentView = getWindow().getDecorView().findViewById(android.R.id.content);

        contentView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(contentView.getWidth(), contentView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        contentView.draw(canvas);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        page.getCanvas().drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        String fileName = "Receipt_" + System.currentTimeMillis() + ".pdf";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            // Share intent
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Share Receipt PDF"));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "PDF generation failed", Toast.LENGTH_SHORT).show();
        }
    }
}
