package com.uottawa.eecs.seg_project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchEventsActivity extends AppCompatActivity {

    private List<String> eventsList = new ArrayList<>();
    private ArrayAdapter<String> eventsAdapter;
    private ListView listView;
    private EditText eventSearchField;
    private String attendeeEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_events);

        eventSearchField = findViewById(R.id.event_name);
        Button searchButton = findViewById(R.id.search_button);
        listView = findViewById(R.id.listView);

        eventsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventsList);
        listView.setAdapter(eventsAdapter);

        loadAllEvents();

        searchButton.setOnClickListener(v -> performSearch());

        Button backButton = findViewById(R.id.backButton13);
        backButton.setOnClickListener(v -> finish());

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String selectedEvent = eventsList.get(position);
            fetchEventDetails(selectedEvent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadAllEvents() {
        SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        attendeeEmail = sharedPreferences.getString("attendeeEmail", "");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot eventQuerySnapshot = task.getResult();
                        if (eventQuerySnapshot != null) {
                            eventsList.clear(); // Clear previous event data
                            List<DocumentSnapshot> allEvents = eventQuerySnapshot.getDocuments();

                            // Current system time for filtering
                            Timestamp currentTime = Timestamp.now();

                            // Loop through the events and add only upcoming events
                            for (DocumentSnapshot eventDocument : allEvents) {
                                Timestamp eventDate = eventDocument.getTimestamp("startTime");

                                if (eventDate != null && eventDate.compareTo(currentTime) > 0) { // Only upcoming events
                                    String eventTitle = eventDocument.getId(); // Use document ID as the event title

                                    // Check if the user is in the attendees list of the event
                                    db.collection("events")
                                            .document(eventTitle)
                                            .collection("attendees")
                                            .document(attendeeEmail)
                                            .get()
                                            .addOnCompleteListener(attendeeTask -> {
                                                if (attendeeTask.isSuccessful() && !attendeeTask.getResult().exists()) {
                                                    // User is not registered for this event, add it to the list
                                                    eventsList.add(eventTitle);
                                                    eventsAdapter.notifyDataSetChanged();
                                                }
                                            });
                                }
                            }
                        }
                    } else {
                        Toast.makeText(SearchEventsActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void performSearch() {
        String searchQuery = eventSearchField.getText().toString().trim();

        if (!searchQuery.isEmpty()) {
            // Filter events based on the user's inputted keyword
            List<String> filteredEvents = new ArrayList<>();

            for (String event : eventsList) {
                if (event.toLowerCase().contains(searchQuery.toLowerCase())) {
                    filteredEvents.add(event);
                }
            }

            if (filteredEvents.isEmpty()) {
                Toast.makeText(SearchEventsActivity.this, "No events found", Toast.LENGTH_SHORT).show();
                eventsAdapter.clear();
                eventsAdapter.notifyDataSetChanged();
            } else {
                eventsAdapter.clear();
                eventsAdapter.addAll(filteredEvents);
                eventsAdapter.notifyDataSetChanged();
            }

        } else {
            eventsAdapter.clear();
            eventsAdapter.addAll(eventsList);
            eventsAdapter.notifyDataSetChanged();
        }
    }

    private void fetchEventDetails(String eventName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .whereEqualTo("title", eventName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                            String description = document.getString("description");
                            String address = document.getString("address");

                            // Handle Firestore Timestamp fields
                            Timestamp dateTimestamp = document.getTimestamp("date");
                            Timestamp startTimeTimestamp = document.getTimestamp("startTime");
                            Timestamp endTimeTimestamp = document.getTimestamp("endTime");

                            // Convert Timestamps to Strings
                            String startTime = (startTimeTimestamp != null) ? startTimeTimestamp.toDate().toString() : "N/A";
                            String endTime = (endTimeTimestamp != null) ? endTimeTimestamp.toDate().toString() : "N/A";

                            // Show dialog with fetched details
                            showEventDetailsDialog(eventName, description, startTime, endTime, address);
                        } else {
                            Toast.makeText(SearchEventsActivity.this, "Event details not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SearchEventsActivity.this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEventDetailsDialog(String title, String description, String startTime, String endTime, String address) {
        // Handle null or missing values
        description = (description != null) ? description : "No description available";
        address = (address != null) ? address : "No address provided";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(
                "Description: " + description + "\n\n" +
                        "Start Time: " + startTime + "\n" +
                        "End Time: " + endTime + "\n" +
                        "Address: " + address
        );
        builder.setPositiveButton("Register", (DialogInterface dialog, int which) -> {
            registerForEvent(title);
        });
        builder.setNegativeButton("Close", (DialogInterface dialog, int which) -> dialog.dismiss());
        builder.show();
    }

    private void registerForEvent(String eventTitle) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);

        // Get attendee email from SharedPreferences
        String attendeeEmail = sharedPreferences.getString("attendeeEmail", "");

        if (attendeeEmail.isEmpty()) {
            Toast.makeText(this, "Unable to retrieve attendee email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch attendee information from Firestore
        db.collection("attendees")
                .document(attendeeEmail)
                .get()
                .addOnSuccessListener(attendeeDocument -> {
                    if (attendeeDocument.exists()) {
                        // Retrieve attendee details
                        String attendeeFirstName = attendeeDocument.getString("firstName");
                        String attendeeLastName = attendeeDocument.getString("lastName");
                        String attendeeAddress = attendeeDocument.getString("address");
                        String attendeePassword = attendeeDocument.getString("password");
                        String attendeePhone = attendeeDocument.getString("phone");

                        // Fetch the event's automaticApproval field
                        db.collection("events")
                                .document(eventTitle)
                                .get()
                                .addOnSuccessListener(eventDocument -> {
                                    if (eventDocument.exists()) {
                                        boolean automaticApproval = eventDocument.getBoolean("automaticApproval") != null && eventDocument.getBoolean("automaticApproval");

                                        // Prepare attendee data for registration
                                        HashMap<String, Object> attendeeData = new HashMap<>();
                                        attendeeData.put("email", attendeeEmail);
                                        attendeeData.put("firstName", attendeeFirstName);
                                        attendeeData.put("lastName", attendeeLastName);
                                        attendeeData.put("address", attendeeAddress);
                                        attendeeData.put("password", attendeePassword); // Consider hashing
                                        attendeeData.put("phone", attendeePhone);
                                        attendeeData.put("Event Status", automaticApproval ? "approved" : "pending");

                                        // Add attendee to the attendees collection under the event
                                        db.collection("events")
                                                .document(eventTitle)
                                                .collection("attendees")
                                                .document(attendeeEmail)
                                                .set(attendeeData)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Remove the event from the local list and refresh the adapter
                                                    eventsList.remove(eventTitle);
                                                    eventsAdapter.notifyDataSetChanged();
                                                    Toast.makeText(SearchEventsActivity.this, "Successfully registered for " + eventTitle, Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(SearchEventsActivity.this, "Failed to register for event", Toast.LENGTH_SHORT).show());
                                    } else {
                                        Toast.makeText(SearchEventsActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(SearchEventsActivity.this, "Failed to fetch event details", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(SearchEventsActivity.this, "Attendee information not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(SearchEventsActivity.this, "Failed to fetch attendee information", Toast.LENGTH_SHORT).show());
    }
}
