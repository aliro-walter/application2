package com.walter.myinternshiplogbook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DailyLogEntryActivity extends AppCompatActivity {

    private EditText etDay, etName, etRegNo, etActivity, etProblem, etSolutions, etSkills;
    private Button btnUploadPhoto, btnCapturePhoto, btnSaveLog, btnShareLog;
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
        etName = findViewById(R.id.etName);
        etRegNo = findViewById(R.id.etRegNo);
        etActivity = findViewById(R.id.etActivity);
        etProblem = findViewById(R.id.etProblem);
        etSolutions = findViewById(R.id.etSolutions);
        etSkills = findViewById(R.id.etSkills);

        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto);
        btnSaveLog = findViewById(R.id.btnSaveLog);
        btnShareLog = findViewById(R.id.btnShareLog);
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

        // Image Picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            ivEvidence.setImageURI(uri);
                            // Convert URI to Bitmap, then to byte[]
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (inputStream != null) {
                                inputStream.close();
                            }

                            if (bitmap != null) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                                evidenceImageBytes = baos.toByteArray();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Camera capture (preview bitmap)
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

        // Save Log Button
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

        // Share Log Button
        btnShareLog.setOnClickListener(v -> {
            String logContent = "Daily Log Entry:\n" +
                    "Date: " + etDay.getText().toString() + "\n" +
                    "Name: " + etName.getText().toString() + "\n" +
                    "Registration No: " + etRegNo.getText().toString() + "\n" +
                    "Activity: " + etActivity.getText().toString() + "\n" +
                    "Problem: " + etProblem.getText().toString() + "\n" +
                    "Solutions: " + etSolutions.getText().toString() + "\n" +
                    "Skills: " + etSkills.getText().toString();

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, logContent);
            shareIntent.setType("text/plain");

            startActivity(Intent.createChooser(shareIntent, "Share Log via"));
        });

        btnNext.setOnClickListener(v -> {
            String studentName = etName.getText() != null ? etName.getText().toString().trim() : "";
            if (studentName.isEmpty()) {
                String spName = getSharedPreferences("StudentProfile", MODE_PRIVATE).getString("name", "");
                studentName = spName != null ? spName : "";
            }
            Intent intent = new Intent(DailyLogEntryActivity.this, Feedback.class);
            intent.putExtra("student_name", studentName);
            startActivity(intent);
            finish();
        });
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentDashboardActivity.class));
            finish();
        });
    }
}
