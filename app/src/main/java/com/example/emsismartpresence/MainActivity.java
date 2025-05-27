package com.example.emsismartpresence;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcomeHeader, tvDepartment, tvEmail, tvCampus;
    private CardView presenceCard, mapsCard, learnCard, contributeCard, assistantCard, vacancesCard, documentCard;
    private ImageView logoutIcon;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentGroupId = ""; // Variable pour stocker l'ID du groupe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisations Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Liaison des vues
        tvWelcomeHeader = findViewById(R.id.tvWelcomeHeader);
        tvDepartment = findViewById(R.id.tvDepartment);
        tvEmail = findViewById(R.id.tvEmail);
        tvCampus = findViewById(R.id.tvCampus);
        learnCard = findViewById(R.id.learnCard);
        contributeCard = findViewById(R.id.contributeCard);
        assistantCard = findViewById(R.id.assistantCard);
        vacancesCard = findViewById(R.id.vacancesCard);
        documentCard = findViewById(R.id.practiceCard);
        logoutIcon = findViewById(R.id.logoutIcon);
        mapsCard = findViewById(R.id.map);
        presenceCard = findViewById(R.id.presencesCard);

        // Configuration des listeners
        setupLogoutButton();
        setupCardListeners();


    }

    private void setupCardListeners() {
        documentCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DocumentActivity.class);
            startActivity(intent);
        });

        vacancesCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Vacances.class);
            startActivity(intent);
        });

        contributeCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WeeklyScheduleActivity.class);
            startActivity(intent);
        });

        assistantCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AssistantVirtuelActivity.class);
            startActivity(intent);
        });

        learnCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RattrapageActivity.class);
            startActivity(intent);
        });

        mapsCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, maps.class);
            startActivity(intent);
        });
        presenceCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GroupListActivity.class);
            startActivity(intent);
        });

    }

    // ... [Le reste de vos méthodes existantes (setupLogoutButton, showLogoutConfirmationDialog, etc.)] ...
    private void setupLogoutButton() {
        logoutIcon.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        auth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, Signin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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