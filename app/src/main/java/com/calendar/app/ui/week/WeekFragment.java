package com.calendar.app.ui.week;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.calendar.app.R;
import com.calendar.app.data.CalendarEvent;
import com.calendar.app.data.CalendarRepository;
import com.calendar.app.ui.event.NewEventActivity;
import com.calendar.app.ui.settings.SettingsActivity;
import com.calendar.app.utils.DateUtils;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeekFragment extends Fragment {

    private Calendar weekStart;
    private TextView tvWeekMonthTitle, tvWeekYearSubtitle;
    private LinearLayout dayStripContainer;
    private WeekGridView weekGridView;
    private CalendarRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_week, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new CalendarRepository(requireContext());

        tvWeekMonthTitle = view.findViewById(R.id.tvWeekMonthTitle);
        tvWeekYearSubtitle = view.findViewById(R.id.tvWeekYearSubtitle);
        dayStripContainer = view.findViewById(R.id.dayStripContainer);
        weekGridView = view.findViewById(R.id.weekGridView);

        // Init to current week (Sunday-start)
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        weekStart = today;

        view.findViewById(R.id.btnPrevWeek).setOnClickListener(v -> changeWeek(-1));
        view.findViewById(R.id.btnNextWeek).setOnClickListener(v -> changeWeek(1));
        view.findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));
        view.findViewById(R.id.btnToday).setOnClickListener(v -> jumpToToday());
        view.findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NewEventActivity.class)));

        updateUI();

        // Scroll to ~8 AM on open
        view.post(() -> {
            View sv = view.findViewById(R.id.weekScrollView);
            if (sv instanceof androidx.core.widget.NestedScrollView) {
                float density = getResources().getDisplayMetrics().density;
                int scrollTo = (int) (8 * 56 * density);
                ((androidx.core.widget.NestedScrollView) sv).smoothScrollTo(0, scrollTo);
            }
        });
    }

    private void changeWeek(int delta) {
        weekStart = (Calendar) weekStart.clone();
        weekStart.add(Calendar.WEEK_OF_YEAR, delta);
        updateUI();
    }

    private void jumpToToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        weekStart = today;
        updateUI();
    }

    private void updateUI() {
        // Header: show month of the week's middle day (Wednesday)
        Calendar mid = (Calendar) weekStart.clone();
        mid.add(Calendar.DAY_OF_YEAR, 3);
        tvWeekMonthTitle.setText(DateUtils.formatMonth(mid.get(Calendar.YEAR), mid.get(Calendar.MONTH)));
        tvWeekYearSubtitle.setText(DateUtils.formatYear(mid.get(Calendar.YEAR)));

        buildDayStrip();
        weekGridView.setWeekStart(weekStart);
        loadWeekEvents();
    }

    private void buildDayStrip() {
        dayStripContainer.removeAllViews();
        Context ctx = requireContext();
        Calendar today = Calendar.getInstance();
        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) weekStart.clone();
            day.add(Calendar.DAY_OF_YEAR, i);

            boolean isToday = day.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && day.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);

            TextView tv = new TextView(ctx);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setText(DateUtils.formatWeekTabDay(
                    day.get(Calendar.YEAR),
                    day.get(Calendar.MONTH),
                    day.get(Calendar.DAY_OF_MONTH)));
            tv.setTextColor(isToday
                    ? ctx.getColor(R.color.text_primary)
                    : ctx.getColor(R.color.text_muted));
            tv.setTextSize(13);
            tv.setPadding((int)(8*density), (int)(4*density), (int)(8*density), (int)(8*density));

            if (isToday) {
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(ctx.getColor(R.color.accent_purple));
                tv.setBackground(circle);
            }

            dayStripContainer.addView(tv);
        }
    }

    private void loadWeekEvents() {
        Calendar start = (Calendar) weekStart.clone();
        Calendar end = (Calendar) weekStart.clone();
        end.add(Calendar.DAY_OF_YEAR, 7);
        end.add(Calendar.MILLISECOND, -1);

        executor.execute(() -> {
            List<CalendarEvent> events = repository.getEvents(
                    start.getTimeInMillis(), end.getTimeInMillis());

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    weekGridView.setEvents(events);
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdown();
    }
}
