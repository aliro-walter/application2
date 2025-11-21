package com.walter.myinternshiplogbook;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentDashboardActivity extends AppCompatActivity {

    private ImageView ivProfilePic;
    private ImageButton btnEdit, btnLogout;
    private Button btnUploadPic, btnSaveProfile, btnCaptureProfile, btnViewNews;
    private EditText etStudentName, etRegNo, etStudentNo, etProgram;

    private TextView tipsTextView;  // NEW — where tips will be displayed
    private Button btnViewFeedback; // NEW

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Void> takePicturePreviewLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "StudentProfile";

    private Uri imageUri;
    private boolean isEditing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize views
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnEdit = findViewById(R.id.btnEdit);
        btnLogout = findViewById(R.id.btnLogout);
        btnUploadPic = findViewById(R.id.btnUploadPic);
        btnCaptureProfile = findViewById(R.id.btnCaptureProfile);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        etStudentName = findViewById(R.id.etStudentName);
        etRegNo = findViewById(R.id.etRegNo);
        etStudentNo = findViewById(R.id.etStudentNo);
        etProgram = findViewById(R.id.etProgram);
        btnViewNews = findViewById(R.id.btnViewNews);

        tipsTextView = findViewById(R.id.tipsTextView);  // NEW
        btnViewFeedback = findViewById(R.id.btnViewFeedback); // NEW

        // Shared preferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        // If LoginActivity passed the logged-in email, persist it so validation can use it
        if (getIntent() != null) {
            String emailFromLogin = getIntent().getStringExtra("email");
            if (emailFromLogin != null && !emailFromLogin.trim().isEmpty()) {
                sharedPreferences.edit().putString("email", emailFromLogin.trim()).apply();
            }
        }
        loadProfile();
        setFieldsEditable(isEditing);

        // Load internship tips
        loadInternshipTips();

        // Image pick and camera
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        ivProfilePic.setImageURI(uri);
                        saveImageUri(uri);
                        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    }
                });

        // permission launcher
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        takePicturePreviewLauncher.launch(null);
                    } else {
                        Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        takePicturePreviewLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                (Bitmap bitmap) -> {
                    if (bitmap != null) {
                        ivProfilePic.setImageBitmap(bitmap);
                        try {
                            File out = new File(getCacheDir(), "profile_pic.jpg");
                            FileOutputStream fos = new FileOutputStream(out);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                            fos.flush();
                            fos.close();
                            Uri saved = Uri.fromFile(out);
                            imageUri = saved;
                            saveImageUri(saved);
                            Toast.makeText(this, "Profile picture captured", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to save captured image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnUploadPic.setOnClickListener(v -> {
            if (isEditing) pickImageLauncher.launch("image/*");
            else Toast.makeText(this, "Click Edit first to change picture", Toast.LENGTH_SHORT).show();
        });

        btnCaptureProfile.setOnClickListener(v -> {
            if (!isEditing) {
                Toast.makeText(this, "Click Edit first to change picture", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    takePicturePreviewLauncher.launch(null);
                } else {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show();
            }
        });

        // Save profile and go to Daily Log
        btnSaveProfile.setOnClickListener(v -> saveProfileAndNavigate());

        // Edit toggle
        btnEdit.setOnClickListener(v -> {
            isEditing = !isEditing;
            setFieldsEditable(isEditing);
            Toast.makeText(this,
                    isEditing ? "You can now edit your profile" : "Editing disabled",
                    Toast.LENGTH_SHORT).show();
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // View Feedback button
        if (btnViewFeedback != null) {
            btnViewFeedback.setOnClickListener(v -> {
                String studentName = sharedPreferences.getString("name", etStudentName.getText().toString().trim());
                if (studentName == null || studentName.trim().isEmpty()) {
                    Toast.makeText(this, "Please save your profile with your full name first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(StudentDashboardActivity.this, Feedback.class);
                intent.putExtra("student_name", studentName);
                startActivity(intent);
            });
        }

        // View News button
        if (btnViewNews != null) {
            btnViewNews.setOnClickListener(v -> {
                Intent intent = new Intent(StudentDashboardActivity.this, NewsActivity.class);
                startActivity(intent);
            });
        }

        // Back to Login button (clears saved profile)
        Button btnBackToLogin = findViewById(R.id.btnBackToLogin);
        if (btnBackToLogin != null) {
            btnBackToLogin.setOnClickListener(v -> {
                // Clear saved profile
                getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().clear().apply();
                // Go back to login
                Intent intent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    // --------------------------------------------------------------------
    // REAL WORKING WEBSCRAPING FUNCTION
    // --------------------------------------------------------------------
    private void loadInternshipTips() {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                // REAL WEBSITE with real readable text
                Document doc = Jsoup.connect("https://www.skillsyouneed.com/start-learning.html").get();

                Elements paragraphs = doc.select("div.content p");

                ArrayList<String> tips = new ArrayList<>();

                for (Element p : paragraphs) {
                    String text = p.text().trim();
                    if (!text.isEmpty()) {
                        tips.add(text);
                    }
                    if (tips.size() == 6) break; // limit to 6 tips
                }

                runOnUiThread(() -> {
                    if (!tips.isEmpty()) {
                        StringBuilder formatted = new StringBuilder();
                        for (String tip : tips) {
                            formatted.append("• ").append(tip).append("\n\n");
                        }
                        tipsTextView.setText(formatted.toString());
                    } else {
                        tipsTextView.setText("No internship tips available at the moment.");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        tipsTextView.setText("Unable to load internship tips. Check your internet.")
                );
            }
        });
    }

    // --------------------------------------------------------------------

    private void saveProfileAndNavigate() {
        String name = etStudentName.getText().toString().trim();
        String regNo = etRegNo.getText().toString().trim();
        String studentNo = etStudentNo.getText().toString().trim();
        String program = etProgram.getText().toString().trim();

        if (name.isEmpty() || regNo.isEmpty() || studentNo.isEmpty() || program.isEmpty()) {
            Toast.makeText(this, "Please fill all fields before saving", Toast.LENGTH_SHORT).show();
            return;
        }

        // Email-based validation for registration number
        String emailToCheck = sharedPreferences.getString("email", null);
        if (emailToCheck != null && !emailToCheck.trim().isEmpty()) {
            // Only apply this rule for student emails that contain the 'std' subdomain
            String lower = emailToCheck.toLowerCase().trim();
            // Apply rule for student emails that contain the 'std' marker after the @ (e.g. @std.must.ac.ug or @std)
            if (lower.contains("@std")) {
                String local = lower.split("@")[0];
                // Find the last continuous group of digits inside the local part (e.g. '2023bit111' -> '111')
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)").matcher(local);
                String lastDigits = "";
                while (m.find()) {
                    lastDigits = m.group(1);
                }
                if (lastDigits == null || lastDigits.isEmpty() || !regNo.contains(lastDigits)) {
                    Toast.makeText(this, "Registration number must include the student digits present in your email address.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("regNo", regNo);
        editor.putString("studentNo", studentNo);
        editor.putString("program", program);
        if (imageUri != null) editor.putString("imageUri", imageUri.toString());
        editor.apply();

        isEditing = false;
        setFieldsEditable(false);

        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(StudentDashboardActivity.this, DailyLogEntryActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadProfile() {
        String name = sharedPreferences.getString("name", "");
        String regNo = sharedPreferences.getString("regNo", "");
        String studentNo = sharedPreferences.getString("studentNo", "");
        String program = sharedPreferences.getString("program", "");
        String savedUri = sharedPreferences.getString("imageUri", null);

        etStudentName.setText(name);
        etRegNo.setText(regNo);
        etStudentNo.setText(studentNo);
        etProgram.setText(program);

        if (savedUri != null) {
            try {
                imageUri = Uri.parse(savedUri);
                ivProfilePic.setImageURI(imageUri);
            } catch (Exception e) {
                ivProfilePic.setImageResource(android.R.drawable.ic_menu_camera);
            }
        }
    }

    private void saveImageUri(Uri uri) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("imageUri", uri.toString());
        editor.apply();
    }

    private void setFieldsEditable(boolean editable) {
        etStudentName.setEnabled(editable);
        etRegNo.setEnabled(editable);
        etStudentNo.setEnabled(editable);
        etProgram.setEnabled(editable);
        btnUploadPic.setEnabled(editable);
        btnCaptureProfile.setEnabled(editable);
        btnSaveProfile.setEnabled(editable);
    }
}
