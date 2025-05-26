package com.example.emsismartpresence;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Vacances extends AppCompatActivity {

    private TableLayout tableVacances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vacances);

        tableVacances = findViewById(R.id.tableVacances);

        List<VacancesModel> vacancesList = VacancesModel.getListeVacances();

        for (int i = 0; i < vacancesList.size(); i++) {
            addRow(vacancesList.get(i), i);
        }
    }

    private void addRow(VacancesModel vacances, int index) {
        TableRow row = new TableRow(this);

        // Alternate row colors for better readability
        if (index % 2 == 0) {
            row.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        } else {
            row.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGray));
        }

        // Row styling
        row.setPadding(16, 20, 16, 20);

        // Add a subtle border effect
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = 2;
        row.setLayoutParams(rowParams);

        // Create text views with improved styling
        TextView nom = createEventTextView(vacances.getNom());
        TextView date = createDateTextView(vacances.getDate());
        TextView jours = createDaysTextView(vacances.getJours());

        row.addView(nom);
        row.addView(date);
        row.addView(jours);

        tableVacances.addView(row);
    }

    private TextView createEventTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setLayoutParams(new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.5f
        ));
        tv.setTextColor(ContextCompat.getColor(this, R.color.black));
        tv.setPadding(0, 8, 12, 8);
        tv.setTextSize(14);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        tv.setLineSpacing(4, 1.1f);
        return tv;
    }

    private TextView createDateTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setLayoutParams(new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));
        tv.setTextColor(ContextCompat.getColor(this, R.color.darkGreen));
        tv.setPadding(8, 8, 8, 8);
        tv.setTextSize(12);
        tv.setGravity(Gravity.CENTER);
        tv.setLineSpacing(3, 1.1f);
        return tv;
    }

    private TextView createDaysTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setLayoutParams(new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0.5f
        ));

        // Style the days count with accent color
        tv.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        tv.setPadding(8, 8, 8, 8);
        tv.setTextSize(14);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setGravity(Gravity.CENTER);

        // Add a subtle background for days
        tv.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGreen));
        tv.setBackground(createRoundedBackground());

        return tv;
    }

    private android.graphics.drawable.GradientDrawable createRoundedBackground() {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(20f);
        drawable.setColor(ContextCompat.getColor(this, R.color.lightGreen));
        drawable.setStroke(2, ContextCompat.getColor(this, R.color.colorAccent));
        return drawable;
    }

    // Classe modèle interne
    public static class VacancesModel {
        private String nom;
        private String date;
        private String jours;

        public VacancesModel(String nom, String date, String jours) {
            this.nom = nom;
            this.date = date;
            this.jours = jours;
        }

        public String getNom() {
            return nom;
        }

        public String getDate() {
            return date;
        }

        public String getJours() {
            return jours;
        }

        public static List<VacancesModel> getListeVacances() {
            List<VacancesModel> liste = new ArrayList<>();
            liste.add(new VacancesModel("Aid Al Maoulid Anabaoui", "Lundi 12 et Mardi 13 Rabiî Al Awwal 1446", "2"));
            liste.add(new VacancesModel("La Marche Verte", "Mercredi 06 novembre 2024", "1"));
            liste.add(new VacancesModel("Fête de l'indépendance", "Lundi 18 novembre 2024", "1"));
            liste.add(new VacancesModel("Le Nouvel An", "Mercredi 01 janvier 2025", "1"));
            liste.add(new VacancesModel("Manifeste de l'indépendance", "Samedi 11 janvier 2025", "1"));
            liste.add(new VacancesModel("Nouvel An Amazigh", "Mardi 14 janvier 2025", "1"));
            liste.add(new VacancesModel("Fin du 1er Semestre", "Du Lundi 26 janvier au Dimanche 2 février 2025", "8"));
            liste.add(new VacancesModel("Aid Al Fitr", "Du Jeudi 29 ramadan 1446 au Dimanche 2 chawwal 1446", "3 ou 4"));
            liste.add(new VacancesModel("Fête du Travail", "Jeudi 01 mai 2025", "1"));
            liste.add(new VacancesModel("Printemps", "Du Dimanche 04 mai au Dimanche 11 mai 2025", "8"));
            liste.add(new VacancesModel("Aid Al Adha", "Du Jeudi 09 au Samedi 11 Dhu Al-Hijja 1446", "3"));
            liste.add(new VacancesModel("Nouvel An Hégire", "Lundi 1er Muharram 1447", "1"));
            return liste;
        }
    }
}