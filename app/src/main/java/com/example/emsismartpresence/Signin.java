package com.example.emsismartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import com.example.emsismartpresence.KeyboardUtils;
import com.google.firebase.auth.FirebaseAuth;

public class Signin extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        // Initialisation Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialisation des vues
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register); // Assurez-vous d'avoir cet ID dans votre XML

        // Gestion du clic sur le bouton de connexion
        btnLogin.setOnClickListener(v -> authenticateUser());

        // Gestion du clic sur le texte d'inscription
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(Signin.this, register.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            Log.d("Signin", "Register text clicked");
            // Animation de transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            // Lancement de l'activité d'inscription
            startActivity(new Intent(Signin.this, register.class));
        });

        // Gestion des bordures système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        View rootView = findViewById(android.R.id.content);
        KeyboardUtils.setupKeyboardAutoAdjust(rootView);
    }

    private void authenticateUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation des champs
        if (email.isEmpty()) {
            etEmail.setError("L'email est requis");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Le mot de passe est requis");
            etPassword.requestFocus();
            return;
        }

        // Authentification Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Connexion réussie
                        Toast.makeText(Signin.this, "Connexion réussie!", Toast.LENGTH_SHORT).show();

                        // Redirection vers l'activité principale
                        startActivity(new Intent(Signin.this, MainActivity.class));
                        finish();
                    } else {
                        // Échec de la connexion
                        Toast.makeText(Signin.this, "Échec de l'authentification: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}