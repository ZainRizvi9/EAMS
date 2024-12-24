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

public class OrganizerWelcomePageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_welcome_page);

        Button logoutButton = findViewById(R.id.logoutButton3);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to go back to MainActivity
                Intent intent = new Intent(OrganizerWelcomePageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button createEvent = findViewById(R.id.createEventButton);
        createEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                // Create an intent to go to the Create Event Activity
                Intent intent = new Intent(OrganizerWelcomePageActivity.this, CreateEventActivity.class);
                startActivity(intent);
            }
        });

        Button pastEvents = findViewById(R.id.pastEvents);
        pastEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizerWelcomePageActivity.this, PastEventsActivity.class);
                startActivity(intent);
            }
        });

        Button upcomingEvents = findViewById(R.id.upcomingEvents);
        upcomingEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                // Create an intent to go to the Create Event Activity
                Intent intent = new Intent(OrganizerWelcomePageActivity.this, UpcomingEventsActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
