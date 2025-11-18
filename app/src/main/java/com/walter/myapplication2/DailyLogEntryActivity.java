package com.walter.myapplication2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DailyLogEntryActivity extends AppCompatActivity {

    private EditText etDay, etName, etRegNo, etActivity, etProblem, etSolutions, etSkills;
    private Button btnUploadPhoto, btnCapturePhoto, btnSaveLog; // removed btnBackToDashboard
    private ImageButton btnBack, btnNext;
    private ImageView ivEvidence;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Void> takePicturePreviewLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    private LogDBHelper logDBHelper; // DB helper for saving logs
    private byte[] evidenceImageBytes = null; // holds captured/uploaded image bytes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_log_entry);

        // Initialize Views
        etDay = findViewById(R.id.etDay);
        etName = findViewById(R.id.etName); // ✅ Added name field
        etRegNo = findViewById(R.id.etRegNo);
        etActivity = findViewById(R.id.etActivity);
        etProblem = findViewById(R.id.etProblem);
        etSolutions = findViewById(R.id.etSolutions);
        etSkills = findViewById(R.id.etSkills);

        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto);
        btnSaveLog = findViewById(R.id.btnSaveLog);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        ivEvidence = findViewById(R.id.ivEvidence);

        // Initialize DB helper
        logDBHelper = new LogDBHelper(this);

        // If date field is empty, set today's date
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (etDay.getText() == null || etDay.getText().toString().trim().isEmpty()) {
            etDay.setText(today);
        }

        // Permission requester
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        try {
                            takePicturePreviewLauncher.launch(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Camera permission is required to capture photos", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // ✅ Image Picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        ivEvidence.setImageURI(uri);
                        // Convert selected image to bytes later if needed; for now leave evidenceImageBytes null
                    }
                });

        // ✅ Camera capture (preview bitmap)
        takePicturePreviewLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                (Bitmap bitmap) -> {
                    if (bitmap != null) {
                        ivEvidence.setImageBitmap(bitmap);
                        // Convert Bitmap to byte[]
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        evidenceImageBytes = baos.toByteArray();
                    }
                });

        btnUploadPhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnCapturePhoto.setOnClickListener(v -> {
            try {
                // Check runtime permission first
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    takePicturePreviewLauncher.launch(null);
                } else {
                    // Request permission; the registered launcher will start camera after grant
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Save Log Button
        btnSaveLog.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String day = etDay.getText().toString().trim();
            String regNo = etRegNo.getText().toString().trim();
            String activity = etActivity.getText().toString().trim();
            String problem = etProblem.getText().toString().trim();
            String solutions = etSolutions.getText().toString().trim();
            String skills = etSkills.getText().toString().trim();

            if (name.isEmpty() || day.isEmpty() || activity.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = logDBHelper.insertLog(today, day, regNo, name, activity, problem, solutions, skills, evidenceImageBytes);

            if (success) {
                Toast.makeText(this, "Log submitted successfully!", Toast.LENGTH_SHORT).show();
                // Return to dashboard after successful submission
                startActivity(new Intent(this, StudentDashboardActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to save log. Try again.", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Navigation Buttons
        // Use existing ImageButton `btnBack` for returning to the dashboard (no separate btnBackToDashboard in layout)
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentDashboardActivity.class));
            finish();
        });

        btnNext.setOnClickListener(v -> {
            // Pass student name to Feedback activity
            String studentName = etName.getText() != null ? etName.getText().toString().trim() : "";
            if (studentName.isEmpty()) {
                // try to read from SharedPreferences as fallback
                String spName = getSharedPreferences("StudentProfile", MODE_PRIVATE).getString("name", "");
                studentName = spName != null ? spName : "";
            }
            Intent intent = new Intent(DailyLogEntryActivity.this, Feedback.class);
            intent.putExtra("student_name", studentName);
            startActivity(intent);
            finish();
        });
    }
}
