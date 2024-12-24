package com.uottawa.eecs.seg_project;

import java.util.Map;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class AttendeeRegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendee_registration);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Find the back button by its ID
        Button backButton = findViewById(R.id.backButton4);

        // Set an onClickListener on the back button to finish the activity
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // This will close the current activity and return to the previous one
            }
        });

        EditText firstNameField = findViewById(R.id.editTextTextPassword6);
        EditText lastNameField = findViewById(R.id.editTextTextPassword7);
        EditText emailField = findViewById(R.id.editTextTextEmailAddress2);
        EditText passwordField = findViewById(R.id.editTextTextPassword8);
        EditText phoneField = findViewById(R.id.editTextPhone2);
        EditText addressField = findViewById(R.id.editTextTextPostalAddress2);

        // Register Button
        Button registerButton = findViewById(R.id.registerButton3);
        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Get user inputs
                String firstName = firstNameField.getText().toString().trim();
                String lastName = lastNameField.getText().toString().trim();
                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();
                String phone = phoneField.getText().toString().trim();
                String address = addressField.getText().toString().trim();

                // Validate inputs
                if (validateInputs(firstName, lastName, email, password, phone, address)) {
                    // Create registration request data
                    Map<String, Object> registrationRequest = new HashMap<>();
                    registrationRequest.put("firstName", firstName);
                    registrationRequest.put("lastName", lastName);
                    registrationRequest.put("email", email);
                    registrationRequest.put("password", password);  // Password should be hashed in a real-world app
                    registrationRequest.put("phone", phone);
                    registrationRequest.put("address", address);
                    registrationRequest.put("role", "Attendee");
                    registrationRequest.put("status", "pending");

                    // Save request to Firestore (using "registration_requests" collection)
                    db.collection("registration_requests").document(email)
                            .set(registrationRequest)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AttendeeRegistrationActivity.this, "Registration request sent. Waiting for Admin approval.", Toast.LENGTH_SHORT).show();

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AttendeeRegistrationActivity.this, "Error sending registration request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_attendee_registration), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean validateInputs(String firstName, String lastName, String email, String password, String phone, String address) {
        // First name should only contain letters
        if (firstName == null || firstName.isEmpty() || !firstName.matches("[a-zA-Z]+")) {
            Toast.makeText(this, "Please enter a valid first name (letters only).", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Last name should only contain letters
        if (lastName == null || lastName.isEmpty() || !lastName.matches("[a-zA-Z]+")) {
            Toast.makeText(this, "Please enter a valid last name (letters only).", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate email address format
        if (email == null || email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate phone number (should be exactly 10 digits)
        if (phone == null || phone.isEmpty() || !phone.matches("\\d{10}")) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Password validation
        if (password == null || password.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate Address
        if (address == null || address.isEmpty()) {
            Toast.makeText(this, "Please enter a valid address.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}