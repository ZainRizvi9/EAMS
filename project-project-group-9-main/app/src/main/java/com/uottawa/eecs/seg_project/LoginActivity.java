package com.uottawa.eecs.seg_project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            attemptLogin(username, password); // Pass the inputs to attempt login
        });

        // Back button functionality
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity and return to the home screen (MainActivity)
                finish();
            }
        });

        // Setting padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void attemptLogin(String username, String password) {
        // Validate that username and password are not empty
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("attendees")
                .whereEqualTo("email", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Error retrieving data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // Handle attendee login
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String storedPassword = document.getString("password");
                            if (storedPassword != null && storedPassword.equals(password)) {
                                // Store the attendee's email (username) for later use
                                SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("attendeeEmail", username);  // Save the email
                                editor.apply();

                                // Login successful as Attendee
                                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, WelcomePageActivity.class);
                                intent.putExtra("userRole", "Attendee"); // Pass the role to the WelcomePageActivity
                                startActivity(intent);
                                finish();
                                return;
                            }
                        }
                        Toast.makeText(this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // If not found in attendees, check organizers
                    db.collection("organizers")
                            .whereEqualTo("email", username)
                            .get()
                            .addOnCompleteListener(task1 -> {
                                if (!task1.isSuccessful()) {
                                    Toast.makeText(this, "Error retrieving data: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                QuerySnapshot organizerSnapshot = task1.getResult();
                                if (organizerSnapshot != null && !organizerSnapshot.isEmpty()) {
                                    // Handle organizer login
                                    for (QueryDocumentSnapshot document : organizerSnapshot) {
                                        String storedPassword = document.getString("password");
                                        if (storedPassword != null && storedPassword.equals(password)) {
                                            // Store the user's email (username) for later use
                                            SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("organizerEmail", username);  // Save the email
                                            editor.apply();

                                            // Login successful as Organizer
                                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, OrganizerWelcomePageActivity.class);
                                            startActivity(intent);
                                            finish();
                                            return;
                                        }
                                    }

                                    Toast.makeText(this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // If not found in organizers, check rejections
                                db.collection("rejections")
                                        .whereEqualTo("email", username)
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (!task2.isSuccessful()) {
                                                Toast.makeText(this, "Error retrieving data: " + task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            QuerySnapshot rejectionsSnapshot = task2.getResult();
                                            if (rejectionsSnapshot != null && !rejectionsSnapshot.isEmpty()) {
                                                // User request denied
                                                Toast.makeText(this, "User request denied, please contact administrator at (613) 124-2342.", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            // If not found in rejections, check pending registration requests
                                            db.collection("registration_requests")
                                                    .whereEqualTo("email", username)
                                                    .get()
                                                    .addOnCompleteListener(task3 -> {
                                                        if (!task3.isSuccessful()) {
                                                            Toast.makeText(this, "Error retrieving data: " + task3.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }

                                                        QuerySnapshot pendingSnapshot = task3.getResult();
                                                        if (pendingSnapshot != null && !pendingSnapshot.isEmpty()) {
                                                            // User request is pending
                                                            Toast.makeText(this, "Your request is still pending approval. Please check back later.", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }

                                                        // No user found in any collection
                                                        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                                                    });
                                        });
                            });
                });
    }

}
