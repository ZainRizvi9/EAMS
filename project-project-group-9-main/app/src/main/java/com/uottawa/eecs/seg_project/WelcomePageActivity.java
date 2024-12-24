package com.uottawa.eecs.seg_project;

import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;
import android.view.View;
import android.widget.Button;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WelcomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_page);

        // Retrieve the user role passed from the previous activity
        String userRole = getIntent().getStringExtra("userRole");

        // Find the TextView where the role will be displayed
        TextView roleTextView = findViewById(R.id.roleTextView);

        // Display the user's role
        if (userRole != null) {
            roleTextView.setText("Welcome, " + userRole + "!");
        } else {
            roleTextView.setText("Welcome!");
        }

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to go back to MainActivity
                Intent intent = new Intent(WelcomePageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button searchButton = findViewById(R.id.searchEventsButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to go back to MainActivity
                Intent intent = new Intent(WelcomePageActivity.this, SearchEventsActivity.class);
                startActivity(intent);
            }
        });

        Button myEventsButton = findViewById(R.id.myEventsButton);
        myEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to go back to MainActivity
                Intent intent = new Intent(WelcomePageActivity.this, MyEventsActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_welcome_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}