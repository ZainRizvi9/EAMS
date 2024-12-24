package com.uottawa.eecs.seg_project;

import android.app.AlertDialog;
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

public class ViewEventAttendeesActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ListView attendeesListView;
    private ArrayAdapter<String> adapter;
    private List<String> attendeesList;
    private List<QueryDocumentSnapshot> attendeesInformation;
    private String eventId;
    private boolean automaticApproval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_event_attendees);

        db = FirebaseFirestore.getInstance();

        attendeesListView = findViewById(R.id.attendeesListView);
        attendeesList = new ArrayList<>();
        attendeesInformation = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendeesList);
        attendeesListView.setAdapter(adapter);

        // Retrieve eventId from the intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId != null) {
            automaticApprovalStatus();
            loadAttendees();
        } else {
            Toast.makeText(this, "Event ID not found.", Toast.LENGTH_SHORT).show();
        }

        Button backButton = findViewById(R.id.backButton11);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity and return to the previous page
                finish();
            }
        });

        attendeesListView.setOnItemClickListener((parent, view, position, id) -> {
            if (!automaticApproval) {
                showAttendeeApprovalDialog(position);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadAttendees() {
        // Fetch attendees from the "attendees" collection inside of the event specified by organizer
        db.collection("events").document(eventId).collection("attendees")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    attendeesList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");

                        String attendeeFullName = firstName + " " + lastName;
                        attendeesList.add(attendeeFullName);

                        attendeesInformation.add(document);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading attendees", Toast.LENGTH_SHORT).show());
    }

    private void showAttendeeApprovalDialog(int position) {
        // Show dialog to approve or deny attendee
        QueryDocumentSnapshot document = attendeesInformation.get(position);

        String firstName = document.getString("firstName");
        String lastName = document.getString("lastName");
        String email = document.getString("email");
        String phone = document.getString("phone");
        String address = document.getString("address");
        String status = document.getString("Event Status");

        String attendeeInfo = "Name: " + firstName + " " + lastName + "\n" +
                "Email: " + email + "\n" +
                "Phone: " + phone + "\n" +
                "Address: " + address + "\n" +
                "Status: " + status;

        new AlertDialog.Builder(this)
                .setTitle("Manage Attendee")
                .setMessage("Would you like to approve or deny? " + "\n" + attendeeInfo)
                .setPositiveButton("Approve", (dialog, which) -> updateAttendeeStatus(position, "approved"))
                .setNegativeButton("Deny", (dialog, which) -> updateAttendeeStatus(position, "denied"))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void automaticApprovalStatus() {
        // Fetch automaticApproval field for the event
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        automaticApproval = documentSnapshot.getBoolean("automaticApproval");
                        if (automaticApproval) {
                            approveAllAttendees();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching approval status", Toast.LENGTH_SHORT).show());
    }

    private void approveAllAttendees() {
        // Set all attendees' "Event Status" to "approved" if automaticApproval is true
        db.collection("events").document(eventId).collection("attendees")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("Event Status", "approved")
                                .addOnFailureListener(e -> Toast.makeText(this, "Error approving attendee", Toast.LENGTH_SHORT).show());
                    }
                    Toast.makeText(this, "All attendees approved automatically", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateAttendeeStatus(int position, String status) {
        // getting the attendees name from previously stored variable
        String attendeeName = attendeesList.get(position);

        db.collection("events").document(eventId).collection("attendees")
                .whereEqualTo("firstName", attendeeName.split(" ")[0]) // split the full name to get first name
                .whereEqualTo("lastName", attendeeName.split(" ")[1])  // split the full name to get last name
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("Event Status", status)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ViewEventAttendeesActivity.this, "Attendee " + status, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ViewEventAttendeesActivity.this, "Error updating status", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewEventAttendeesActivity.this, "Error fetching attendee", Toast.LENGTH_SHORT).show();
                });
    }
}