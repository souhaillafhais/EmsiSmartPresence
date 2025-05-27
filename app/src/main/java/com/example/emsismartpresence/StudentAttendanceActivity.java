package com.example.emsismartpresence;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentAttendanceActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {
    private RecyclerView studentsRecyclerView;
    private StudentAttendanceAdapter adapter;
    private FirebaseFirestore db;
    private String groupId;
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);

        db = FirebaseFirestore.getInstance();
        groupId = getIntent().getStringExtra("groupId");

        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC non supporté sur cet appareil", Toast.LENGTH_SHORT).show();
        }

        fetchStudents();
        addTestStudent();
    }

    private void fetchStudents() {
        db.collection("students")
                .whereEqualTo("groupId", groupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Student> students = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Log.d("FIREBASE_DEBUG", "Document trouvé: " + doc.getId() + " => " + doc.getData());

                            Student student = doc.toObject(Student.class);
                            if (student != null) {
                                student.setId(doc.getId());
                                students.add(student);
                            }
                        }

                        if (students.isEmpty()) {
                            Toast.makeText(this,
                                    "Aucun étudiant trouvé. Un étudiant test a été ajouté.",
                                    Toast.LENGTH_LONG).show();
                            addTestStudent(); // Ajoute un étudiant si la liste est vide
                        } else {
                            adapter = new StudentAttendanceAdapter(students, this::markAttendance);
                            studentsRecyclerView.setAdapter(adapter);
                        }
                    } else {
                        Log.e("FIREBASE", "Erreur de requête", task.getException());
                    }
                });
    }

    private void addTestStudent() {
        // Vérifiez d'abord si l'étudiant existe déjà
        db.collection("students")
                .whereEqualTo("groupId", groupId)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Des étudiants existent déjà
                        fetchStudents();
                    } else {
                        // Aucun étudiant trouvé, ajoutez-en un
                        Map<String, Object> student = new HashMap<>();
                        student.put("name", "Étudiant Test");
                        student.put("groupId", groupId);
                        student.put("nfcTagId", "TEST123");

                        db.collection("students")
                                .add(student)
                                .addOnSuccessListener(docRef -> {
                                    Log.d("FIREBASE", "Étudiant ajouté avec ID: " + docRef.getId());
                                    // Attendre 1 seconde avant de rafraîchir
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        fetchStudents();
                                    }, 1000);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FIREBASE", "Erreur d'ajout", e);
                                });
                    }
                });
    }
    private void markAttendance(String studentId, boolean present) {
        Attendance attendance = new Attendance(
                groupId,
                studentId,
                present,
                "teacher",
                new Date()
        );

        db.collection("attendance").add(attendance)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Présence enregistrée", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }

        char[] buffer = new char[2];
        for (byte b : src) {
            buffer[0] = Character.forDigit((b >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(b & 0x0F, 16);
            stringBuilder.append(buffer);
        }

        return stringBuilder.toString();
    }

    // NFC Callbacks

    @Override
    public void onTagDiscovered(Tag tag) {
        String tagId = bytesToHexString(tag.getId());
        runOnUiThread(() -> Toast.makeText(this, "Tag NFC détecté: " + tagId, Toast.LENGTH_SHORT).show());

        db.collection("students")
                .whereEqualTo("nfcTagId", tagId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Student student = doc.toObject(Student.class);
                        student.setId(doc.getId());

                        // Marquer automatiquement présent
                        markAttendance(student.getId(), true);
                        runOnUiThread(() -> {
                            Toast.makeText(this, student.getName() + " marqué présent via NFC",
                                    Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(this, "Étudiant non trouvé pour ce tag NFC",
                                        Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Erreur NFC: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, this,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }
}