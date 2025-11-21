
package com.walter.myinternshiplogbook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class UniversitySupervisorActivity extends AppCompatActivity {

    private ListView studentListView, logsListView;
    private EditText feedbackInput;
    private Button saveFeedbackBtn;
    private TextView noStudentsMessage;

    private LogDBHelper logDBHelper;
    private ArrayList<String> studentNames;
    private ArrayList<Log> logs;
    private ArrayAdapter<String> studentAdapter;
    private LogAdapter logsAdapter;

    private String selectedStudent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_university_supervisor);

        // Initialize views
        studentListView = findViewById(R.id.studentListView);
        logsListView = findViewById(R.id.logsListView);
        feedbackInput = findViewById(R.id.feedbackInput);
        saveFeedbackBtn = findViewById(R.id.saveFeedbackBtn);
        noStudentsMessage = findViewById(R.id.noStudentsMessage); // Add this TextView in XML

        logDBHelper = new LogDBHelper(this);

        // ✅ Get distinct student names from logs only
        studentNames = new ArrayList<>();
        for (String name : logDBHelper.getDistinctStudentNamesFromLogs()) {
            if (name != null && !name.trim().isEmpty()) {
                studentNames.add(name.trim());
            }
        }

        // ✅ Hide list and show message if no logs yet
        if (studentNames.isEmpty()) {
            noStudentsMessage.setVisibility(View.VISIBLE);
            studentListView.setVisibility(View.GONE);
        } else {
            noStudentsMessage.setVisibility(View.GONE);
            studentListView.setVisibility(View.VISIBLE);
        }

        // ✅ Populate ListView with student names
        studentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentNames);
        studentListView.setAdapter(studentAdapter);

        // If an incoming intent specified a student, preselect and load their logs
        String incomingSelected = getIntent().getStringExtra("selectedStudent");
        if (incomingSelected != null && !incomingSelected.trim().isEmpty()) {
            selectedStudent = incomingSelected.trim();
            int idx = studentNames.indexOf(selectedStudent);
            if (idx >= 0) {
                studentListView.setSelection(idx);
                loadStudentLogs(selectedStudent);
            } else {
                // If not found, still try to load logs; getLogsByStudent will return empty if none
                loadStudentLogs(selectedStudent);
            }
        }

        // ✅ When a student is tapped, load their logs
        studentListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedStudent = studentNames.get(position);
            loadStudentLogs(selectedStudent);
        });

        // ✅ Save feedback
        saveFeedbackBtn.setOnClickListener(v -> {
            if (selectedStudent == null) {
                Toast.makeText(this, "Select a student first", Toast.LENGTH_SHORT).show();
                return;
            }

            String feedback = feedbackInput.getText().toString().trim();
            if (feedback.isEmpty()) {
                Toast.makeText(this, "Please enter feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save feedback to the latest log entry for the selected student
            boolean success = logDBHelper.saveFeedbackForLatestLog(selectedStudent, feedback);

            if (success) {
                Toast.makeText(this, "Feedback saved successfully!", Toast.LENGTH_SHORT).show();
                feedbackInput.setText("");
                loadStudentLogs(selectedStudent);
            } else {
                Toast.makeText(this, "Failed to save feedback", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button: return to Supervisor Dashboard
        Button btnBackSupervisor = findViewById(R.id.btnBackSupervisor);
        if (btnBackSupervisor != null) {
            btnBackSupervisor.setOnClickListener(v -> {
                startActivity(new Intent(UniversitySupervisorActivity.this, SupervisorDashboardActivity.class));
                finish();
            });
        }

    }

    // ✅ Load logs for selected student
    private void loadStudentLogs(String studentName) {
        logs = logDBHelper.getLogsWithImagesByStudent(studentName);
        if (logs.isEmpty()) {
            // Create a dummy log to show a message if no logs are found
            logs.add(new Log("No logs found for " + studentName, null));
        }

        logsAdapter = new LogAdapter(this, logs);
        logsListView.setAdapter(logsAdapter);
    }
}
