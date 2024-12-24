package com.uottawa.eecs.seg_project;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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

public class AdminInboxActivity extends AppCompatActivity {

    private ListView requestListView;
    private List<RegistrationRequest> requestList;
    private ArrayAdapter<RegistrationRequest> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_inbox);

        requestListView = findViewById(R.id.requestListView);
        requestList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, requestList);
        requestListView.setAdapter(adapter);

        loadRequests();

        // Handle clicks on ListView items
        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RegistrationRequest selectedRequest = requestList.get(position);
                showRequestDialog(selectedRequest);
            }
        });

        Button backButton = findViewById(R.id.backButton7);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to go back to the Admin Welcome Page
                Intent intent = new Intent(AdminInboxActivity.this, AdminWelcomePageActivity.class);
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

    private void loadRequests() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("registration_requests")
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
                            String password = document.getString("password");
                            String phone = document.getString("phone");
                            RegistrationRequest request = new RegistrationRequest(firstName, lastName, email, role, address, organization, password, phone);
                            requestList.add(request);
                        }
                        adapter.notifyDataSetChanged(); // Notify adapter of data changes
                    }
                });
    }

    // For the Admin Inbox to show a pop-up dialog box
    private void showRequestDialog(RegistrationRequest request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request from " + request.getFirstName() + request.getLastName());
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

        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                acceptRequest(request);
            }
        });

        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                denyRequest(request);
            }
        });

        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    // Accepting the user's request
    private void acceptRequest(RegistrationRequest request) {
        String collectionName;
        Map<String, Object> userData = new HashMap<>();

        if (request.getRole().equals("Organizer")) {
            collectionName = "organizers"; // Store in organizers collection
            userData.put("organization", request.getOrganization());
        } else {
            collectionName = "attendees"; // Store in attendees collection
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
                    Toast.makeText(AdminInboxActivity.this, "User added to " + collectionName + ".", Toast.LENGTH_SHORT).show();
                    // Send a success email to the user
                    sendEmailNotification(request.getEmail(), "approved");
                    // After successful addition, remove from "registration_requests" collection on Firebase
                    removeFromPending(request);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminInboxActivity.this, "Error adding user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Denying the user's request
    private void denyRequest(RegistrationRequest request) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> rejectionData = new HashMap<>();

        if (request.getRole().equals("Organizer")) {
            rejectionData.put("organization", request.getOrganization());
        }

        rejectionData.put("email", request.getEmail());
        rejectionData.put("firstName", request.getFirstName());
        rejectionData.put("lastName", request.getLastName());
        rejectionData.put("password", request.getPassword());
        rejectionData.put("role", request.getRole());
        rejectionData.put("address", request.getAddress());
        rejectionData.put("phone", request.getPhone());

        db.collection("rejections")
                .document(request.getEmail())  // Use the email as the document ID
                .set(rejectionData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminInboxActivity.this, "Request denied and logged in rejections.", Toast.LENGTH_SHORT).show();
                    // Send a rejected email to the user
                    sendEmailNotification(request.getEmail(), "rejected");
                    // After successfully logging the rejection, remove from "registration_requests"
                    removeFromPending(request);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminInboxActivity.this, "Error logging rejection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Removing the request from the registration_requests collection on Firestore once accepted or rejected
    private void removeFromPending(RegistrationRequest request) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("registration_requests")
                .document(request.getEmail())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminInboxActivity.this, "Request removed from inbox.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminInboxActivity.this, "Error removing request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendEmailNotification(String userEmail, String status) {
        String subject = "Registration " + status;
        String message;

        if (status.equals("approved")){
            message = "Congratulations! Your registration has been approved. Welcome aboard!";
        } else {
            message = "We regret to inform you that your registration was not approved. Please contact support if you have any questions.";

        }

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
