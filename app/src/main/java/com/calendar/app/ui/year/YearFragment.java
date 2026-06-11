package com.calendar.app.ui.year;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calendar.app.R;
import com.calendar.app.adapter.YearMonthAdapter;
import com.calendar.app.data.CalendarEvent;
import com.calendar.app.data.CalendarRepository;
import com.calendar.app.ui.event.NewEventActivity;
import com.calendar.app.ui.settings.SettingsActivity;
import com.calendar.app.utils.DateUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YearFragment extends Fragment {

    private int currentYear;
    private TextView tvYear;
    private YearMonthAdapter adapter;
    private CalendarRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_year, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        repository = new CalendarRepository(requireContext());

        tvYear = view.findViewById(R.id.tvYear);
        RecyclerView rv = view.findViewById(R.id.rvYearGrid);

        rv.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        adapter = new YearMonthAdapter(currentYear, new HashMap<>(), month -> {
            // Switch to month view at that month (via MainActivity)
            if (getActivity() instanceof YearMonthNavListener) {
                ((YearMonthNavListener) getActivity()).navigateToMonth(currentYear, month);
            }
        });
        rv.setAdapter(adapter);

        view.findViewById(R.id.btnPrevYear).setOnClickListener(v -> changeYear(-1));
        view.findViewById(R.id.btnNextYear).setOnClickListener(v -> changeYear(1));
        view.findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));
        view.findViewById(R.id.btnToday).setOnClickListener(v -> {
            currentYear = Calendar.getInstance().get(Calendar.YEAR);
            updateUI();
        });
        view.findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NewEventActivity.class)));

        updateUI();
    }

    private void changeYear(int delta) {
        currentYear += delta;
        updateUI();
    }

    private void updateUI() {
        tvYear.setText(DateUtils.formatYear(currentYear));
        loadEventDots();
    }

    private void loadEventDots() {
        int year = currentYear;
        executor.execute(() -> {
            Calendar start = Calendar.getInstance();
            start.set(year, 0, 1, 0, 0, 0);
            start.set(Calendar.MILLISECOND, 0);

            Calendar end = Calendar.getInstance();
            end.set(year, 11, 31, 23, 59, 59);
            end.set(Calendar.MILLISECOND, 999);

            List<CalendarEvent> events = repository.getEvents(
                    start.getTimeInMillis(), end.getTimeInMillis());

            Map<Integer, Set<Integer>> byMonth = new HashMap<>();
            for (CalendarEvent e : events) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(e.getStartMillis());
                int m = c.get(Calendar.MONTH);
                int d = c.get(Calendar.DAY_OF_MONTH);
                byMonth.computeIfAbsent(m, k -> new HashSet<>()).add(d);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter != null) adapter.update(year, byMonth);
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdown();
    }

    public interface YearMonthNavListener {
        void navigateToMonth(int year, int month);
    }
}
