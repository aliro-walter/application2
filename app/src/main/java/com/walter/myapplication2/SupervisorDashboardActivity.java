package com.walter.myapplication2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class SupervisorDashboardActivity extends AppCompatActivity {

    TextView tvWelcome;
    ListView lvStudents;
    Button btnLogout;
    ArrayList<String> studentList;
    ArrayAdapter<String> adapter;

    private LogDBHelper logDBHelper; // DB helper to read student names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_dashboard);

        tvWelcome = findViewById(R.id.tvWelcome);
        lvStudents = findViewById(R.id.lvStudents);
        btnLogout = findViewById(R.id.btnLogout);

        // Get role and username from Login
        String username = getIntent().getStringExtra("username");
        String role = getIntent().getStringExtra("role");

        if (username == null) username = "Supervisor";
        if (role == null) role = "Supervisor";

        tvWelcome.setText("Welcome " + role + ": " + username);

        // Initialize DB helper and load distinct student names from logs
        logDBHelper = new LogDBHelper(this);
        studentList = new ArrayList<>();
        for (String name : logDBHelper.getDistinctStudentNamesFromLogs()) {
            if (name != null && !name.trim().isEmpty()) {
                studentList.add(name.trim());
            }
        }

        // If no students have submitted logs yet, show a helpful message
        if (studentList.isEmpty()) {
            studentList.add("No students have submitted daily logs yet.");
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        lvStudents.setAdapter(adapter);

        lvStudents.setOnItemClickListener((parent, view, position, id) -> {
            String student = studentList.get(position);
            if (student == null || student.startsWith("No students")) return;

            // Open the UniversitySupervisorActivity and ask it to show logs for this student
            Intent intent = new Intent(SupervisorDashboardActivity.this, UniversitySupervisorActivity.class);
            intent.putExtra("selectedStudent", student);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
