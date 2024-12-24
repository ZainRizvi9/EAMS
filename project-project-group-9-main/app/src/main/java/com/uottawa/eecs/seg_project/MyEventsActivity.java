package com.uottawa.eecs.seg_project;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MyEventsActivity extends AppCompatActivity {

    private String attendeeEmail;
    private ListView eventsListView;
    private List<String> myEventsList = new ArrayList<>();
    private ArrayAdapter<String> eventsAdapter;
    private FirebaseFirestore db;
    private List<QueryDocumentSnapshot> eventDocuments = new ArrayList<>();

    private String title;
    private String description;
    private String address;
    private long startTimeMillis;
    private long endTimeMillis;
    private String organizerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_events);

        SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        attendeeEmail = sharedPreferences.getString("attendeeEmail", ""); // Retrieve the attendee's email

        db = FirebaseFirestore.getInstance();

        eventsListView = findViewById(R.id.listView);
        eventsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myEventsList);
        eventsListView.setAdapter(eventsAdapter);

        loadMyEvents();

        eventsListView.setOnItemClickListener((parent, view, position, id) -> showEventDetails(position));

        Button backButton = findViewById(R.id.backButton12);
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

    private void loadMyEvents() {
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                myEventsList.clear(); // Clear previous data
                eventDocuments.clear();

                int totalEvents = task.getResult().size();
                AtomicInteger completedEvents = new AtomicInteger(0); // Atomic counter for completed events

                for (QueryDocumentSnapshot eventDoc : task.getResult()) {
                    String eventId = eventDoc.getId();
                    String eventTitle = eventDoc.getString("title");

                    // Check if this event contains the attendee
                    db.collection("events")
                            .document(eventId)
                            .collection("attendees")
                            .whereEqualTo("email", attendeeEmail)
                            .get()
                            .addOnCompleteListener(attendeeTask -> {
                                if (attendeeTask.isSuccessful() && !attendeeTask.getResult().isEmpty()) {
                                    // Add the event title to the list if the attendee is found inside of the event's attendee collection
                                    myEventsList.add(eventTitle);
                                    eventDocuments.add(eventDoc);
                                }

                                // Increment the counter after each attendee check
                                if (completedEvents.incrementAndGet() == totalEvents) {
                                    // Once all events are processed, notify the adapter
                                    eventsAdapter.notifyDataSetChanged();
                                }
                            });
                }
            } else {
                Toast.makeText(this, "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showEventDetails(int position) {
        if (position < 0 || position >= eventDocuments.size()) {
            // Log an error or show a message if the position is invalid
            Toast.makeText(this, "Invalid event position", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the event document for the clicked event
        QueryDocumentSnapshot eventDoc = eventDocuments.get(position);

        // Check if eventDoc is not null
        if (eventDoc != null) {
            // Safely retrieve the event details
            title = eventDoc.getString("title");
            description = eventDoc.getString("description");
            address = eventDoc.getString("address");

            // Handle potential null values in timestamps
            if (eventDoc.getTimestamp("startTime") != null) {
                startTimeMillis = eventDoc.getTimestamp("startTime").toDate().getTime();
            } else {
                startTimeMillis = 0; // Set a default value or handle error
            }

            if (eventDoc.getTimestamp("endTime") != null) {
                endTimeMillis = eventDoc.getTimestamp("endTime").toDate().getTime();
            } else {
                endTimeMillis = 0; // Set a default value or handle error
            }

            organizerEmail = eventDoc.getString("organizerEmail");

            // Check if essential details are missing
            if (title == null || description == null || address == null || organizerEmail == null) {
                Toast.makeText(this, "Missing event details", Toast.LENGTH_SHORT).show();
                return;
            }

            // Launch EventDetailsActivity to display the event details
            Intent intent = new Intent(this, EventDetailsActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("description", description);
            intent.putExtra("address", address);
            intent.putExtra("startTime", startTimeMillis);
            intent.putExtra("endTime", endTimeMillis);
            intent.putExtra("organizerEmail", organizerEmail);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
        }
    }

}