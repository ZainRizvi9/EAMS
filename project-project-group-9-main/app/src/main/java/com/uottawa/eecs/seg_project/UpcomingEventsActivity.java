package com.uottawa.eecs.seg_project;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UpcomingEventsActivity extends AppCompatActivity {
    private ListView eventsListView;
    private FirebaseFirestore db;
    private List<String> eventList;
    private ArrayAdapter<String> adapter;

    private String organizerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upcoming_events);

        SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        organizerEmail = sharedPreferences.getString("organizerEmail", ""); // Retrieve the organizer's email


        eventsListView = findViewById(R.id.eventsListView);
        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventList);
        eventsListView.setAdapter(adapter);

        loadUpcomingEvents();

        Button backButton = findViewById(R.id.backButton8);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity and return to the previous page
                finish();
            }
        });

        // Creates an on-click listener to open the dialog box
        eventsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedEvent = eventList.get(position);
            showDeleteDialog(selectedEvent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUpcomingEvents() {
        Date currentDate = Calendar.getInstance().getTime();

        db.collection("events")
                .whereGreaterThan("date", currentDate)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.forEach(documentSnapshot -> {
                            String title = documentSnapshot.getString("title");
                            Date eventDate = documentSnapshot.getDate("date");
                            String formattedEvent = title + " - " + eventDate.toString();
                            eventList.add(formattedEvent);
                        });
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No upcoming events found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show());
    }

    private void showDeleteDialog(String eventSummary) {
        String eventTitle = eventSummary.split(" - ")[0];  // Extract the title from summary

        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete the event: " + eventTitle + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(eventTitle))
                .setNegativeButton("Cancel", null)
                .setNeutralButton("View Attendees", (dialog, which) -> viewAttendees(eventTitle))
                .show();
    }

    private void deleteEvent(String eventTitle) {
        // Retrieve the event document and check if the organizer's email matches
        db.collection("events").document(eventTitle)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventOrganizerEmail = documentSnapshot.getString("organizerEmail");

                        if (organizerEmail.equals(eventOrganizerEmail)) {
                            // Check if there are any approved attendees
                            db.collection("events").document(eventTitle).collection("attendees")
                                    .whereEqualTo("Event Status", "approved")
                                    .get()
                                    .addOnSuccessListener(attendeeSnapshots -> {
                                        if (!attendeeSnapshots.isEmpty()) {
                                            // Approved attendees exist
                                            Toast.makeText(this, "Event cannot be deleted as it has approved attendees.", Toast.LENGTH_LONG).show();
                                        } else {
                                            // No approved attendees continue with deletion
                                            db.collection("events").document(eventTitle)
                                                    .delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(this, "Event deleted successfully.", Toast.LENGTH_SHORT).show();
                                                        loadUpcomingEvents();  // Refresh the list
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete event.", Toast.LENGTH_SHORT).show());
                                        }
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Error checking attendees.", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "You are not authorized to delete this event.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Event does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error retrieving event.", Toast.LENGTH_SHORT).show());
    }

    private void viewAttendees(String eventTitle) {
        db.collection("events").document(eventTitle)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventOrganizerEmail = documentSnapshot.getString("organizerEmail");

                        if (organizerEmail.equals(eventOrganizerEmail)) {
                            // Organizer is authorized, start ViewEventAttendeesActivity
                            Intent intent = new Intent(UpcomingEventsActivity.this, ViewEventAttendeesActivity.class);
                            intent.putExtra("eventId", eventTitle);  // Pass the event title or ID
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "You are not authorized to view attendees for this event.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Event does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error retrieving event.", Toast.LENGTH_SHORT).show());
    }

}