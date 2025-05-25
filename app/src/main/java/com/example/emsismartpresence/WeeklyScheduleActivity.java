package com.example.emsismartpresence;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeeklyScheduleActivity extends AppCompatActivity {

    private static final String TAG = "WeeklyScheduleActivity";
    private static final int HOUR_HEIGHT = 80; // Height per hour in dp
    private static final int DAY_WIDTH = 140; // Width per day in dp
    private static final int START_HOUR = 8;
    private static final int START_MINUTE = 30; // Start at 8:30
    private static final int END_HOUR = 18;
    private static final int END_MINUTE = 15; // End at 18:15

    private LinearLayout timeAxisContainer;
    private LinearLayout dayHeadersContainer;
    private LinearLayout gridLinesContainer;
    private FrameLayout classesContainer;
    private ScrollView timeScrollView;
    private ScrollView scheduleScrollView;
    private TextView tvWeekTitle;
    private ImageButton btnPrevWeek, btnNextWeek;
    private ProgressBar progressBar;

    private Calendar currentWeek;
    private List<ScheduleItem> scheduleItems;
    private String[] dayNames = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
    private String[] fullDayNames = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};

    // Firebase
    private FirebaseFirestore db;
    private List<String> userGroups = List.of("G1", "G2","G3","G4","G5","G6"); // Add all groups you want to fetch

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_schedule);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupWeekNavigation();
        initializeCurrentWeek();
        setupScheduleGrid();
        syncScrollViews();

        // Load data from Firebase
        loadScheduleFromFirebase();
    }

    private void initializeViews() {
        timeAxisContainer = findViewById(R.id.timeAxisContainer);
        dayHeadersContainer = findViewById(R.id.dayHeadersContainer);
        gridLinesContainer = findViewById(R.id.gridLinesContainer);
        classesContainer = findViewById(R.id.classesContainer);
        timeScrollView = findViewById(R.id.timeScrollView);
        scheduleScrollView = findViewById(R.id.scheduleScrollView);
        tvWeekTitle = findViewById(R.id.tvWeekTitle);
        btnPrevWeek = findViewById(R.id.btnPrevWeek);
        btnNextWeek = findViewById(R.id.btnNextWeek);

        // Add progress bar programmatically if not in layout
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        ((ViewGroup) findViewById(android.R.id.content)).addView(progressBar);
    }

    private void setupWeekNavigation() {
        btnPrevWeek.setOnClickListener(v -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, -1);
            updateWeekDisplay();
            loadScheduleFromFirebase();
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, 1);
            updateWeekDisplay();
            loadScheduleFromFirebase();
        });
    }

    private void initializeCurrentWeek() {
        currentWeek = Calendar.getInstance();
        currentWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        updateWeekDisplay();
    }

    private void updateWeekDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.FRENCH);
        Calendar endWeek = (Calendar) currentWeek.clone();
        endWeek.add(Calendar.DAY_OF_WEEK, 5); // Friday

        String weekText = sdf.format(currentWeek.getTime()) + " - " + sdf.format(endWeek.getTime()) + " 2025";
        tvWeekTitle.setText(weekText);
    }

    private void loadScheduleFromFirebase() {
        showProgressBar(true);
        scheduleItems = new ArrayList<>();

        // Get week date range
        Calendar weekStart = (Calendar) currentWeek.clone();
        Calendar weekEnd = (Calendar) currentWeek.clone();
        weekEnd.add(Calendar.DAY_OF_WEEK, 6); // Saturday

        // Both collections use French date format "dd MMM yyyy"
        SimpleDateFormat frenchDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);

        // Load regular schedule for all groups
        loadRegularSchedule(weekStart, weekEnd, frenchDateFormat, () -> {
            // After regular schedule is loaded, load ratrapages
            loadRatrapages(weekStart, weekEnd, frenchDateFormat, () -> {
                showProgressBar(false);
                setupScheduleGrid();
            });
        });
    }

    private void loadRegularSchedule(Calendar weekStart, Calendar weekEnd, SimpleDateFormat dateFormat, Runnable onComplete) {
        // Query for all groups instead of filtering by one specific group
        db.collection("schedule")
                .whereIn("group", userGroups) // Fetch multiple groups
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Parse schedule date and check if it falls within current week
                                String dateStr = document.getString("date");
                                if (dateStr != null) {
                                    Calendar scheduleDate = Calendar.getInstance();
                                    scheduleDate.setTime(dateFormat.parse(dateStr));

                                    // Check if schedule falls within current week
                                    if (!scheduleDate.before(weekStart) && !scheduleDate.after(weekEnd)) {
                                        ScheduleItem item = createScheduleItemFromDocument(document, scheduleDate, false);
                                        if (item != null) {
                                            scheduleItems.add(item);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing schedule document: " + document.getId(), e);
                            }
                        }
                        Log.d(TAG, "Loaded " + scheduleItems.size() + " regular schedule items");
                        onComplete.run();
                    } else {
                        Log.e(TAG, "Error getting regular schedule", task.getException());
                        showProgressBar(false);
                        Toast.makeText(this, "Erreur lors du chargement de l'emploi du temps", Toast.LENGTH_SHORT).show();
                        onComplete.run();
                    }
                });
    }

    private void loadRatrapages(Calendar weekStart, Calendar weekEnd, SimpleDateFormat dateFormat, Runnable onComplete) {
        // Also update ratrapages to fetch multiple groups if needed
        db.collection("rattrapages")
                .whereIn("groupe", userGroups) // Assuming ratrapages also have multiple groups
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Parse rattrapage date
                                String dateStr = document.getString("date");
                                if (dateStr != null) {
                                    Calendar ratrapageDate = Calendar.getInstance();
                                    ratrapageDate.setTime(dateFormat.parse(dateStr));

                                    // Check if rattrapage falls within current week
                                    if (!ratrapageDate.before(weekStart) && !ratrapageDate.after(weekEnd)) {
                                        ScheduleItem item = createRatrapageItemFromDocument(document, ratrapageDate);
                                        if (item != null) {
                                            scheduleItems.add(item);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing rattrapage document: " + document.getId(), e);
                            }
                        }
                        Log.d(TAG, "Total schedule items (including ratrapages): " + scheduleItems.size());
                        onComplete.run();
                    } else {
                        Log.e(TAG, "Error getting ratrapages", task.getException());
                        onComplete.run();
                    }
                });
    }

    private ScheduleItem createScheduleItemFromDocument(QueryDocumentSnapshot document, Calendar scheduleDate, boolean isRattrapage) {
        try {
            String subject = document.getString("subject");
            String type = document.getString("type");
            String location = document.getString("location");
            String teacher = document.getString("teacher"); // This field might be missing in your data
            String startTime = document.getString("startTime");
            String endTime = document.getString("endTime");
            String group = document.getString("group"); // Get the group for display

            if (subject == null || startTime == null || endTime == null) {
                Log.w(TAG, "Missing required fields in document: " + document.getId());
                return null;
            }

            // Use the provided scheduleDate for day of week
            int dayOfWeek = scheduleDate.get(Calendar.DAY_OF_WEEK);

            // Parse time
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");

            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);

            return new ScheduleItem(
                    subject,
                    type != null ? type : "Cours",
                    dayOfWeek,
                    startHour, startMinute,
                    endHour, endMinute,
                    location != null ? location : "Salle",
                    teacher != null ? teacher : "Professeur", // Handle missing teacher field
                    isRattrapage,
                    group // Add group info
            );

        } catch (Exception e) {
            Log.e(TAG, "Error creating schedule item from document", e);
            return null;
        }
    }

    private ScheduleItem createRatrapageItemFromDocument(QueryDocumentSnapshot document, Calendar ratrapageDate) {
        try {
            String matiere = document.getString("matiere");
            String site = document.getString("site");
            String heureDeb = document.getString("heureDeb");
            String heureFin = document.getString("heureFin");
            String statut = document.getString("statut");
            String groupe = document.getString("groupe");

            if (matiere == null || heureDeb == null || heureFin == null) {
                Log.w(TAG, "Missing required fields in rattrapage document: " + document.getId());
                return null;
            }

            int dayOfWeek = ratrapageDate.get(Calendar.DAY_OF_WEEK);

            // Parse time
            String[] startParts = heureDeb.split(":");
            String[] endParts = heureFin.split(":");

            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);

            return new ScheduleItem(
                    matiere,
                    "Rattrapage", // Special type for ratrapages
                    dayOfWeek,
                    startHour, startMinute,
                    endHour, endMinute,
                    site != null ? site : "Site",
                    "Rattrapage", // Teacher field used for rattrapage status
                    true, // isRattrapage = true
                    groupe != null ? groupe : "Groupe" // Add group info
            );

        } catch (Exception e) {
            Log.e(TAG, "Error creating rattrapage item from document", e);
            return null;
        }
    }

    private void setupScheduleGrid() {
        createTimeAxis();
        createDayHeaders();
        createGridLines();
        displayClasses();
    }

    private void createTimeAxis() {
        timeAxisContainer.removeAllViews();

        // Create time labels from 8:30 to 18:15
        for (int hour = START_HOUR; hour <= END_HOUR; hour++) {
            // For the first hour, start at 8:30
            if (hour == START_HOUR) {
                TextView timeLabel = new TextView(this);
                timeLabel.setText(String.format(Locale.FRENCH, "%02d:30", hour));
                timeLabel.setTextSize(12);
                timeLabel.setTextColor(ContextCompat.getColor(this, R.color.gray));
                timeLabel.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(HOUR_HEIGHT / 2)); // Half height for 30 minutes
                timeLabel.setLayoutParams(params);
                timeAxisContainer.addView(timeLabel);
            }

            // Add regular hour labels
            if (hour < END_HOUR || (hour == END_HOUR && END_MINUTE > 0)) {
                TextView timeLabel = new TextView(this);
                timeLabel.setText(String.format(Locale.FRENCH, "%02d:00", hour + 1));
                timeLabel.setTextSize(12);
                timeLabel.setTextColor(ContextCompat.getColor(this, R.color.gray));
                timeLabel.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(HOUR_HEIGHT));
                timeLabel.setLayoutParams(params);
                timeAxisContainer.addView(timeLabel);
            }

            // For the last hour, add 18:15 if needed
            if (hour == END_HOUR && END_MINUTE > 0) {
                TextView timeLabel = new TextView(this);
                timeLabel.setText(String.format(Locale.FRENCH, "%02d:%02d", END_HOUR, END_MINUTE));
                timeLabel.setTextSize(12);
                timeLabel.setTextColor(ContextCompat.getColor(this, R.color.gray));
                timeLabel.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(HOUR_HEIGHT / 4)); // Quarter height for 15 minutes
                timeLabel.setLayoutParams(params);
                timeAxisContainer.addView(timeLabel);
            }
        }
    }

    private void createDayHeaders() {
        dayHeadersContainer.removeAllViews();

        Calendar dayCalendar = (Calendar) currentWeek.clone();
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.FRENCH);

        for (int i = 0; i < 6; i++) { // Monday to Saturday
            LinearLayout dayHeader = new LinearLayout(this);
            dayHeader.setOrientation(LinearLayout.VERTICAL);
            dayHeader.setGravity(Gravity.CENTER);
            dayHeader.setPadding(8, 8, 8, 8);

            TextView dayName = new TextView(this);
            dayName.setText(dayNames[i]);
            dayName.setTextSize(14);
            dayName.setTextColor(ContextCompat.getColor(this, R.color.darkGreen));
            dayName.setTypeface(null, Typeface.BOLD);

            TextView dayNumber = new TextView(this);
            dayNumber.setText(dayFormat.format(dayCalendar.getTime()));
            dayNumber.setTextSize(16);
            dayNumber.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            dayNumber.setTypeface(null, Typeface.BOLD);

            dayHeader.addView(dayName);
            dayHeader.addView(dayNumber);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(DAY_WIDTH), ViewGroup.LayoutParams.MATCH_PARENT);
            if (i < 5) {
                params.setMarginEnd(1);
            }
            dayHeader.setLayoutParams(params);

            // Highlight current day
            Calendar today = Calendar.getInstance();
            if (isSameDay(dayCalendar, today)) {
                GradientDrawable background = new GradientDrawable();
                background.setShape(GradientDrawable.RECTANGLE);
                background.setColor(ContextCompat.getColor(this, R.color.lightGreen));
                background.setCornerRadius(8);
                dayHeader.setBackground(background);
            }

            dayHeadersContainer.addView(dayHeader);

            if (i < 5) {
                View divider = new View(this);
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGray));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        1, ViewGroup.LayoutParams.MATCH_PARENT);
                divider.setLayoutParams(dividerParams);
                dayHeadersContainer.addView(divider);
            }

            dayCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void createGridLines() {
        gridLinesContainer.removeAllViews();

        // Calculate total hours including the 30-minute start and 15-minute end
        double totalHours = (END_HOUR - START_HOUR) + (END_MINUTE - START_MINUTE) / 60.0;
        int totalHeight = dpToPx((int)(HOUR_HEIGHT * totalHours));
        int totalWidth = dpToPx(DAY_WIDTH * 6 + 5);

        // Horizontal lines - adjusted for new time range
        double currentTime = START_HOUR + START_MINUTE / 60.0;
        while (currentTime <= END_HOUR + END_MINUTE / 60.0) {
            View line = new View(this);
            line.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGray));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    totalWidth, 1);
            params.topMargin = dpToPx((int)(HOUR_HEIGHT * (currentTime - START_HOUR - START_MINUTE / 60.0)));
            line.setLayoutParams(params);

            gridLinesContainer.addView(line);
            currentTime += 1.0; // Add lines every hour
        }

        // Vertical lines
        for (int day = 0; day <= 6; day++) {
            View line = new View(this);
            line.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGray));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    1, totalHeight);
            params.leftMargin = dpToPx((DAY_WIDTH + 1) * day);
            line.setLayoutParams(params);

            gridLinesContainer.addView(line);
        }

        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                totalWidth, totalHeight);
        gridLinesContainer.setLayoutParams(containerParams);
    }

    private void displayClasses() {
        classesContainer.removeAllViews();

        if (scheduleItems != null) {
            for (ScheduleItem item : scheduleItems) {
                CardView classCard = createClassCard(item);
                if (classCard != null) {
                    classesContainer.addView(classCard);
                }
            }
        }
    }

    private CardView createClassCard(ScheduleItem item) {
        CardView cardView = new CardView(this);
        cardView.setCardElevation(dpToPx(2));
        cardView.setRadius(dpToPx(8));
        cardView.setCardBackgroundColor(getClassColor(item.type, item.isRattrapage));

        // Calculate position and size
        int dayIndex = item.dayOfWeek - 2; // Monday = 2, so Monday = 0
        if (dayIndex < 0) dayIndex = 6; // Sunday = 0, so Sunday = 6
        if (dayIndex > 5) return null; // Skip Sunday

        int leftMargin = dpToPx((DAY_WIDTH + 1) * dayIndex + 4);

        // Calculate top margin based on the new time range (8:30 start)
        double startTimeInHours = item.startHour + item.startMinute / 60.0;
        double scheduleStartTime = START_HOUR + START_MINUTE / 60.0;
        int topMargin = dpToPx((int)(HOUR_HEIGHT * (startTimeInHours - scheduleStartTime)));

        int width = dpToPx(DAY_WIDTH - 8);
        int height = dpToPx((int)(HOUR_HEIGHT * item.getDurationInHours()));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.leftMargin = leftMargin;
        params.topMargin = topMargin;
        cardView.setLayoutParams(params);

        // Create card content
        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
        cardContent.setGravity(Gravity.CENTER_VERTICAL);

        TextView subjectText = new TextView(this);
        subjectText.setText(item.subject + (item.isRattrapage ? " (Rattrapage)" : ""));
        subjectText.setTextSize(12);
        subjectText.setTextColor(Color.WHITE);
        subjectText.setTypeface(null, Typeface.BOLD);
        subjectText.setMaxLines(2);

        TextView timeText = new TextView(this);
        timeText.setText(String.format(Locale.FRENCH, "%02d:%02d-%02d:%02d",
                item.startHour, item.startMinute,
                item.endHour, item.endMinute));
        timeText.setTextSize(10);
        timeText.setTextColor(Color.WHITE);

        TextView locationText = new TextView(this);
        locationText.setText(item.location);
        locationText.setTextSize(10);
        locationText.setTextColor(Color.WHITE);

        // Add group info to the card
        TextView groupText = new TextView(this);
        groupText.setText("Groupe: " + item.group);
        groupText.setTextSize(9);
        groupText.setTextColor(Color.WHITE);

        cardContent.addView(subjectText);
        cardContent.addView(timeText);
        cardContent.addView(locationText);
        cardContent.addView(groupText);

        cardView.addView(cardContent);
        cardView.setOnClickListener(v -> showClassDetails(item));

        return cardView;
    }

    private int getClassColor(String type, boolean isRattrapage) {
        if (isRattrapage) {
            return ContextCompat.getColor(this, R.color.colorAccent); // Orange/red for ratrapages
        }

        switch (type) {
            case "Cours":
                return ContextCompat.getColor(this, R.color.colorPrimary);
            case "TD":
                return ContextCompat.getColor(this, R.color.colorAccent);
            case "TP":
                return ContextCompat.getColor(this, R.color.darkGreen);
            default:
                return ContextCompat.getColor(this, R.color.gray);
        }
    }

    private void syncScrollViews() {
        timeScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            scheduleScrollView.scrollTo(scrollX, scrollY);
        });

        scheduleScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            timeScrollView.scrollTo(scrollX, scrollY);
        });
    }

    private void showClassDetails(ScheduleItem item) {
        String details;
        if (item.isRattrapage) {
            details = String.format(Locale.FRENCH,
                    "RATTRAPAGE\n%s\n%s\n%02d:%02d - %02d:%02d\nLieu: %s\nGroupe: %s",
                    item.subject,
                    fullDayNames[getDayIndex(item.dayOfWeek)],
                    item.startHour, item.startMinute,
                    item.endHour, item.endMinute,
                    item.location,
                    item.group);
        } else {
            details = String.format(Locale.FRENCH,
                    "%s\n%s\n%s\n%02d:%02d - %02d:%02d\nSalle: %s\nProfesseur: %s\nGroupe: %s",
                    item.subject,
                    item.type,
                    fullDayNames[getDayIndex(item.dayOfWeek)],
                    item.startHour, item.startMinute,
                    item.endHour, item.endMinute,
                    item.location,
                    item.teacher,
                    item.group);
        }

        Toast.makeText(this, details, Toast.LENGTH_LONG).show();
    }

    private int getDayIndex(int dayOfWeek) {
        // Convert Calendar day of week to array index
        switch (dayOfWeek) {
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: return 4;
            case Calendar.SATURDAY: return 5;
            default: return 0;
        }
    }

    private void showProgressBar(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // Updated data class to handle both schedule and ratrapages
    static class ScheduleItem {
        String subject;
        String type;
        int dayOfWeek;
        int startHour, startMinute;
        int endHour, endMinute;
        String location;
        String teacher;
        boolean isRattrapage;
        String group; // Added group field

        ScheduleItem(String subject, String type, int dayOfWeek,
                     int startHour, int startMinute, int endHour, int endMinute,
                     String location, String teacher, boolean isRattrapage, String group) {
            this.subject = subject;
            this.type = type;
            this.dayOfWeek = dayOfWeek;
            this.startHour = startHour;
            this.startMinute = startMinute;
            this.endHour = endHour;
            this.endMinute = endMinute;
            this.location = location;
            this.teacher = teacher;
            this.isRattrapage = isRattrapage;
            this.group = group;
        }

        double getDurationInHours() {
            int startMinutes = startHour * 60 + startMinute;
            int endMinutes = endHour * 60 + endMinute;
            return (endMinutes - startMinutes) / 60.0;
        }
    }
}