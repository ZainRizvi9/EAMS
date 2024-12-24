package com.uottawa.eecs.seg_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminWelcomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_welcome_page);

        Button logoutButton = findViewById(R.id.logoutButton2);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to go back to MainActivity
                Intent intent = new Intent(AdminWelcomePageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button rejectedRequests = findViewById(R.id.rejected_requests);
        rejectedRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to go to view the rejected requests
                Intent intent = new Intent(AdminWelcomePageActivity.this, RejectedRequestsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button inboxButton = findViewById(R.id.inboxButton);
        inboxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to go to the Inbox
                Intent intent = new Intent(AdminWelcomePageActivity.this, AdminInboxActivity.class);
                startActivity(intent);
                finish();
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}