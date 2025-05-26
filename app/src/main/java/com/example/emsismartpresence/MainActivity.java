package com.example.emsismartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcomeHeader, tvDepartment, tvEmail, tvCampus;
    private CardView learnCard, assistantCard, planingCard, documentCard, mapCard, vacancesCard;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Link UI elements
        tvWelcomeHeader = findViewById(R.id.tvWelcomeHeader);
        tvDepartment = findViewById(R.id.tvDepartment);
        tvEmail = findViewById(R.id.tvEmail);
        tvCampus = findViewById(R.id.tvCampus);
        learnCard = findViewById(R.id.learnCard);
        assistantCard = findViewById(R.id.assistantCard);
        planingCard = findViewById(R.id.contributeCard);
        documentCard = findViewById(R.id.practiceCard);
        mapCard = findViewById(R.id.interestsCard);

        vacancesCard = findViewById(R.id.vacancesCard);

        vacancesCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Vacances.class);
            startActivity(intent);
        });

        // Set click listeners
        learnCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RattrapageActivity.class);
            startActivity(intent);
        });

        assistantCard.setOnClickListener(v -> {
            launchAssistant();
        });

        planingCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WeeklyScheduleActivity.class);
            startActivity(intent);
        });

        documentCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DocumentActivity.class);  // Vérifie que le nom est bien DocumentActivity
            startActivity(intent);
        });

        mapCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });


        // Get current user
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            fetchUserData(userId);
        } else {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, Signin.class));
            finish();
        }
    }

    private void launchAssistant() {
        Intent intent = new Intent(MainActivity.this, AssistantVirtuelActivity.class);
        startActivity(intent);
    }

    private void fetchUserData(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String department = documentSnapshot.getString("department");
                        String email = documentSnapshot.getString("email");
                        String campus = documentSnapshot.getString("campus");

                        tvWelcomeHeader.setText("Welcome, Prof. " + (fullName != null ? fullName : "User"));
                        tvDepartment.setText("Département: " + (department != null ? department : "N/A"));
                        tvEmail.setText("Email: " + (email != null ? email : "N/A"));
                        tvCampus.setText("Campus: " + (campus != null ? campus : "N/A"));
                    } else {
                        Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
