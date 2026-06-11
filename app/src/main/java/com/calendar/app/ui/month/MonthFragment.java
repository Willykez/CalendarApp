package com.calendar.app.ui.month;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calendar.app.R;
import com.calendar.app.adapter.EventTileAdapter;
import com.calendar.app.data.CalendarEvent;
import com.calendar.app.data.CalendarRepository;
import com.calendar.app.ui.event.NewEventActivity;
import com.calendar.app.ui.settings.SettingsActivity;
import com.calendar.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MonthFragment extends Fragment {

    private int year, month, selectedDay;
    private TextView tvMonthTitle, tvYearSubtitle, tvSelectedDay;
    private MonthGridView monthGrid;
    private RecyclerView rvDayEvents;
    private LinearLayout emptyState;
    private EventTileAdapter eventAdapter;
    private CalendarRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // All events for this month keyed by day
    private Map<Integer, List<CalendarEvent>> eventsByDay = new HashMap<>();

    public static MonthFragment newInstance(int year, int month) {
        MonthFragment f = new MonthFragment();
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_month, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Calendar today = Calendar.getInstance();
        if (getArguments() != null) {
            year = getArguments().getInt("year", today.get(Calendar.YEAR));
            month = getArguments().getInt("month", today.get(Calendar.MONTH));
        } else {
            year = today.get(Calendar.YEAR);
            month = today.get(Calendar.MONTH);
        }
        selectedDay = (year == today.get(Calendar.YEAR) && month == today.get(Calendar.MONTH))
                ? today.get(Calendar.DAY_OF_MONTH) : 1;

        repository = new CalendarRepository(requireContext());

        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        tvYearSubtitle = view.findViewById(R.id.tvYearSubtitle);
        tvSelectedDay = view.findViewById(R.id.tvSelectedDay);
        monthGrid = view.findViewById(R.id.monthGrid);
        emptyState = view.findViewById(R.id.emptyState);

        rvDayEvents = view.findViewById(R.id.rvDayEvents);
        rvDayEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventAdapter = new EventTileAdapter();
        rvDayEvents.setAdapter(eventAdapter);

        view.findViewById(R.id.btnPrevMonth).setOnClickListener(v -> changeMonth(-1));
        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> changeMonth(1));
        view.findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));
        view.findViewById(R.id.btnToday).setOnClickListener(v -> jumpToToday());
        view.findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NewEventActivity.class)));

        monthGrid.setOnDayClickListener((y, m, d) -> {
            selectedDay = d;
            showDayEvents(d);
        });

        updateHeader();
        loadMonthEvents();
    }

    public void setMonth(int year, int month) {
        this.year = year;
        this.month = month;
        Calendar today = Calendar.getInstance();
        selectedDay = (year == today.get(Calendar.YEAR) && month == today.get(Calendar.MONTH))
                ? today.get(Calendar.DAY_OF_MONTH) : 1;
        updateHeader();
        loadMonthEvents();
    }

    private void changeMonth(int delta) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        c.add(Calendar.MONTH, delta);
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        selectedDay = 1;
        updateHeader();
        loadMonthEvents();
    }

    private void jumpToToday() {
        Calendar today = Calendar.getInstance();
        year = today.get(Calendar.YEAR);
        month = today.get(Calendar.MONTH);
        selectedDay = today.get(Calendar.DAY_OF_MONTH);
        updateHeader();
        loadMonthEvents();
    }

    private void updateHeader() {
        tvMonthTitle.setText(DateUtils.formatMonth(year, month));
        tvYearSubtitle.setText(DateUtils.formatYear(year));
        monthGrid.setMonth(year, month);
        monthGrid.setSelectedDay(selectedDay);
        tvSelectedDay.setText(DateUtils.formatDayLabel(year, month, selectedDay));
    }

    private void loadMonthEvents() {
        executor.execute(() -> {
            Calendar start = DateUtils.getStartOfMonth(year, month);
            Calendar end = DateUtils.getEndOfMonth(year, month);
            List<CalendarEvent> events = repository.getEvents(
                    start.getTimeInMillis(), end.getTimeInMillis());

            // Group by day
            Map<Integer, List<CalendarEvent>> byDay = new HashMap<>();
            Map<Integer, List<Integer>> dotColors = new HashMap<>();
            for (CalendarEvent e : events) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(e.getStartMillis());
                int day = c.get(Calendar.DAY_OF_MONTH);
                byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(e);
                dotColors.computeIfAbsent(day, k -> new ArrayList<>())
                        .add(e.getCalendarColor() != 0 ? e.getCalendarColor()
                                : requireContext().getColor(R.color.accent_purple));
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    eventsByDay = byDay;
                    monthGrid.setEventColors(dotColors);
                    showDayEvents(selectedDay);
                });
            }
        });
    }

    private void showDayEvents(int day) {
        tvSelectedDay.setText(DateUtils.formatDayLabel(year, month, day));
        List<CalendarEvent> dayEvents = eventsByDay.getOrDefault(day, new ArrayList<>());
        eventAdapter.setEvents(dayEvents);

        boolean hasEvents = !dayEvents.isEmpty();
        rvDayEvents.setVisibility(hasEvents ? View.VISIBLE : View.GONE);
        emptyState.setVisibility(hasEvents ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdown();
    }
}
