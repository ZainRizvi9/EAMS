package com.uottawa.eecs.seg_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView eventDetailsTextView;
    private ImageView registrationStatusImageView;
    private Button cancelRegistrationButton;

    private FirebaseFirestore db;
    private String title;
    private String attendeeEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);

        db = FirebaseFirestore.getInstance();

        eventDetailsTextView = findViewById(R.id.eventDetailsTextView);
        registrationStatusImageView = findViewById(R.id.registrationStatusImageView);  // For ImageView status
        cancelRegistrationButton = findViewById(R.id.cancelRegistrationButton);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String address = intent.getStringExtra("address");
        long startTimeMillis = intent.getLongExtra("startTime", 0);
        long endTimeMillis = intent.getLongExtra("endTime", 0);
        String organizerEmail = intent.getStringExtra("organizerEmail");

        SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        attendeeEmail = sharedPreferences.getString("attendeeEmail", "");

        String eventDetails = "Title: " + title + "\n\n"
                + "Description: " + description + "\n\n"
                + "Address: " + address + "\n\n"
                + "Start Time: " + formatTime(startTimeMillis) + "\n\n"
                + "End Time: " + formatTime(endTimeMillis) + "\n\n"
                + "Organizer Email: " + organizerEmail;

        // Set the event details text to the TextView
        eventDetailsTextView.setText(eventDetails);

        checkRegistrationStatus();

        cancelRegistrationButton.setOnClickListener(v -> cancelRegistration());

        Button backButton = findViewById(R.id.backButton14);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private String formatTime(long timeMillis) {
        if (timeMillis == 0) return "N/A";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault());
        java.util.Date date = new java.util.Date(timeMillis);
        return sdf.format(date);
    }

    private void checkRegistrationStatus() {
        // Reference to the attendee's document within the event's 'attendees' subcollection
        DocumentReference attendeeRef = db.collection("events")
                .document(title)  // Event ID is the title
                .collection("attendees")
                .document(attendeeEmail);  // Attendee email

        // Get the attendee's document and check the registration status
        attendeeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String eventStatus = document.getString("Event Status");

                    // Show the corresponding status image
                    if ("approved".equals(eventStatus)) {
                        registrationStatusImageView.setImageResource(R.drawable.checkmark);  // Checkmark image
                    } else if ("denied".equals(eventStatus)) {
                        registrationStatusImageView.setImageResource(R.drawable.xmark);  // Xmark image
                    } else {
                        registrationStatusImageView.setImageResource(R.drawable.dash);  // Dash image for pending
                    }
                } else {
                    Toast.makeText(EventDetailsActivity.this, "Attendee not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EventDetailsActivity.this, "Failed to load registration status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelRegistration() {
        long currentTimeMillis = System.currentTimeMillis();
        long twentyFourHoursMillis = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

        // Check if the event starts within 24 hours
        Intent intent = getIntent();
        long startTimeMillis = intent.getLongExtra("startTime", 0);

        if (startTimeMillis - currentTimeMillis < twentyFourHoursMillis) {
            Toast.makeText(this, "You cannot cancel registration within 24 hours of the event start time.", Toast.LENGTH_LONG).show();
            return;
        }

        DocumentReference attendeeRef = db.collection("events")
                .document(title)
                .collection("attendees")
                .document(attendeeEmail);

        attendeeRef.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Registration canceled successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to cancel registration", Toast.LENGTH_SHORT).show();
            }
        });
    }

}