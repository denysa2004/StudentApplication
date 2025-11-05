package com.example.studentapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class WeekScheduleActivity extends ComponentActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;


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

        Button logoutButton = findViewById(R.id.button4);
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(WeekScheduleActivity.this, MainActivity1.class));
            finish();
        });


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

        loadScheduleFromFirebase();
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

        switch (type.toLowerCase()) {
            case "course":
            case "curs":
                subjectView.setTextColor(Color.RED);
                break;
            case "seminar":
                subjectView.setTextColor(Color.BLUE);
                break;
            case "lab":
            case "laborator":
                subjectView.setTextColor(Color.parseColor("#228B22"));
                break;
            default:
                subjectView.setTextColor(Color.DKGRAY);
                break;
        }

        android.view.ViewGroup parent = (android.view.ViewGroup) buttonClicked.getParent();
        int index = parent.indexOfChild(buttonClicked);
        parent.removeView(buttonClicked);
        parent.addView(subjectView, index);
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

                        switch (time) {
                            case "Monday 08:00":
                                displaySubject(addMon8, subject, type);
                                break;
                            case "Tuesday 08:00":
                                displaySubject(addTue8, subject, type);
                                break;
                            case "Wednesday 08:00":
                                displaySubject(addWed8, subject, type);
                                break;
                            case "Thursday 08:00":
                                displaySubject(addThu8, subject, type);
                                break;
                            case "Friday 08:00":
                                displaySubject(addFri8, subject, type);
                                break;
                            case "Monday 10:00":
                                displaySubject(addMon10, subject, type);
                                break;
                            case "Tuesday 10:00":
                                displaySubject(addTue10, subject, type);
                                break;
                            case "Wednesday 10:00":
                                displaySubject(addWed10, subject, type);
                                break;
                            case "Thursday 10:00":
                                displaySubject(addThu10, subject, type);
                                break;
                            case "Friday 10:00":
                                displaySubject(addFri10, subject, type);
                                break;

                            case "Monday 12:00":
                                displaySubject(addMon12, subject, type);
                                break;
                            case "Tuesday 12:00":
                                displaySubject(addTue12, subject, type);
                                break;
                            case "Wednesday 12:00":
                                displaySubject(addWed12, subject, type);
                                break;
                            case "Thursday 12:00":
                                displaySubject(addThu12, subject, type);
                                break;
                            case "Friday 12:00":
                                displaySubject(addFri12, subject, type);
                                break;

                            case "Monday 14:00":
                                displaySubject(addMon14, subject, type);
                                break;
                            case "Tuesday 14:00":
                                displaySubject(addTue14, subject, type);
                                break;
                            case "Wednesday 14:00":
                                displaySubject(addWed14, subject, type);
                                break;
                            case "Thursday 14:00":
                                displaySubject(addThu14, subject, type);
                                break;
                            case "Friday 14:00":
                                displaySubject(addFri14, subject, type);
                                break;
                            case "Monday 16:00":
                                displaySubject(addMon16, subject, type);
                                break;
                            case "Tuesday 16:00":
                                displaySubject(addTue16, subject, type);
                                break;
                            case "Wednesday 16:00":
                                displaySubject(addWed16, subject, type);
                                break;
                            case "Thursday 16:00":
                                displaySubject(addThu16, subject, type);
                                break;
                            case "Friday 16:00":
                                displaySubject(addFri16, subject, type);
                                break;

                            case "Monday 18:00":
                                displaySubject(addMon18, subject, type);
                                break;
                            case "Tuesday 18:00":
                                displaySubject(addTue18, subject, type);
                                break;
                            case "Wednesday 18:00":
                                displaySubject(addWed18, subject, type);
                                break;
                            case "Thursday 18:00":
                                displaySubject(addThu18, subject, type);
                                break;
                            case "Friday 18:00":
                                displaySubject(addFri18, subject, type);
                                break;
                            case "Monday 20:00":
                                displaySubject(addMon20, subject, type);
                                break;
                            case "Tuesday 20:00":
                                displaySubject(addTue20, subject, type);
                                break;
                            case "Wednesday 20:00":
                                displaySubject(addWed20, subject, type);
                                break;
                            case "Thursday 20:00":
                                displaySubject(addThu20, subject, type);
                                break;
                            case "Friday 20:00":
                                displaySubject(addFri20, subject, type);
                                break;

                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
