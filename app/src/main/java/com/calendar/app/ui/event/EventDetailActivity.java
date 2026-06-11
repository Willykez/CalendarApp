package com.calendar.app.ui.event;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.calendar.app.R;
import com.calendar.app.data.CalendarRepository;
import com.calendar.app.utils.DateUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_TITLE = "event_title";
    public static final String EXTRA_START_MILLIS = "start_millis";
    public static final String EXTRA_END_MILLIS = "end_millis";
    public static final String EXTRA_ALL_DAY = "all_day";
    public static final String EXTRA_DESCRIPTION = "description";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_CALENDAR_NAME = "calendar_name";
    public static final String EXTRA_ORGANIZER = "organizer";
    public static final String EXTRA_COLOR = "color";

    private long eventId;
    private CalendarRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        repository = new CalendarRepository(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        Intent intent = getIntent();
        eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1);
        String title = intent.getStringExtra(EXTRA_EVENT_TITLE);
        long startMillis = intent.getLongExtra(EXTRA_START_MILLIS, 0);
        long endMillis = intent.getLongExtra(EXTRA_END_MILLIS, 0);
        boolean allDay = intent.getBooleanExtra(EXTRA_ALL_DAY, false);
        String description = intent.getStringExtra(EXTRA_DESCRIPTION);
        String location = intent.getStringExtra(EXTRA_LOCATION);
        String calendarName = intent.getStringExtra(EXTRA_CALENDAR_NAME);
        String organizer = intent.getStringExtra(EXTRA_ORGANIZER);
        int color = intent.getIntExtra(EXTRA_COLOR, getColor(R.color.accent_purple));

        // Color dot
        View colorDot = findViewById(R.id.eventColorDot);
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(color);
        colorDot.setBackground(dot);

        ((TextView) findViewById(R.id.tvEventTitle)).setText(title != null ? title : "(No Title)");
        ((TextView) findViewById(R.id.tvEventTime)).setText(
                DateUtils.formatDateTimeRange(startMillis, endMillis, allDay));

        // Location
        LinearLayout locationRow = findViewById(R.id.locationRow);
        if (location != null && !location.isEmpty()) {
            locationRow.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvEventLocation)).setText(location);
        } else {
            locationRow.setVisibility(View.GONE);
        }

        // Description
        LinearLayout descRow = findViewById(R.id.descriptionRow);
        if (description != null && !description.isEmpty()) {
            descRow.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvEventDescription)).setText(description);
        } else {
            descRow.setVisibility(View.GONE);
        }

        // Calendar name
        String calName = calendarName != null && !calendarName.isEmpty()
                ? calendarName : getString(R.string.local_calendar);
        ((TextView) findViewById(R.id.tvCalendarName)).setText(calName);

        // Organizer
        LinearLayout organizerCard = findViewById(R.id.organizerCard);
        if (organizer != null && !organizer.isEmpty()) {
            organizerCard.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvOrganizer)).setText(organizer);
        } else {
            organizerCard.setVisibility(View.GONE);
        }

        // Delete
        findViewById(R.id.btnDelete).setOnClickListener(v -> confirmDelete());

        // Edit (open NewEventActivity pre-filled — basic)
        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent editIntent = new Intent(this, NewEventActivity.class);
            editIntent.putExtra(NewEventActivity.EXTRA_EDIT_EVENT_ID, eventId);
            editIntent.putExtra(NewEventActivity.EXTRA_EDIT_TITLE, title);
            editIntent.putExtra(NewEventActivity.EXTRA_EDIT_START, startMillis);
            editIntent.putExtra(NewEventActivity.EXTRA_EDIT_END, endMillis);
            editIntent.putExtra(NewEventActivity.EXTRA_EDIT_ALL_DAY, allDay);
            editIntent.putExtra(NewEventActivity.EXTRA_EDIT_DESC, description);
            editIntent.putExtra(NewEventActivity.EXTRA_EDIT_LOCATION, location);
            startActivity(editIntent);
            finish();
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (d, w) -> deleteEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent() {
        if (eventId < 0) return;
        executor.execute(() -> {
            boolean ok = repository.deleteEvent(eventId);
            runOnUiThread(() -> {
                if (ok) {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Could not delete event", Toast.LENGTH_SHORT).show();
                }
            });
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
