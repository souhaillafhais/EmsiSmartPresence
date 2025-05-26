package com.example.emsismartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private AutoCompleteTextView departmentDropdown, campusDropdown;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.et_fullname);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        departmentDropdown = findViewById(R.id.department_dropdown);
        campusDropdown = findViewById(R.id.campus_dropdown);

        // Setup dropdown options
        String[] departments = {"Informatique", "Génie Civil", "Génie Électrique", "Génie Industriel"};
        String[] campuses = {"Casablanca", "Rabat", "Marrakech", "Tanger"};

        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, departments);
        departmentDropdown.setAdapter(departmentAdapter);

        ArrayAdapter<String> campusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, campuses);
        campusDropdown.setAdapter(campusAdapter);

        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String department = departmentDropdown.getText().toString().trim();
        String campus = campusDropdown.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Nom complet requis");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email requis");
            etEmail.requestFocus();
            return;
        }

        if (!email.endsWith("@emsi-edu.ma")) {
            etEmail.setError("Email doit se terminer par @emsi-edu.ma");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Mot de passe requis");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Le mot de passe doit contenir au moins 6 caractères");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Les mots de passe ne correspondent pas");
            etConfirmPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    userId = user.getUid();
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("fullName", fullName);
                    userData.put("email", email);
                    userData.put("department", department);
                    userData.put("campus", campus);

                    db.collection("users").document(userId).set(userData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(register.this, "Inscription réussie!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(register.this, Signin.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(register.this, "Erreur Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(register.this, "Erreur d'inscription: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}