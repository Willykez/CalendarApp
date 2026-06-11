package com.calendar.app.ui.event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.calendar.app.R;
import com.calendar.app.data.CalendarInfo;
import com.calendar.app.data.CalendarRepository;
import com.calendar.app.utils.DateUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewEventActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_EVENT_ID = "edit_event_id";
    public static final String EXTRA_EDIT_TITLE = "edit_title";
    public static final String EXTRA_EDIT_START = "edit_start";
    public static final String EXTRA_EDIT_END = "edit_end";
    public static final String EXTRA_EDIT_ALL_DAY = "edit_all_day";
    public static final String EXTRA_EDIT_DESC = "edit_desc";
    public static final String EXTRA_EDIT_LOCATION = "edit_location";

    private TextInputEditText etTitle, etDescription, etLocation;
    private SwitchMaterial switchAllDay;
    private TextView tvStartTime, tvEndTime, tvCalendar, tvTimezone;
    private View rowStartTime, rowEndTime;

    private Calendar startCal, endCal;
    private long selectedCalendarId = -1;
    private List<CalendarInfo> calendars;
    private boolean isEditMode = false;
    private long editEventId = -1;

    private CalendarRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final SimpleDateFormat dtFormat =
            new SimpleDateFormat("EEE, MMM d  h:mm a", Locale.getDefault());
    private final SimpleDateFormat dateOnlyFormat =
            new SimpleDateFormat("EEE, MMM d", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        repository = new CalendarRepository(this);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        switchAllDay = findViewById(R.id.switchAllDay);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvCalendar = findViewById(R.id.tvCalendar);
        tvTimezone = findViewById(R.id.tvTimezone);
        rowStartTime = findViewById(R.id.rowStartTime);
        rowEndTime = findViewById(R.id.rowEndTime);

        // Init times
        startCal = Calendar.getInstance();
        // Round to nearest hour
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        startCal.add(Calendar.HOUR_OF_DAY, 1);

        endCal = (Calendar) startCal.clone();
        endCal.add(Calendar.HOUR_OF_DAY, 1);

        tvTimezone.setText(TimeZone.getDefault().getID());

        // Check edit mode
        if (getIntent().hasExtra(EXTRA_EDIT_EVENT_ID)) {
            isEditMode = true;
            editEventId = getIntent().getLongExtra(EXTRA_EDIT_EVENT_ID, -1);
            String editTitle = getIntent().getStringExtra(EXTRA_EDIT_TITLE);
            long editStart = getIntent().getLongExtra(EXTRA_EDIT_START, startCal.getTimeInMillis());
            long editEnd = getIntent().getLongExtra(EXTRA_EDIT_END, endCal.getTimeInMillis());
            boolean editAllDay = getIntent().getBooleanExtra(EXTRA_EDIT_ALL_DAY, false);
            String editDesc = getIntent().getStringExtra(EXTRA_EDIT_DESC);
            String editLoc = getIntent().getStringExtra(EXTRA_EDIT_LOCATION);

            if (editTitle != null) etTitle.setText(editTitle);
            if (editDesc != null) etDescription.setText(editDesc);
            if (editLoc != null) etLocation.setText(editLoc);
            switchAllDay.setChecked(editAllDay);
            startCal.setTimeInMillis(editStart);
            endCal.setTimeInMillis(editEnd);
        }

        updateTimeLabels();

        switchAllDay.setOnCheckedChangeListener((btn, checked) -> {
            rowStartTime.setVisibility(checked ? View.GONE : View.VISIBLE);
            rowEndTime.setVisibility(checked ? View.GONE : View.VISIBLE);
            updateTimeLabels();
        });

        rowStartTime.setOnClickListener(v -> pickStartDateTime());
        rowEndTime.setOnClickListener(v -> pickEndDateTime());

        loadCalendars();

        findViewById(R.id.rowCalendar).setOnClickListener(v -> showCalendarPicker());
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveEvent());
    }

    private void updateTimeLabels() {
        boolean allDay = switchAllDay.isChecked();
        if (allDay) {
            tvStartTime.setText(dateOnlyFormat.format(startCal.getTime()));
            tvEndTime.setText(dateOnlyFormat.format(endCal.getTime()));
        } else {
            tvStartTime.setText(dtFormat.format(startCal.getTime()));
            tvEndTime.setText(dtFormat.format(endCal.getTime()));
        }
    }

    private void pickStartDateTime() {
        new DatePickerDialog(this, (view, y, m, d) -> {
            startCal.set(Calendar.YEAR, y);
            startCal.set(Calendar.MONTH, m);
            startCal.set(Calendar.DAY_OF_MONTH, d);
            if (!switchAllDay.isChecked()) {
                new TimePickerDialog(this, (tv, h, min) -> {
                    startCal.set(Calendar.HOUR_OF_DAY, h);
                    startCal.set(Calendar.MINUTE, min);
                    // Auto-adjust end if needed
                    if (endCal.before(startCal)) {
                        endCal = (Calendar) startCal.clone();
                        endCal.add(Calendar.HOUR_OF_DAY, 1);
                    }
                    updateTimeLabels();
                }, startCal.get(Calendar.HOUR_OF_DAY), startCal.get(Calendar.MINUTE), false).show();
            } else {
                updateTimeLabels();
            }
        }, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH),
                startCal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickEndDateTime() {
        new DatePickerDialog(this, (view, y, m, d) -> {
            endCal.set(Calendar.YEAR, y);
            endCal.set(Calendar.MONTH, m);
            endCal.set(Calendar.DAY_OF_MONTH, d);
            if (!switchAllDay.isChecked()) {
                new TimePickerDialog(this, (tv, h, min) -> {
                    endCal.set(Calendar.HOUR_OF_DAY, h);
                    endCal.set(Calendar.MINUTE, min);
                    updateTimeLabels();
                }, endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE), false).show();
            } else {
                updateTimeLabels();
            }
        }, endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH),
                endCal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadCalendars() {
        executor.execute(() -> {
            calendars = repository.getCalendars();
            runOnUiThread(() -> {
                if (calendars != null && !calendars.isEmpty()) {
                    CalendarInfo first = calendars.get(0);
                    selectedCalendarId = first.getId();
                    tvCalendar.setText(first.getName());
                } else {
                    tvCalendar.setText(getString(R.string.local_calendar));
                }
            });
        });
    }

    private void showCalendarPicker() {
        if (calendars == null || calendars.isEmpty()) return;
        String[] names = new String[calendars.size()];
        for (int i = 0; i < calendars.size(); i++) {
            names[i] = calendars.get(i).getName();
        }
        new android.app.AlertDialog.Builder(this)
                .setTitle("Save to Calendar")
                .setItems(names, (dialog, which) -> {
                    CalendarInfo selected = calendars.get(which);
                    selectedCalendarId = selected.getId();
                    tvCalendar.setText(selected.getName());
                })
                .show();
    }

    private void saveEvent() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }

        if (selectedCalendarId < 0) {
            Toast.makeText(this, "No calendar selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endCal.before(startCal)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String loc = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        boolean allDay = switchAllDay.isChecked();

        executor.execute(() -> {
            // Delete old event in edit mode
            if (isEditMode && editEventId >= 0) {
                repository.deleteEvent(editEventId);
            }

            long newId = repository.insertEvent(title, startCal.getTimeInMillis(),
                    endCal.getTimeInMillis(), allDay, desc, loc, selectedCalendarId,
                    TimeZone.getDefault().getID());

            runOnUiThread(() -> {
                if (newId >= 0) {
                    Toast.makeText(this, "Event saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save event. Check calendar permission.",
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
