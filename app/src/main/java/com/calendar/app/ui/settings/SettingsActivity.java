package com.calendar.app.ui.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.calendar.app.R;
import com.calendar.app.data.CalendarRepository;
import com.calendar.app.utils.Prefs;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private Prefs prefs;
    private CalendarRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new Prefs(this);
        repository = new CalendarRepository(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Show week numbers toggle
        SwitchMaterial switchWeekNumbers = findViewById(R.id.switchWeekNumbers);
        switchWeekNumbers.setChecked(prefs.showWeekNumbers());
        switchWeekNumbers.setOnCheckedChangeListener((btn, checked) ->
                prefs.setShowWeekNumbers(checked));

        // Start week on
        android.widget.TextView tvStartWeek = findViewById(R.id.tvStartWeekValue);
        tvStartWeek.setText(prefs.startWeekOnSunday() ? "Sunday" : "Monday");
        findViewById(R.id.rowStartWeek).setOnClickListener(v -> {
            String[] options = {"Sunday", "Monday"};
            new AlertDialog.Builder(this)
                    .setTitle("Start Week On")
                    .setItems(options, (dialog, which) -> {
                        prefs.setStartWeekOnSunday(which == 0);
                        tvStartWeek.setText(options[which]);
                    })
                    .show();
        });

        // Delete overdue events
        findViewById(R.id.rowDeleteOverdue).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Overdue Events")
                    .setMessage("This will permanently delete all events that ended before today. Continue?")
                    .setPositiveButton("Delete", (d, w) -> deleteOverdueEvents())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void deleteOverdueEvents() {
        executor.execute(() -> {
            long now = Calendar.getInstance().getTimeInMillis();
            // Load events from 5 years ago to now
            Calendar past = Calendar.getInstance();
            past.add(Calendar.YEAR, -5);
            java.util.List<com.calendar.app.data.CalendarEvent> old =
                    repository.getEvents(past.getTimeInMillis(), now);

            int deleted = 0;
            for (com.calendar.app.data.CalendarEvent e : old) {
                if (e.getEndMillis() < now) {
                    if (repository.deleteEvent(e.getId())) deleted++;
                }
            }

            int finalDeleted = deleted;
            runOnUiThread(() ->
                    Toast.makeText(this, "Deleted " + finalDeleted + " overdue events",
                            Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
