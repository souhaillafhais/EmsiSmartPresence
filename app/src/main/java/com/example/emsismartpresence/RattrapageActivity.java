package com.example.emsismartpresence;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RattrapageActivity extends AppCompatActivity {

    private LinearLayout layoutCards;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView tvTotalCount, tvWeekCount, tvSitesCount;

    private String currentProfesseurId;
    private TextView tvHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rattrapage);

        // Initialize views
        layoutCards = findViewById(R.id.layoutCards);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvWeekCount = findViewById(R.id.tvWeekCount);
        tvSitesCount = findViewById(R.id.tvSitesCount);
        tvHeader = findViewById(R.id.tvHeader);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            currentProfesseurId = user.getUid();
            loadRattrapagesFromFirestore();
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadRattrapagesFromFirestore() {
        db.collection("rattrapages")
                .whereEqualTo("professeurId", currentProfesseurId)
                .whereEqualTo("statut", "À venir")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            List<Rattrapage> rattrapages = new ArrayList<>();
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                String matiere = doc.getString("matiere");
                                String groupe = doc.getString("groupe");
                                String date = doc.getString("date");
                                String heureDeb = doc.getString("heureDeb");
                                String heureFin = doc.getString("heureFin");
                                String site = doc.getString("site");
                                String statut = doc.getString("statut");

                                rattrapages.add(new Rattrapage(matiere, groupe, date, heureDeb + " - " + heureFin, site, statut, doc.getId()));
                            }
                            updateAnalytics(rattrapages);
                            displayRattrapages(rattrapages);
                        } else {
                            updateAnalytics(new ArrayList<>());
                            Toast.makeText(this, "Aucun rattrapage à venir.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Erreur de chargement.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateAnalytics(List<Rattrapage> rattrapages) {
        // Total count
        tvTotalCount.setText(String.valueOf(rattrapages.size()));

        // Count for this week
        int weekCount = 0;
        Calendar calendar = Calendar.getInstance();
        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        int currentYear = calendar.get(Calendar.YEAR);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);

        for (Rattrapage r : rattrapages) {
            try {
                Date date = sdf.parse(r.date);
                if (date != null) {
                    calendar.setTime(date);
                    int rattrapageWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                    int rattrapageYear = calendar.get(Calendar.YEAR);

                    if (rattrapageWeek == currentWeek && rattrapageYear == currentYear) {
                        weekCount++;
                    }
                }
            } catch (Exception e) {
                // Handle parsing error
            }
        }
        tvWeekCount.setText(String.valueOf(weekCount));

        // Count unique sites
        Set<String> uniqueSites = new HashSet<>();
        for (Rattrapage r : rattrapages) {
            if (r.site != null && !r.site.trim().isEmpty()) {
                // Extract just the site name (before comma if present)
                String siteName = r.site.split(",")[0].trim();
                uniqueSites.add(siteName);
            }
        }
        tvSitesCount.setText(String.valueOf(uniqueSites.size()));
    }

    private void displayRattrapages(List<Rattrapage> rattrapages) {
        layoutCards.removeAllViews();
        for (Rattrapage r : rattrapages) {
            layoutCards.addView(createEnhancedCardView(r));
        }
    }

    private View createEnhancedCardView(Rattrapage r) {
        // Create CardView
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(4);
        cardView.setRadius(12);
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));

        // Main container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(20, 20, 20, 20);

        // Header with matière
        TextView matiereHeader = new TextView(this);
        matiereHeader.setText("📚 " + r.matiere);
        matiereHeader.setTextSize(18);
        matiereHeader.setTextColor(ContextCompat.getColor(this, R.color.darkGreen));
        matiereHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        headerParams.setMargins(0, 0, 0, 12);
        matiereHeader.setLayoutParams(headerParams);
        mainContainer.addView(matiereHeader);

        // Info container
        LinearLayout infoContainer = new LinearLayout(this);
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        infoContainer.setPadding(12, 12, 12, 12);
        infoContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGray));

        // Round corners for info container
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(8);
        shape.setColor(ContextCompat.getColor(this, R.color.lightGray));
        infoContainer.setBackground(shape);

        infoContainer.addView(createInfoTextView("👥 Groupe", r.groupe));
        infoContainer.addView(createInfoTextView("📅 Date", r.date));
        infoContainer.addView(createInfoTextView("🕒 Heure", r.heure));
        infoContainer.addView(createInfoTextView("📍 Site", r.site));
        infoContainer.addView(createInfoTextView("✅ Statut", r.statut));

        mainContainer.addView(infoContainer);

        // Button container
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonContainerParams.setMargins(0, 16, 0, 0);
        buttonContainer.setLayoutParams(buttonContainerParams);

        // Done button
        Button btnDone = new Button(this);
        btnDone.setText("✅ Marquer comme effectué");
        btnDone.setTextColor(ContextCompat.getColor(this, R.color.white));
        btnDone.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        // Round corners for button
        GradientDrawable buttonShape = new GradientDrawable();
        buttonShape.setShape(GradientDrawable.RECTANGLE);
        buttonShape.setCornerRadius(8);
        buttonShape.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        btnDone.setBackground(buttonShape);

        btnDone.setPadding(16, 12, 16, 12);
        btnDone.setOnClickListener(v -> markAsDone(r));

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnDone.setLayoutParams(buttonParams);

        buttonContainer.addView(btnDone);
        mainContainer.addView(buttonContainer);

        cardView.addView(mainContainer);
        return cardView;
    }

    private LinearLayout createInfoTextView(String label, String value) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 4, 0, 4);
        container.setLayoutParams(params);

        TextView labelTv = new TextView(this);
        labelTv.setText(label + " :");
        labelTv.setTextSize(14);
        labelTv.setTextColor(ContextCompat.getColor(this, R.color.gray));
        labelTv.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f);
        labelTv.setLayoutParams(labelParams);

        TextView valueTv = new TextView(this);
        valueTv.setText(value);
        valueTv.setTextSize(14);
        valueTv.setTextColor(ContextCompat.getColor(this, R.color.darkGreen));
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f);
        valueTv.setLayoutParams(valueParams);

        container.addView(labelTv);
        container.addView(valueTv);

        return container;
    }

    private void markAsDone(Rattrapage r) {
        db.collection("rattrapages")
                .document(r.docId)
                .update("statut", "Effectué")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Rattrapage marqué comme effectué", Toast.LENGTH_SHORT).show();
                    loadRattrapagesFromFirestore();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show());
    }

    static class Rattrapage {
        String matiere, groupe, date, heure, site, statut, docId;

        Rattrapage(String matiere, String groupe, String date, String heure, String site, String statut, String docId) {
            this.matiere = matiere;
            this.groupe = groupe;
            this.date = date;
            this.heure = heure;
            this.site = site;
            this.statut = statut;
            this.docId = docId;
        }
    }
}