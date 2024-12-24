package com.uottawa.eecs.seg_project;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText, addressEditText;
    private TextView dateTextView, startTimeTextView, endTimeTextView;
    private Switch approvalSwitch;
    private Button createEventButton;

    private Calendar selectedDate;
    private Calendar selectedStartTime;
    private Calendar selectedEndTime;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String organizerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        //Got from login activity
        SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        organizerEmail = sharedPreferences.getString("organizerEmail", "");  // Retrieve the email

        Button backButton = findViewById(R.id.backButton9);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity and return to the previous page
                finish();
            }
        });

        // Initialize views
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        addressEditText = findViewById(R.id.addressEditText);
        dateTextView = findViewById(R.id.dateTextView);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        endTimeTextView = findViewById(R.id.endTimeTextView);
        approvalSwitch = findViewById(R.id.approvalSwitch);
        createEventButton = findViewById(R.id.createEventButton);

        selectedDate = Calendar.getInstance();
        selectedStartTime = Calendar.getInstance();
        selectedEndTime = Calendar.getInstance();

        // Date selection
        dateTextView.setOnClickListener(v -> openDatePicker());

        // Start time selection
        startTimeTextView.setOnClickListener(v -> openTimePicker(true));

        // End time selection
        endTimeTextView.setOnClickListener(v -> openTimePicker(false));

        // Create Event button listener
        createEventButton.setOnClickListener(v -> createEvent());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void openDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateText();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void updateDateText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateTextView.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void openTimePicker(boolean isStartTime) {
        int hour = isStartTime ? selectedStartTime.get(Calendar.HOUR_OF_DAY) : selectedEndTime.get(Calendar.HOUR_OF_DAY);
        int minute = isStartTime ? selectedStartTime.get(Calendar.MINUTE) : selectedEndTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    // Adjust to nearest 30-minute interval
                    selectedMinute = (selectedMinute / 30) * 30;

                    if (isStartTime) {
                        selectedStartTime.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
                        selectedStartTime.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
                        selectedStartTime.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));
                        selectedStartTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                        selectedStartTime.set(Calendar.MINUTE, selectedMinute);
                        startTimeTextView.setText(formatTime(selectedStartTime));
                    } else {
                        selectedEndTime.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
                        selectedEndTime.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
                        selectedEndTime.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));
                        selectedEndTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                        selectedEndTime.set(Calendar.MINUTE, selectedMinute);
                        endTimeTextView.setText(formatTime(selectedEndTime));
                    }
                },
                hour, minute, true);

        timePickerDialog.show();
    }

    private String formatTime(Calendar time) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(time.getTime());
    }

    private void createEvent() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        boolean isManualApproval = approvalSwitch.isChecked();

        // Checks to see if entered information is correct according to the field
        if (title.isEmpty() || description.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedStartTime == null || selectedEndTime == null) {
            Toast.makeText(this, "Please select both start and end times.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedStartTime.after(selectedEndTime)) {
            Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("description", description);
        event.put("address", address);
        event.put("date", selectedStartTime.getTime());
        event.put("startTime", selectedStartTime.getTime());
        event.put("endTime", selectedEndTime.getTime());
        event.put("automaticApproval", isManualApproval);
        event.put("organizerEmail", organizerEmail);

        db.collection("events").document(title)
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        finish();
    }
}