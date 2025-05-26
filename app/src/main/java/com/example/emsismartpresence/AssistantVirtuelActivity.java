package com.example.emsismartpresence;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
    private LinearLayout messagesContainer;
    private ScrollView chatScrollView;
    private LinearLayout typingIndicator;
    private LinearLayout quickActionsContainer;

    private final String API_KEY = "AIzaSyAzt_fyR8HzBeCy3tBqePuRU4qSpYIbbr0";
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant_virtuel);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        messagesContainer = findViewById(R.id.messagesContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        typingIndicator = findViewById(R.id.typingIndicator);
        quickActionsContainer = findViewById(R.id.quickActionsContainer);
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> handleSendMessage());

        // Quick action buttons
        findViewById(R.id.quickAction1).setOnClickListener(v -> {
            inputMessage.setText("Comment puis-je améliorer mes études ?");
            handleSendMessage();
        });

        findViewById(R.id.quickAction2).setOnClickListener(v -> {
            inputMessage.setText("Donnez-moi des conseils pour être plus productif");
            handleSendMessage();
        });

        findViewById(R.id.quickAction3).setOnClickListener(v -> {
            inputMessage.setText("J'ai des questions sur l'organisation de mon temps");
            handleSendMessage();
        });
    }

    private void handleSendMessage() {
        String message = inputMessage.getText().toString().trim();

        if (!message.isEmpty()) {
            // Hide quick actions after first message
            quickActionsContainer.setVisibility(View.GONE);

            // Add user message to chat
            addUserMessage(message);

            // Clear input and disable send button
            inputMessage.setText("");
            sendButton.setEnabled(false);

            // Show typing indicator
            showTypingIndicator();

            // Send message to API
            sendMessageToGemini(message);
        } else {
            Toast.makeText(this, "Veuillez entrer un message", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserMessage(String message) {
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setGravity(android.view.Gravity.END);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(dpToPx(64), 0, 0, dpToPx(16));
        messageLayout.setLayoutParams(layoutParams);

        // User message bubble
        CardView messageCard = new CardView(this);
        CardView.LayoutParams cardParams = new CardView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        messageCard.setLayoutParams(cardParams);
        messageCard.setRadius(dpToPx(20));
        messageCard.setCardElevation(dpToPx(4));
        messageCard.setCardBackgroundColor(getColor(R.color.colorPrimary));

        TextView messageText = new TextView(this);
        messageText.setText(message);
        messageText.setTextColor(getColor(R.color.white));
        messageText.setTextSize(16);
        messageText.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
        messageText.setLineSpacing(dpToPx(4), 1.2f);

        messageCard.addView(messageText);
        messageLayout.addView(messageCard);
        messagesContainer.addView(messageLayout);

        scrollToBottom();
    }

    private void addBotMessage(String message) {
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setGravity(android.view.Gravity.START);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, dpToPx(64), dpToPx(16));
        messageLayout.setLayoutParams(layoutParams);

        // Bot avatar
        CardView avatarCard = new CardView(this);
        CardView.LayoutParams avatarParams = new CardView.LayoutParams(dpToPx(32), dpToPx(32));
        avatarParams.setMargins(0, dpToPx(4), dpToPx(8), 0);
        avatarCard.setLayoutParams(avatarParams);
        avatarCard.setRadius(dpToPx(16));
        avatarCard.setCardElevation(dpToPx(2));
        avatarCard.setCardBackgroundColor(getColor(R.color.colorPrimary));

        TextView avatarText = new TextView(this);
        avatarText.setText("🤖");
        avatarText.setTextSize(16);
        avatarText.setGravity(android.view.Gravity.CENTER);
        avatarCard.addView(avatarText);

        // Bot message bubble
        CardView messageCard = new CardView(this);
        CardView.LayoutParams cardParams = new CardView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        messageCard.setLayoutParams(cardParams);
        messageCard.setRadius(dpToPx(20));
        messageCard.setCardElevation(dpToPx(4));
        messageCard.setCardBackgroundColor(getColor(R.color.white));

        TextView messageText = new TextView(this);
        messageText.setText(message);
        messageText.setTextColor(getColor(R.color.black));
        messageText.setTextSize(16);
        messageText.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
        messageText.setLineSpacing(dpToPx(4), 1.2f);

        messageCard.addView(messageText);
        messageLayout.addView(avatarCard);
        messageLayout.addView(messageCard);
        messagesContainer.addView(messageLayout);

        scrollToBottom();
    }

    private void showTypingIndicator() {
        typingIndicator.setVisibility(View.VISIBLE);
        scrollToBottom();
    }

    private void hideTypingIndicator() {
        typingIndicator.setVisibility(View.GONE);
    }

    private void scrollToBottom() {
        new Handler(Looper.getMainLooper()).post(() -> {
            chatScrollView.fullScroll(View.FOCUS_DOWN);
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void sendMessageToGemini(String message) {
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
                        hideTypingIndicator();
                        addBotMessage(reply);
                        sendButton.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    hideTypingIndicator();
                    addBotMessage("Désolé, une erreur s'est produite. Veuillez réessayer.");
                    sendButton.setEnabled(true);
                    Toast.makeText(AssistantVirtuelActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace();
            }
        }).start();
    }
}