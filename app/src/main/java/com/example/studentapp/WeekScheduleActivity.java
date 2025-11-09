package com.example.studentapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeekScheduleActivity extends ComponentActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ScrollView weekScrollView, dayScrollView;
    private Switch daySwitch;
    private Spinner daySpinner;
    private LinearLayout dayTimeSlotsContainer;
    private TextView selectedDayTitle;

    private String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private String[] timeSlots = {"08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00"};

    private Map<String, Map<String, SubjectData>> scheduleData = new HashMap<>();

    private ImageButton addMon8, addTue8, addWed8, addThu8, addFri8,
            addMon10, addTue10, addWed10, addThu10, addFri10,
            addMon12, addTue12, addWed12, addThu12, addFri12,
            addMon14, addTue14, addWed14, addThu14, addFri14,
            addMon16, addTue16, addWed16, addThu16, addFri16,
            addMon18, addTue18, addWed18, addThu18, addFri18,
            addMon20, addTue20, addWed20, addThu20, addFri20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weekschedule);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupDaySwitch();
        setupDaySpinner();
        setupLogoutButton();
        initializeScheduleButtons();
        loadScheduleFromFirebase();
    }

    private void initializeViews() {
        weekScrollView = findViewById(R.id.weekScrollView);
        dayScrollView = findViewById(R.id.dayScrollView);
        daySwitch = findViewById(R.id.switch1);
        daySpinner = findViewById(R.id.daySpinner);
        dayTimeSlotsContainer = findViewById(R.id.dayTimeSlots);
        selectedDayTitle = findViewById(R.id.selectedDayTitle);

        // Setup search functionality
        android.widget.SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchSubject(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchSubject(String subjectName) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("default")
                .whereEqualTo("userId", userId)
                .whereEqualTo("subject", subjectName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        com.google.firebase.firestore.QueryDocumentSnapshot doc =
                                (com.google.firebase.firestore.QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String subject = doc.getString("subject");
                        String type = doc.getString("type");
                        String timeSlot = doc.getString("time");

                        openSubjectDetails(subject, type, timeSlot);
                    } else {
                        Toast.makeText(this, "Subject not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupDaySwitch() {
        daySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show day view
                weekScrollView.setVisibility(View.GONE);
                dayScrollView.setVisibility(View.VISIBLE);
                daySpinner.setVisibility(View.VISIBLE);
                updateDayView(daysOfWeek[0]);
            } else {
                // Show week view
                weekScrollView.setVisibility(View.VISIBLE);
                dayScrollView.setVisibility(View.GONE);
                daySpinner.setVisibility(View.GONE);
            }
        });
    }

    private void setupDaySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, daysOfWeek);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);

        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDayView(daysOfWeek[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateDayView(String selectedDay) {
        selectedDayTitle.setText(selectedDay + " Schedule");
        dayTimeSlotsContainer.removeAllViews();

        for (String time : timeSlots) {
            String timeSlotKey = selectedDay + " " + time;

            LinearLayout timeSlotLayout = new LinearLayout(this);
            timeSlotLayout.setOrientation(LinearLayout.HORIZONTAL);
            timeSlotLayout.setPadding(16, 16, 16, 16);
            timeSlotLayout.setBackgroundColor(Color.WHITE);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 8, 0, 8);
            timeSlotLayout.setLayoutParams(layoutParams);

            // Time label
            TextView timeLabel = new TextView(this);
            timeLabel.setText(time);
            timeLabel.setTextSize(18);
//            timeLabel.setTextStyle(android.graphics.Typeface.BOLD);
            timeLabel.setPadding(16, 16, 16, 16);
            timeLabel.setTextColor(Color.parseColor("#B08BF2"));
            LinearLayout.LayoutParams timeLabelParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            timeLabel.setLayoutParams(timeLabelParams);

            // Subject info container
            LinearLayout subjectContainer = new LinearLayout(this);
            subjectContainer.setOrientation(LinearLayout.VERTICAL);
            subjectContainer.setPadding(16, 16, 16, 16);
            subjectContainer.setBackgroundColor(Color.parseColor("#FFE6EF"));
            LinearLayout.LayoutParams subjectContainerParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    3
            );
            subjectContainer.setLayoutParams(subjectContainerParams);

            // Check if there's data for this time slot
            if (scheduleData.containsKey(selectedDay) &&
                    scheduleData.get(selectedDay).containsKey(time)) {

                SubjectData data = scheduleData.get(selectedDay).get(time);

                TextView subjectText = new TextView(this);
                subjectText.setText(data.subject);
                subjectText.setTextSize(16);
//                subjectText.setTextStyle(android.graphics.Typeface.BOLD);

                TextView typeText = new TextView(this);
                typeText.setText("(" + data.type + ")");
                typeText.setTextSize(14);

                // Set colors based on type
                int textColor = getColorForType(data.type);
                subjectText.setTextColor(textColor);
                typeText.setTextColor(textColor);

                subjectContainer.addView(subjectText);
                subjectContainer.addView(typeText);

                // Make clickable
                subjectContainer.setClickable(true);
                subjectContainer.setFocusable(true);
                final String finalTimeSlot = timeSlotKey;
                final String finalSubject = data.subject;
                final String finalType = data.type;
                subjectContainer.setOnClickListener(v ->
                        openSubjectDetails(finalSubject, finalType, finalTimeSlot));

            } else {
                TextView emptyText = new TextView(this);
                emptyText.setText("No class");
                emptyText.setTextSize(14);
                emptyText.setTextColor(Color.GRAY);
                emptyText.setGravity(Gravity.CENTER);
                subjectContainer.addView(emptyText);
            }

            timeSlotLayout.addView(timeLabel);
            timeSlotLayout.addView(subjectContainer);
            dayTimeSlotsContainer.addView(timeSlotLayout);
        }
    }

    private int getColorForType(String type) {
        switch (type.toLowerCase()) {
            case "course":
            case "curs":
                return Color.RED;
            case "seminar":
                return Color.BLUE;
            case "lab":
            case "laborator":
                return Color.parseColor("#228B22");
            default:
                return Color.DKGRAY;
        }
    }

    private void setupLogoutButton() {
        Button logoutButton = findViewById(R.id.button4);
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(WeekScheduleActivity.this, MainActivity1.class));
            finish();
        });
    }

    private void initializeScheduleButtons() {
        addMon8 = findViewById(R.id.addMon8);
        addMon10 = findViewById(R.id.addMon10);
        addMon12 = findViewById(R.id.addMon12);
        addMon14 = findViewById(R.id.addMon14);
        addMon16 = findViewById(R.id.addMon16);
        addMon18 = findViewById(R.id.addMon18);
        addMon20 = findViewById(R.id.addMon20);

        addTue8 = findViewById(R.id.addTue8);
        addTue10 = findViewById(R.id.addTue10);
        addTue12 = findViewById(R.id.addTue12);
        addTue14 = findViewById(R.id.addTue14);
        addTue16 = findViewById(R.id.addTue16);
        addTue18 = findViewById(R.id.addTue18);
        addTue20 = findViewById(R.id.addTue20);

        addWed8 = findViewById(R.id.addWed8);
        addWed10 = findViewById(R.id.addWed10);
        addWed12 = findViewById(R.id.addWed12);
        addWed14 = findViewById(R.id.addWed14);
        addWed16 = findViewById(R.id.addWed16);
        addWed18 = findViewById(R.id.addWed18);
        addWed20 = findViewById(R.id.addWed20);

        addThu8 = findViewById(R.id.addThu8);
        addThu10 = findViewById(R.id.addThu10);
        addThu12 = findViewById(R.id.addThu12);
        addThu14 = findViewById(R.id.addThu14);
        addThu16 = findViewById(R.id.addThu16);
        addThu18 = findViewById(R.id.addThu18);
        addThu20 = findViewById(R.id.addThu20);

        addFri8 = findViewById(R.id.addFri8);
        addFri10 = findViewById(R.id.addFri10);
        addFri12 = findViewById(R.id.addFri12);
        addFri14 = findViewById(R.id.addFri14);
        addFri16 = findViewById(R.id.addFri16);
        addFri18 = findViewById(R.id.addFri18);
        addFri20 = findViewById(R.id.addFri20);

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        addMon8.setOnClickListener(v -> showAddDialog(addMon8, "Monday 08:00"));
        addMon10.setOnClickListener(v -> showAddDialog(addMon10, "Monday 10:00"));
        addMon12.setOnClickListener(v -> showAddDialog(addMon12, "Monday 12:00"));
        addMon14.setOnClickListener(v -> showAddDialog(addMon14, "Monday 14:00"));
        addMon16.setOnClickListener(v -> showAddDialog(addMon16, "Monday 16:00"));
        addMon18.setOnClickListener(v -> showAddDialog(addMon18, "Monday 18:00"));
        addMon20.setOnClickListener(v -> showAddDialog(addMon20, "Monday 20:00"));

        addTue8.setOnClickListener(v -> showAddDialog(addTue8, "Tuesday 08:00"));
        addTue10.setOnClickListener(v -> showAddDialog(addTue10, "Tuesday 10:00"));
        addTue12.setOnClickListener(v -> showAddDialog(addTue12, "Tuesday 12:00"));
        addTue14.setOnClickListener(v -> showAddDialog(addTue14, "Tuesday 14:00"));
        addTue16.setOnClickListener(v -> showAddDialog(addTue16, "Tuesday 16:00"));
        addTue18.setOnClickListener(v -> showAddDialog(addTue18, "Tuesday 18:00"));
        addTue20.setOnClickListener(v -> showAddDialog(addTue20, "Tuesday 20:00"));

        addWed8.setOnClickListener(v -> showAddDialog(addWed8, "Wednesday 08:00"));
        addWed10.setOnClickListener(v -> showAddDialog(addWed10, "Wednesday 10:00"));
        addWed12.setOnClickListener(v -> showAddDialog(addWed12, "Wednesday 12:00"));
        addWed14.setOnClickListener(v -> showAddDialog(addWed14, "Wednesday 14:00"));
        addWed16.setOnClickListener(v -> showAddDialog(addWed16, "Wednesday 16:00"));
        addWed18.setOnClickListener(v -> showAddDialog(addWed18, "Wednesday 18:00"));
        addWed20.setOnClickListener(v -> showAddDialog(addWed20, "Wednesday 20:00"));

        addThu8.setOnClickListener(v -> showAddDialog(addThu8, "Thursday 08:00"));
        addThu10.setOnClickListener(v -> showAddDialog(addThu10, "Thursday 10:00"));
        addThu12.setOnClickListener(v -> showAddDialog(addThu12, "Thursday 12:00"));
        addThu14.setOnClickListener(v -> showAddDialog(addThu14, "Thursday 14:00"));
        addThu16.setOnClickListener(v -> showAddDialog(addThu16, "Thursday 16:00"));
        addThu18.setOnClickListener(v -> showAddDialog(addThu18, "Thursday 18:00"));
        addThu20.setOnClickListener(v -> showAddDialog(addThu20, "Thursday 20:00"));

        addFri8.setOnClickListener(v -> showAddDialog(addFri8, "Friday 08:00"));
        addFri10.setOnClickListener(v -> showAddDialog(addFri10, "Friday 10:00"));
        addFri12.setOnClickListener(v -> showAddDialog(addFri12, "Friday 12:00"));
        addFri14.setOnClickListener(v -> showAddDialog(addFri14, "Friday 14:00"));
        addFri16.setOnClickListener(v -> showAddDialog(addFri16, "Friday 16:00"));
        addFri18.setOnClickListener(v -> showAddDialog(addFri18, "Friday 18:00"));
        addFri20.setOnClickListener(v -> showAddDialog(addFri20, "Friday 20:00"));
    }

    private void showAddDialog(ImageButton buttonClicked, String timeSlot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add subject for " + timeSlot);

        final EditText subjectInput = new EditText(this);
        subjectInput.setHint("Subject name");

        final EditText typeInput = new EditText(this);
        typeInput.setHint("Course / Seminar / Lab");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(subjectInput);
        layout.addView(typeInput);
        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String subject = subjectInput.getText().toString().trim();
            String type = typeInput.getText().toString().trim();

            if (subject.isEmpty() || type.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> subjectData = new HashMap<>();
            subjectData.put("time", timeSlot);
            subjectData.put("subject", subject);
            subjectData.put("type", type);
            subjectData.put("userId", auth.getCurrentUser().getUid());

            db.collection("default")
                    .add(subjectData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Saved to cloud", Toast.LENGTH_SHORT).show();
                        displaySubject(buttonClicked, subject, type);

                        // Update local data structure
                        String[] parts = timeSlot.split(" ");
                        String day = parts[0];
                        String time = parts[1];

                        if (!scheduleData.containsKey(day)) {
                            scheduleData.put(day, new HashMap<>());
                        }
                        scheduleData.get(day).put(time, new SubjectData(subject, type));

                        // Update day view if it's currently visible
                        if (daySwitch.isChecked()) {
                            updateDayView((String) daySpinner.getSelectedItem());
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void displaySubject(ImageButton buttonClicked, String subject, String type) {
        TextView subjectView = new TextView(this);
        subjectView.setText(subject + "\n(" + type + ")");
        subjectView.setGravity(Gravity.CENTER);
        subjectView.setTextSize(13);
        subjectView.setPadding(15, 15, 15, 15);
        subjectView.setBackgroundColor(Color.parseColor("#FFE6EF"));
        subjectView.setTextColor(getColorForType(type));
        subjectView.setClickable(true);

        // Get the time slot for this button
        String timeSlot = getTimeSlotForButton(buttonClicked);

        // Set click listener to open details
        subjectView.setOnClickListener(v -> openSubjectDetails(subject, type, timeSlot));

        android.view.ViewGroup parent = (android.view.ViewGroup) buttonClicked.getParent();
        int index = parent.indexOfChild(buttonClicked);
        parent.removeView(buttonClicked);
        parent.addView(subjectView, index);
    }

    private String getTimeSlotForButton(ImageButton button) {
        int id = button.getId();
        if (id == R.id.addMon8) return "Monday 08:00";
        else if (id == R.id.addTue8) return "Tuesday 08:00";
        else if (id == R.id.addWed8) return "Wednesday 08:00";
        else if (id == R.id.addThu8) return "Thursday 08:00";
        else if (id == R.id.addFri8) return "Friday 08:00";
        else if (id == R.id.addMon10) return "Monday 10:00";
        else if (id == R.id.addTue10) return "Tuesday 10:00";
        else if (id == R.id.addWed10) return "Wednesday 10:00";
        else if (id == R.id.addThu10) return "Thursday 10:00";
        else if (id == R.id.addFri10) return "Friday 10:00";
        else if (id == R.id.addMon12) return "Monday 12:00";
        else if (id == R.id.addTue12) return "Tuesday 12:00";
        else if (id == R.id.addWed12) return "Wednesday 12:00";
        else if (id == R.id.addThu12) return "Thursday 12:00";
        else if (id == R.id.addFri12) return "Friday 12:00";
        else if (id == R.id.addMon14) return "Monday 14:00";
        else if (id == R.id.addTue14) return "Tuesday 14:00";
        else if (id == R.id.addWed14) return "Wednesday 14:00";
        else if (id == R.id.addThu14) return "Thursday 14:00";
        else if (id == R.id.addFri14) return "Friday 14:00";
        else if (id == R.id.addMon16) return "Monday 16:00";
        else if (id == R.id.addTue16) return "Tuesday 16:00";
        else if (id == R.id.addWed16) return "Wednesday 16:00";
        else if (id == R.id.addThu16) return "Thursday 16:00";
        else if (id == R.id.addFri16) return "Friday 16:00";
        else if (id == R.id.addMon18) return "Monday 18:00";
        else if (id == R.id.addTue18) return "Tuesday 18:00";
        else if (id == R.id.addWed18) return "Wednesday 18:00";
        else if (id == R.id.addThu18) return "Thursday 18:00";
        else if (id == R.id.addFri18) return "Friday 18:00";
        else if (id == R.id.addMon20) return "Monday 20:00";
        else if (id == R.id.addTue20) return "Tuesday 20:00";
        else if (id == R.id.addWed20) return "Wednesday 20:00";
        else if (id == R.id.addThu20) return "Thursday 20:00";
        else if (id == R.id.addFri20) return "Friday 20:00";
        return "";
    }

    private void openSubjectDetails(String subject, String type, String timeSlot) {
        Intent intent = new Intent(WeekScheduleActivity.this, SubjectDetailsActivity.class);
        intent.putExtra("subjectName", subject);
        intent.putExtra("subjectType", type);
        intent.putExtra("timeSlot", timeSlot);
        startActivity(intent);
    }

    private void loadScheduleFromFirebase() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("default")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String time = doc.getString("time");
                        String subject = doc.getString("subject");
                        String type = doc.getString("type");

                        if (time == null) continue;

                        // Store data in local structure
                        String[] parts = time.split(" ");
                        if (parts.length >= 2) {
                            String day = parts[0];
                            String timeSlot = parts[1];

                            if (!scheduleData.containsKey(day)) {
                                scheduleData.put(day, new HashMap<>());
                            }
                            scheduleData.get(day).put(timeSlot, new SubjectData(subject, type));
                        }

                        ImageButton button = getButtonForTimeSlot(time);
                        if (button != null) {
                            displaySubject(button, subject, type);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private ImageButton getButtonForTimeSlot(String timeSlot) {
        switch (timeSlot) {
            case "Monday 08:00": return addMon8;
            case "Tuesday 08:00": return addTue8;
            case "Wednesday 08:00": return addWed8;
            case "Thursday 08:00": return addThu8;
            case "Friday 08:00": return addFri8;
            case "Monday 10:00": return addMon10;
            case "Tuesday 10:00": return addTue10;
            case "Wednesday 10:00": return addWed10;
            case "Thursday 10:00": return addThu10;
            case "Friday 10:00": return addFri10;
            case "Monday 12:00": return addMon12;
            case "Tuesday 12:00": return addTue12;
            case "Wednesday 12:00": return addWed12;
            case "Thursday 12:00": return addThu12;
            case "Friday 12:00": return addFri12;
            case "Monday 14:00": return addMon14;
            case "Tuesday 14:00": return addTue14;
            case "Wednesday 14:00": return addWed14;
            case "Thursday 14:00": return addThu14;
            case "Friday 14:00": return addFri14;
            case "Monday 16:00": return addMon16;
            case "Tuesday 16:00": return addTue16;
            case "Wednesday 16:00": return addWed16;
            case "Thursday 16:00": return addThu16;
            case "Friday 16:00": return addFri16;
            case "Monday 18:00": return addMon18;
            case "Tuesday 18:00": return addTue18;
            case "Wednesday 18:00": return addWed18;
            case "Thursday 18:00": return addThu18;
            case "Friday 18:00": return addFri18;
            case "Monday 20:00": return addMon20;
            case "Tuesday 20:00": return addTue20;
            case "Wednesday 20:00": return addWed20;
            case "Thursday 20:00": return addThu20;
            case "Friday 20:00": return addFri20;
            default: return null;
        }
    }

    // Helper class to store subject data
    private static class SubjectData {
        String subject;
        String type;

        SubjectData(String subject, String type) {
            this.subject = subject;
            this.type = type;
        }
    }
}