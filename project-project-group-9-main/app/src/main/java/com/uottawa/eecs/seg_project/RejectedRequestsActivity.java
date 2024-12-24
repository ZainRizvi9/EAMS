package com.uottawa.eecs.seg_project;

import android.app.AlertDialog;
import android.content.Intent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RejectedRequestsActivity extends AppCompatActivity {

    private ListView requestListView;
    private List<RegistrationRequest> rejectedRequestList;
    private ArrayAdapter<RegistrationRequest> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rejected_requests);

        requestListView = findViewById(R.id.requestListView1);
        rejectedRequestList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rejectedRequestList);
        requestListView.setAdapter(adapter);

        requestListView.setOnItemClickListener((parent, view, position, id) -> {
            RegistrationRequest request = rejectedRequestList.get(position);
            showApproveDialog(request);
        });

        loadRejectedRequests();

        Button backButton = findViewById(R.id.backButton6);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to go to view the rejected requests
                Intent intent = new Intent(RejectedRequestsActivity.this, AdminWelcomePageActivity.class);
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

    private void loadRejectedRequests() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rejections")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            String email = document.getString("email");
                            String role = document.getString("role");
                            String address = document.getString("address");
                            String organization = document.getString("organization");
                            String phone = document.getString("phone");
                            String password = document.getString("password");

                            RegistrationRequest rejectedRequest = new RegistrationRequest(firstName, lastName, email, role, address, organization, password, phone);
                            rejectedRequestList.add(rejectedRequest);
                        }
                        adapter.notifyDataSetChanged(); // Notify adapter to update ListView
                    } else {
                        Toast.makeText(RejectedRequestsActivity.this, "Failed to load rejected requests.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showApproveDialog(RegistrationRequest request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Approve Request from " + request.getFirstName() + " " + request.getLastName());

        if (request.getRole().equals("Organizer")) {
            builder.setMessage("Email: " + request.getEmail() +
                    "\nRole: " + request.getRole() +
                    "\nPhone: " + request.getPhone() +
                    "\nAddress: " + request.getAddress()+
                    "\nOrganization: "+ request.getOrganization());
        } else {
            builder.setMessage("Email: " + request.getEmail() +
                    "\nRole: " + request.getRole() +
                    "\nPhone: " + request.getPhone() +
                    "\nAddress: " + request.getAddress());
        }

        builder.setPositiveButton("Approve", (dialog, which) -> {
            approveRejectedRequest(request);  // Call to approve the rejected request
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void approveRejectedRequest(RegistrationRequest request) {
        String collectionName;
        Map<String, Object> userData = new HashMap<>();

        if (request.getRole().equals("Organizer")) {
            collectionName = "organizers"; // Move to organizers collection
            userData.put("organization", request.getOrganization());
        } else {
            collectionName = "attendees"; // Move to attendees collection
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        userData.put("address", request.getAddress());
        userData.put("email", request.getEmail());
        userData.put("firstName", request.getFirstName());
        userData.put("lastName", request.getLastName());
        userData.put("password", request.getPassword());
        userData.put("phone", request.getPhone());

        // Add the user data to the appropriate collection
        db.collection(collectionName)
                .document(request.getEmail())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Success: Remove from "rejections" after moving to the correct collection
                    db.collection("rejections")
                            .document(request.getEmail())
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(RejectedRequestsActivity.this, "Request approved and moved.", Toast.LENGTH_SHORT).show();
                                // Send an approved email to the user
                                sendEmailNotification(request.getEmail());
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RejectedRequestsActivity.this, "Error removing from rejections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RejectedRequestsActivity.this, "Error adding user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendEmailNotification(String userEmail) {
        String subject = "CrowdSync Registration";
        String message = "Congratulations! After further review, your registration has been approved. Welcome aboard!";

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "No email client installed on device.", Toast.LENGTH_SHORT).show();
        }
    }

}