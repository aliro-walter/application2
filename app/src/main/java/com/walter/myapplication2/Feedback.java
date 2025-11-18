package com.walter.myapplication2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class Feedback extends AppCompatActivity {

    private TextView tvFeedbackList;
    private TextView tvMessage;
    private Button btnBack;
    private LogDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);

        // Adjust layout for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize
        tvFeedbackList = findViewById(R.id.tvFeedbackList); // Make sure this TextView exists in your layout
        tvMessage = findViewById(R.id.tvMessage);
        btnBack = findViewById(R.id.btnBack);
        dbHelper = new LogDBHelper(this);

        // ✅ Get student name (you can modify this depending on your flow)
        String studentName = getIntent().getStringExtra("student_name");
        if (studentName == null) {
            studentName = "Unknown Student"; // fallback
        }

        // ✅ Fetch feedbacks from the database
        ArrayList<String> feedbacks = dbHelper.getFeedbacksForStudent(studentName);

        // ✅ Display feedbacks in the TextView
        if (feedbacks.isEmpty()) {
            tvFeedbackList.setText("No feedback available yet for " + studentName + ".");
            tvMessage.setVisibility(android.view.View.VISIBLE);
        } else {
            StringBuilder feedbackText = new StringBuilder();
            for (String fb : feedbacks) {
                feedbackText.append(fb).append("\n\n");
            }
            tvFeedbackList.setText(feedbackText.toString());
            tvMessage.setVisibility(android.view.View.GONE);
        }

        btnBack.setOnClickListener(v -> finish());
    }
}
