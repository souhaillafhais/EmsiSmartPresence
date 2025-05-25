package com.example.emsismartpresence;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AssistantVirtuelActivity extends AppCompatActivity {

    private EditText inputMessage;
    private Button sendButton;
    private TextView responseText;

    private final String API_KEY = "AIzaSyAzt_fyR8HzBeCy3tBqePuRU4qSpYIbbr0";
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant_virtuel);

        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        responseText = findViewById(R.id.responseText);

        sendButton.setOnClickListener(v -> {
            sendButton.setEnabled(false);
            String message = inputMessage.getText().toString().trim();

            if (!message.isEmpty()) {
                sendMessageToGemini(message);
                inputMessage.setText("");
            } else {
                Toast.makeText(this, "Veuillez entrer un message", Toast.LENGTH_SHORT).show();
                sendButton.setEnabled(true);
            }
        });
    }

    private void sendMessageToGemini(String message) {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Envoi en cours...");
        progress.setCancelable(false);
        progress.show();

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.get("application/json; charset=utf-8");

                JSONObject part = new JSONObject();
                part.put("text", message);

                JSONArray partsArray = new JSONArray();
                partsArray.put(part);

                JSONObject content = new JSONObject();
                content.put("parts", partsArray);

                JSONArray contentsArray = new JSONArray();
                contentsArray.put(content);

                JSONObject finalBody = new JSONObject();
                finalBody.put("contents", contentsArray);

                RequestBody body = RequestBody.create(finalBody.toString(), JSON);
                Request request = new Request.Builder()
                        .url(GEMINI_URL)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Code: " + response.code());
                    }

                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    String reply = jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    runOnUiThread(() -> {
                        responseText.setText(reply);
                        progress.dismiss();
                        sendButton.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.dismiss();
                    responseText.setText("Erreur: " + e.getMessage());
                    sendButton.setEnabled(true);
                    Toast.makeText(AssistantVirtuelActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace();
            }
        }).start();
    }
}