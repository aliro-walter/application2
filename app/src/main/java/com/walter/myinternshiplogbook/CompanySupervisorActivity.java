package com.walter.myinternshiplogbook;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CompanySupervisorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_supervisor);

        TextView tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setText("Welcome, Company Supervisor!");
    }
}
