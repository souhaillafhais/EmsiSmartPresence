package com.example.emsismartpresence;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



    public class Signin extends AppCompatActivity {

        private EditText etEmail, etPassword;

        private Button btnLogin;

        private final String validEmail = "user@example.com";

        private final String validPassword = "123456";


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_signin);


            etEmail = findViewById(R.id.et_email);
            etPassword = findViewById(R.id.et_password);
            btnLogin = findViewById(R.id.btn_login);

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    authenticateUser();
                }
            });
        }

        private void authenticateUser() {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.equals(validEmail) && password.equals(validPassword)) {
                Toast.makeText(this, "Authentification réussie !", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
            }
        }
    }


