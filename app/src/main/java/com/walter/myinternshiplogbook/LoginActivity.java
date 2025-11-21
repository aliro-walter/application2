package com.walter.myinternshiplogbook;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


// ✅ Main Login Activity
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup;
    private UserDBHelper dbHelper;  // Database helper instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);

        // Initialize DB
        dbHelper = new UserDBHelper(this);

        // ---- Login button logic ----
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Validate user credentials
            User user = dbHelper.validateUserWithoutRole(email, password);
            if (user != null) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                Intent intent;
                if ("Student".equalsIgnoreCase(user.getRole())) {
                    intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, SupervisorDashboardActivity.class);
                }

                intent.putExtra("username", user.getUsername());
                intent.putExtra("role", user.getRole());
                intent.putExtra("email", user.getEmail());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        // ---- Go to Signup ----
        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });
    }
}
