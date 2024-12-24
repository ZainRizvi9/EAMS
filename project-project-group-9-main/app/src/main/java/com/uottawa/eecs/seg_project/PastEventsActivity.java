package com.uottawa.eecs.seg_project;

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

public class PastEventsActivity extends AppCompatActivity {
    private ListView eventsListView;
    private FirebaseFirestore db;
    private List<String> eventList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_past_events);

        eventsListView = findViewById(R.id.eventsListView2);
        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventList);
        eventsListView.setAdapter(adapter);

        loadPastEvents();

        Button backButton = findViewById(R.id.backButton10);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity and return to the previous page
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadPastEvents() {
        Date currentDate = Calendar.getInstance().getTime();

        db.collection("events")
                .whereLessThan("date", currentDate)
                .orderBy("date", Query.Direction.DESCENDING)
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
                        Toast.makeText(this, "No past events found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show());
    }
}