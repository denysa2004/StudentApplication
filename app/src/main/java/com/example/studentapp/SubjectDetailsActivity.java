package com.example.studentapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectDetailsActivity extends ComponentActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String subjectName;
    private String subjectType;
    private String timeSlot;
    private String documentId;

    private TextView professorName, locationText, examDateText, finalGradeText;
    private TextView attendanceCount;
    private LinearLayout attendanceDotsContainer, gradesList, notesList, pdfList;
    private MapView mapView;

    private int totalClasses = 0;
    private int attendedClasses = 0;

    private ActivityResultLauncher<Intent> pdfPickerLauncher;
    private ActivityResultLauncher<Intent> locationPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid configuration BEFORE setting the content view
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.subjectdetails);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_LONG).show();
            finish();
            return; // Stop further execution
        }

        // Get data from intent
        subjectName = getIntent().getStringExtra("subjectName");
        subjectType = getIntent().getStringExtra("subjectType");
        timeSlot = getIntent().getStringExtra("timeSlot");

        initializeViews();
        setupLaunchers();
        setupButtons();
        loadSubjectDetails();
    }

    private void initializeViews() {
        TextView subjectTitle = findViewById(R.id.subjectTitle);
        TextView subjectTypeText = findViewById(R.id.subjectType);
        professorName = findViewById(R.id.professorName);
        locationText = findViewById(R.id.locationText);
        examDateText = findViewById(R.id.examDateText);
        finalGradeText = findViewById(R.id.finalGradeText);
        attendanceCount = findViewById(R.id.attendanceCount);
        attendanceDotsContainer = findViewById(R.id.attendanceDotsContainer);
        gradesList = findViewById(R.id.gradesList);
        notesList = findViewById(R.id.notesList);
        pdfList = findViewById(R.id.pdfList);
        mapView = findViewById(R.id.mapView);

        subjectTitle.setText(subjectName);
        subjectTypeText.setText(subjectType);

        int color = getColorForType(subjectType);
        subjectTypeText.setTextColor(color);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
    }

    private void setupLaunchers() {
        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri pdfUri = result.getData().getData();
                        if (pdfUri != null) {
                            showPdfNameDialog(pdfUri);
                        }
                    }
                });

        locationPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        double lat = data.getDoubleExtra("latitude", 0);
                        double lon = data.getDoubleExtra("longitude", 0);
                        String name = data.getStringExtra("address");

                        GeoPoint geoPoint = new GeoPoint(lat, lon);
                        updateLocation(geoPoint, name);
                        updateFieldInFirebase("location", geoPoint);
                        updateFieldInFirebase("locationName", name);
                    }
                });
    }

    private void setupButtons() {
        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> finish());

        Button editProfessorButton = findViewById(R.id.editProfessorButton);
        editProfessorButton.setOnClickListener(v -> showEditDialog("Professor", professorName.getText().toString(), "professor"));

        Button pickLocationButton = findViewById(R.id.pickLocationButton);
        pickLocationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationPickerActivity.class);
            locationPickerLauncher.launch(intent);
        });

        Button editExamDateButton = findViewById(R.id.editExamDateButton);
        editExamDateButton.setOnClickListener(v -> showDatePickerDialog());

        Button editFinalGradeButton = findViewById(R.id.editFinalGradeButton);
        editFinalGradeButton.setOnClickListener(v -> showEditDialog("Final Grade", finalGradeText.getText().toString(), "finalGrade"));

        Button setTotalClassesButton = findViewById(R.id.setTotalClassesButton);
        setTotalClassesButton.setOnClickListener(v -> showSetTotalClassesDialog());

        Button addGradeButton = findViewById(R.id.addGradeButton);
        addGradeButton.setOnClickListener(v -> showAddGradeDialog());

        Button addNoteButton = findViewById(R.id.addNoteButton);
        addNoteButton.setOnClickListener(v -> showAddNoteDialog());

        Button addPdfButton = findViewById(R.id.addPdfButton);
        addPdfButton.setOnClickListener(v -> pickPdfFile());
    }

    private void loadSubjectDetails() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();

        db.collection("default")
                .whereEqualTo("userId", userId)
                .whereEqualTo("time", timeSlot)
                .whereEqualTo("subject", subjectName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        documentId = document.getId();

                        String prof = document.getString("professor");
                        if (prof != null && !prof.isEmpty()) {
                            professorName.setText(prof);
                        }

                        GeoPoint geoPoint = document.getGeoPoint("location");
                        String locationName = document.getString("locationName");
                        if (geoPoint != null) {
                            updateLocation(geoPoint, locationName);
                        }

                        String examDate = document.getString("examDate");
                        if (examDate != null && !examDate.isEmpty()) {
                            examDateText.setText(examDate);
                        }

                        String finalGrade = document.getString("finalGrade");
                        if (finalGrade != null && !finalGrade.isEmpty()) {
                            finalGradeText.setText(finalGrade);
                        }

                        Long totalClassesLong = document.getLong("totalClasses");
                        if (totalClassesLong != null) {
                            totalClasses = totalClassesLong.intValue();
                        }

                        Long attendedClassesLong = document.getLong("attendedClasses");
                        if (attendedClassesLong != null) {
                            attendedClasses = attendedClassesLong.intValue();
                        }

                        updateAttendanceDisplay();

                        List<Map<String, Object>> grades = (List<Map<String, Object>>) document.get("grades");
                        if (grades != null) {
                            for (Map<String, Object> grade : grades) {
                                if (grade.get("name") != null && grade.get("value") != null) {
                                    addGradeToList((String) grade.get("name"), grade.get("value").toString());
                                }
                            }
                        }

                        List<String> notes = (List<String>) document.get("notes");
                        if (notes != null) {
                            for (String note : notes) {
                                addNoteToList(note);
                            }
                        }

                        List<Map<String, Object>> pdfs = (List<Map<String, Object>>) document.get("pdfFiles");
                        if (pdfs != null) {
                            for (Map<String, Object> pdf : pdfs) {
                                if (pdf.get("name") != null && pdf.get("base64") != null) {
                                    addPdfToList((String) pdf.get("name"), (String) pdf.get("base64"));
                                }
                            }
                        }

                    } else {
                        Toast.makeText(this, "Subject not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateLocation(GeoPoint geoPoint, String locationName) {
        locationText.setText(locationName != null ? locationName : "Location set");
        mapView.setVisibility(View.VISIBLE);
        IMapController mapController = mapView.getController();
        mapController.setZoom(18.0);
        org.osmdroid.util.GeoPoint point = new org.osmdroid.util.GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude());
        mapController.setCenter(point);

        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(point);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().clear();
        mapView.getOverlays().add(startMarker);
        mapView.invalidate();
    }

    private void pickPdfFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfPickerLauncher.launch(intent);
    }

    private void showPdfNameDialog(Uri pdfUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Name this PDF");

        EditText input = new EditText(this);
        input.setHint("Example: Week 1 Course");
        input.setPadding(50, 40, 50, 10);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String customName = input.getText().toString().trim();
            if (customName.isEmpty()) {
                Toast.makeText(this, "PDF name required", Toast.LENGTH_SHORT).show();
                return;
            }
            encodeAndSavePdf(customName, pdfUri);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void encodeAndSavePdf(String name, Uri pdfUri) {
        if (documentId == null) return;

        try {
            InputStream inputStream = getContentResolver().openInputStream(pdfUri);
            if (inputStream == null) {
                Toast.makeText(this, "Cannot read file", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] bytes = readAllBytes(inputStream);
            inputStream.close();

            String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);

            savePdfToFirestore(name, base64);

        } catch (Exception e) {
            Toast.makeText(this, "PDF read failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePdfToFirestore(String name, String base64) {
        db.collection("default")
                .document(documentId)
                .get()
                .addOnSuccessListener(doc -> {

                    List<Map<String, Object>> pdfs = (List<Map<String, Object>>) doc.get("pdfFiles");
                    if (pdfs == null) pdfs = new ArrayList<>();

                    Map<String, Object> pdfData = new HashMap<>();
                    pdfData.put("name", name);
                    pdfData.put("base64", base64);

                    pdfs.add(pdfData);

                    db.collection("default")
                            .document(documentId)
                            .update("pdfFiles", pdfs)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "PDF saved", Toast.LENGTH_SHORT).show();
                                addPdfToList(name, base64);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Firestore save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    private void addPdfToList(String name, String base64) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(16, 12, 16, 12);
        item.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        item.setLayoutParams(params);

        TextView pdfText = new TextView(this);
        pdfText.setText("📄 " + name);
        pdfText.setTextSize(16);
        pdfText.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        pdfText.setOnClickListener(v -> openPdfFromBase64(name, base64));

        Button deleteBtn = new Button(this);
        deleteBtn.setText("Delete");
        deleteBtn.setTextSize(12);
        deleteBtn.setTextColor(Color.RED);

        deleteBtn.setOnClickListener(v -> {
            pdfList.removeView(item);
            deletePdfFromFirestore(base64);
        });

        item.addView(pdfText);
        item.addView(deleteBtn);
        pdfList.addView(item);
    }

    private void openPdfFromBase64(String name, String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.NO_WRAP);

            File pdfFile = new File(getCacheDir(), name.replaceAll("\\s+", "_") + ".pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(bytes);
            fos.close();

            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    pdfFile
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open PDF"));

        } catch (Exception e) {
            Toast.makeText(this, "Open failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePdfFromFirestore(String base64) {
        if (documentId == null) return;

        db.collection("default")
                .document(documentId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<Map<String, Object>> pdfs = (List<Map<String, Object>>) doc.get("pdfFiles");
                    if (pdfs != null) {
                        pdfs.removeIf(p -> base64.equals(p.get("base64")));

                        db.collection("default")
                                .document(documentId)
                                .update("pdfFiles", pdfs)
                                .addOnSuccessListener(v ->
                                        Toast.makeText(this, "PDF deleted", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private byte[] readAllBytes(InputStream inputStream) throws Exception {
        ArrayList<Byte> buffer = new ArrayList<>();
        int b;
        while ((b = inputStream.read()) != -1) {
            buffer.add((byte) b);
        }
        byte[] bytes = new byte[buffer.size()];
        for (int i = 0; i < buffer.size(); i++) {
            bytes[i] = buffer.get(i);
        }
        return bytes;
    }

    private void showEditDialog(String title, String currentValue, String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit " + title);

        final EditText input = new EditText(this);
        if (!currentValue.equals("Not set")) {
            input.setText(currentValue);
        }
        input.setPadding(50, 40, 50, 10);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                updateFieldInFirebase(field, value);

                switch (field) {
                    case "professor":
                        professorName.setText(value);
                        break;
                    case "location":
                        locationText.setText(value);
                        break;
                    case "finalGrade":
                        finalGradeText.setText(value);
                        break;
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    examDateText.setText(date);
                    updateFieldInFirebase("examDate", date);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showSetTotalClassesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Total Number of Classes");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter total classes");
        if (totalClasses > 0) {
            input.setText(String.valueOf(totalClasses));
        }
        input.setPadding(50, 40, 50, 10);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                totalClasses = Integer.parseInt(value);
                updateFieldInFirebase("totalClasses", totalClasses);
                updateAttendanceDisplay();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateAttendanceDisplay() {
        attendanceCount.setText(attendedClasses + "/" + totalClasses + " classes attended");
        attendanceDotsContainer.removeAllViews();

        for (int i = 0; i < totalClasses; i++) {
            View dot = createAttendanceDot(i < attendedClasses);
            final int classIndex = i;

            dot.setOnClickListener(v -> {
                if (classIndex < attendedClasses) {
                    attendedClasses--;
                } else if (classIndex == attendedClasses) {
                    attendedClasses++;
                }
                updateFieldInFirebase("attendedClasses", attendedClasses);
                updateAttendanceDisplay();
            });

            attendanceDotsContainer.addView(dot);
        }
    }

    private View createAttendanceDot(boolean attended) {
        View dot = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(40, 40);
        params.setMargins(8, 8, 8, 8);
        dot.setLayoutParams(params);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(attended ? Color.parseColor("#228B22") : Color.RED);
        dot.setBackground(shape);

        return dot;
    }

    private void showAddGradeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Grade");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Grade name (e.g., Midterm, Quiz 1)");
        layout.addView(nameInput);

        final EditText valueInput = new EditText(this);
        valueInput.setHint("Grade value");
        valueInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(valueInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String value = valueInput.getText().toString().trim();

            if (!name.isEmpty() && !value.isEmpty()) {
                addGradeToFirebase(name, value);
                addGradeToList(name, value);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addGradeToList(String name, String value) {
        LinearLayout gradeItem = new LinearLayout(this);
        gradeItem.setOrientation(LinearLayout.HORIZONTAL);
        gradeItem.setPadding(16, 12, 16, 12);
        gradeItem.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        gradeItem.setLayoutParams(params);

        TextView gradeText = new TextView(this);
        gradeText.setText(name + ": " + value);
        gradeText.setTextSize(16);
        gradeText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setTextSize(12);
        deleteButton.setPadding(2, 2, 2, 2);
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setStroke(2, Color.RED);
        border.setCornerRadius(16);
        border.setColor(Color.WHITE);

        deleteButton.setBackground(border);
        deleteButton.setTextColor(Color.RED);

        deleteButton.setOnClickListener(v -> {
            gradesList.removeView(gradeItem);
            deleteGradeFromFirebase(name);
        });

        gradeItem.addView(gradeText);
        gradeItem.addView(deleteButton);
        gradesList.addView(gradeItem);
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Note");

        final EditText input = new EditText(this);
        input.setHint("Enter your note");
        input.setMinLines(3);
        input.setPadding(50, 40, 50, 10);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String note = input.getText().toString().trim();
            if (!note.isEmpty()) {
                addNoteToFirebase(note);
                addNoteToList(note);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addNoteToList(String note) {
        LinearLayout noteItem = new LinearLayout(this);
        noteItem.setOrientation(LinearLayout.VERTICAL);
        noteItem.setPadding(16, 12, 16, 12);
        noteItem.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        noteItem.setLayoutParams(params);

        TextView noteText = new TextView(this);
        noteText.setText(note);
        noteText.setTextSize(16);

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setTextSize(12);
        deleteButton.setPadding(2, 2, 2, 2);
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setStroke(2, Color.RED);
        border.setCornerRadius(16);
        border.setColor(Color.WHITE);

        deleteButton.setBackground(border);
        deleteButton.setTextColor(Color.RED);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        deleteButton.setOnClickListener(v -> {
            notesList.removeView(noteItem);
            deleteNoteFromFirebase(note);
        });

        noteItem.addView(noteText);
        noteItem.addView(deleteButton);
        notesList.addView(noteItem);
    }

    private void updateFieldInFirebase(String field, Object value) {
        if (documentId == null) return;

        db.collection("default")
                .document(documentId)
                .update(field, value)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addGradeToFirebase(String name, String value) {
        if (documentId == null) return;

        Map<String, Object> grade = new HashMap<>();
        grade.put("name", name);
        grade.put("value", value);

        db.collection("default")
                .document(documentId)
                .get()
                .addOnSuccessListener(document -> {
                    List<Map<String, Object>> grades = (List<Map<String, Object>>) document.get("grades");
                    if (grades == null) {
                        grades = new ArrayList<>();
                    }
                    grades.add(grade);

                    db.collection("default")
                            .document(documentId)
                            .update("grades", grades)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this, "Grade added", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    private void deleteGradeFromFirebase(String gradeName) {
        if (documentId == null) return;

        db.collection("default")
                .document(documentId)
                .get()
                .addOnSuccessListener(document -> {
                    List<Map<String, Object>> grades = (List<Map<String, Object>>) document.get("grades");
                    if (grades != null) {
                        grades.removeIf(grade -> gradeName.equals(grade.get("name")));

                        db.collection("default")
                                .document(documentId)
                                .update("grades", grades)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "Grade deleted", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void addNoteToFirebase(String note) {
        if (documentId == null) return;

        db.collection("default")
                .document(documentId)
                .get()
                .addOnSuccessListener(document -> {
                    List<String> notes = (List<String>) document.get("notes");
                    if (notes == null) {
                        notes = new ArrayList<>();
                    }
                    notes.add(note);

                    db.collection("default")
                            .document(documentId)
                            .update("notes", notes)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    private void deleteNoteFromFirebase(String note) {
        if (documentId == null) return;

        db.collection("default")
                .document(documentId)
                .get()
                .addOnSuccessListener(document -> {
                    List<String> notes = (List<String>) document.get("notes");
                    if (notes != null) {
                        notes.remove(note);

                        db.collection("default")
                                .document(documentId)
                                .update("notes", notes)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private int getColorForType(String type) {
        if (type == null) {
            return Color.DKGRAY;
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

}
