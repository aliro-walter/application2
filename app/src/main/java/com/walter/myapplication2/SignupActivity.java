package com.walter.myapplication2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    EditText etUsername, etEmail, etPassword;
    Spinner spinnerRole;
    Button btnSignup;
    TextView tvLogin;
    UserDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);
        dbHelper = new UserDBHelper(this);

        btnSignup.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email based on selected role
            if (!isEmailValidForRole(email, role)) {
                Toast.makeText(this, "Invalid email for selected role!", Toast.LENGTH_LONG).show();
                return;
            }

            // Insert user
            boolean inserted = dbHelper.insertUser(username, email, password, role);
            if (inserted) {
                Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Signup failed! Email may already exist.", Toast.LENGTH_SHORT).show();
            }
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Validates email based on role.
     */
    private boolean isEmailValidForRole(String email, String role) {
        email = email.toLowerCase().trim();

        if (role.equalsIgnoreCase("Student")) {
            // Must end with @std.must.ac.ug
            return email.endsWith("@std.must.ac.ug");
        }
        else if (role.equalsIgnoreCase("University Supervisor")) {
            // Must end with @must.ac.ug
            return email.endsWith("@must.ac.ug");
        }
        else if (role.equalsIgnoreCase("Company Supervisor")) {
            // Any valid email address allowed
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }

        return false;
    }
}
