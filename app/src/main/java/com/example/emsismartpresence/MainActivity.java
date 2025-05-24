package com.example.emsismartpresence;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.view.View;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcomeHeader, tvDepartment, tvEmail, tvCampus;
    private CardView learnCard, assistantCard, planingCard, documentCard, mapCard;
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

        // Set click listeners
        learnCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RattrapageActivity.class);
            startActivity(intent);
        });

        assistantCard.setOnClickListener(v -> {
            // Launch assistant activity or dialog
            launchAssistant();
        });

       /* planingCard.setOnClickListener(v -> {
            // Launch planning activity
            Intent intent = new Intent(MainActivity.this, PlanningActivity.class);
            startActivity(intent);
        });

        documentCard.setOnClickListener(v -> {
            // Launch documents activity
            Intent intent = new Intent(MainActivity.this, DocumentsActivity.class);
            startActivity(intent);
        });

        mapCard.setOnClickListener(v -> {
            // Launch campus map activity
            Intent intent = new Intent(MainActivity.this, CampusMapActivity.class);
            startActivity(intent);
        });*/

        // Get current user
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            fetchUserData(userId);
        } else {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_LONG).show();
            // Redirect to login screen if needed
            startActivity(new Intent(this, Signin.class));
            finish();
        }
    }

    private void launchAssistant() {
        // You can implement this in different ways:

        // Option 1: Launch a chat activity
        Intent intent = new Intent(MainActivity.this, AssistantVirtuelActivity.class);
        startActivity(intent);

        // Option 2: Show a dialog
        /*
        new AlertDialog.Builder(this)
            .setTitle("Virtual Assistant")
            .setMessage("How can I help you today?")
            .setPositiveButton("Ask Question", (dialog, which) -> {
                // Launch question input
                showQuestionInputDialog();
            })
            .setNegativeButton("Cancel", null)
            .show();
        */
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