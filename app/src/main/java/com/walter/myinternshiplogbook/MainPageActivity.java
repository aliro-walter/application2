package com.walter.myinternshiplogbook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // ✅ Use the same SharedPreferences as Signup/Login
        String currentUser = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("username", "Guest");

        // ✅ Display welcome message
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        if (tvWelcome != null) {
            tvWelcome.setText("Welcome, " + currentUser);
        }

        // ✅ Buttons
        Button btnStudent = findViewById(R.id.btnStudentDashboard);
        Button btnSupervisor = findViewById(R.id.btnSupervisorDashboard);
        Button btnLogout = findViewById(R.id.btnLogout);

        // ✅ Student Dashboard
        if (btnStudent != null) {
            btnStudent.setOnClickListener(v -> {
                Intent i = new Intent(this, StudentDashboardActivity.class);
                i.putExtra("username", currentUser);
                i.putExtra("role", "Student");
                startActivity(i);
            });
        }

        // ✅ Supervisor Dashboard
        if (btnSupervisor != null) {
            btnSupervisor.setOnClickListener(v -> {
                Intent i = new Intent(this, SupervisorDashboardActivity.class);
                i.putExtra("username", currentUser);
                i.putExtra("role", "Supervisor");
                startActivity(i);
            });
        }

        // ✅ Logout
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply();

                Intent intent = new Intent(MainPageActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
