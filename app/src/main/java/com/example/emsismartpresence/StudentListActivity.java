package com.example.emsismartpresence;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentListActivity extends AppCompatActivity implements NFCManager.NFCListener {
    private RecyclerView studentsRecyclerView;
    private FirebaseFirestore db;
    private String groupId;
    private NFCManager nfcManager;
    private SoundPool soundPool;
    private int beepSoundId;
    private StudentAttendanceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        // Initialisation Firestore
        db = FirebaseFirestore.getInstance();
        groupId = getIntent().getStringExtra("GROUP_ID");

        // Initialisation NFC
        nfcManager = new NFCManager(this);
        nfcManager.setNfcListener(this);

        // Initialisation SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        try {
            beepSoundId = soundPool.load(this, R.raw.beep, 1);
        } catch (Exception e) {
            Log.e("SoundPool", "Error loading sound", e);
        }

        // Configuration RecyclerView
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadStudents();
    }

    private void loadStudents() {
        db.collection("groups").document(groupId)
                .get()
                .addOnSuccessListener(groupDoc -> {
                    if (groupDoc.exists()) {
                        List<String> studentIds = (List<String>) groupDoc.get("studentIds");
                        if (studentIds != null && !studentIds.isEmpty()) {
                            fetchStudentsDetails(studentIds);
                        } else {
                            showEmptyState();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement du groupe", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error loading group", e);
                });
    }

    private void fetchStudentsDetails(List<String> studentIds) {
        db.collection("students")
                .whereIn(FieldPath.documentId(), studentIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Student> students = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Student student = doc.toObject(Student.class);
                        if (student != null) {
                            student.setId(doc.getId());
                            students.add(student);
                        }
                    }
                    setupStudentsAdapter(students);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement des étudiants", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error loading students", e);
                });
    }

    private void setupStudentsAdapter(List<Student> students) {
        adapter = new StudentAttendanceAdapter(students, (student, isPresent) -> {
            saveAttendance(student, isPresent, false);
        });
        studentsRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onNfcDetected(String nfcId) {
        db.collection("students")
                .whereEqualTo("nfcCardId", nfcId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        Student student = doc.toObject(Student.class);
                        if (student != null) {
                            student.setId(doc.getId());
                            saveAttendance(student, true, true);
                            playBeepSound();
                            updateStudentInList(student);
                        }
                    } else {
                        Toast.makeText(this, "Étudiant non trouvé", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur NFC", Toast.LENGTH_SHORT).show();
                    Log.e("NFC", "Error processing NFC", e);
                });
    }

    private void saveAttendance(Student student, boolean isPresent, boolean nfcVerified) {
        Map<String, Object> attendance = new HashMap<>();
        attendance.put("studentId", student.getId());
        attendance.put("status", isPresent ? "present" : "absent");
        attendance.put("nfcVerified", nfcVerified);
        attendance.put("timestamp", FieldValue.serverTimestamp());
        attendance.put("groupId", groupId);

        db.collection("attendances")
                .add(attendance)
                .addOnSuccessListener(ref -> {
                    String message = isPresent ? "Présence enregistrée" : "Absence enregistrée";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur d'enregistrement", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error saving attendance", e);
                });
    }

    private void playBeepSound() {
        if (soundPool != null && beepSoundId != 0) {
            soundPool.play(beepSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    private void updateStudentInList(Student updatedStudent) {
        if (adapter != null) {
            adapter.updateStudentStatus(updatedStudent.getId(), "present");
        }
    }

    private void showEmptyState() {
        Toast.makeText(this, "Aucun étudiant dans ce groupe", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcManager != null) {
            nfcManager.enableForegroundDispatch();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcManager != null) {
            nfcManager.disableForegroundDispatch();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (nfcManager != null) {
            nfcManager.handleIntent(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}