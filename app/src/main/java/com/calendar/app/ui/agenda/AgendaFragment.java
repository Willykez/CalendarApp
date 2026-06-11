package com.calendar.app.ui.agenda;

import android.content.Intent;
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
import com.calendar.app.adapter.AgendaAdapter;
import com.calendar.app.data.CalendarEvent;
import com.calendar.app.data.CalendarRepository;
import com.calendar.app.ui.event.NewEventActivity;
import com.calendar.app.ui.settings.SettingsActivity;
import com.calendar.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgendaFragment extends Fragment {

    private TextView tvAgendaMonth, tvAgendaYear;
    private RecyclerView rvAgenda;
    private LinearLayout emptyState;
    private AgendaAdapter adapter;
    private CalendarRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Look-ahead: 90 days from today
    private static final int LOOK_AHEAD_DAYS = 90;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new CalendarRepository(requireContext());

        tvAgendaMonth = view.findViewById(R.id.tvAgendaMonth);
        tvAgendaYear = view.findViewById(R.id.tvAgendaYear);
        emptyState = view.findViewById(R.id.emptyState);

        rvAgenda = view.findViewById(R.id.rvAgenda);
        rvAgenda.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AgendaAdapter();
        rvAgenda.setAdapter(adapter);

        view.findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));
        view.findViewById(R.id.btnToday).setOnClickListener(v -> loadEvents());
        view.findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NewEventActivity.class)));

        // Update header when list scrolls
        rvAgenda.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm != null) {
                    int firstVisible = lm.findFirstVisibleItemPosition();
                    List<AgendaAdapter.AgendaGroup> groups = adapter.getGroups();
                    if (firstVisible >= 0 && firstVisible < groups.size()) {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(groups.get(firstVisible).dateMillis);
                        tvAgendaMonth.setText(DateUtils.formatMonth(
                                c.get(Calendar.YEAR), c.get(Calendar.MONTH)));
                        tvAgendaYear.setText(DateUtils.formatYear(c.get(Calendar.YEAR)));
                    }
                }
            }
        });

        loadEvents();
    }

    private void loadEvents() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        tvAgendaMonth.setText(DateUtils.formatMonth(
                today.get(Calendar.YEAR), today.get(Calendar.MONTH)));
        tvAgendaYear.setText(DateUtils.formatYear(today.get(Calendar.YEAR)));

        Calendar end = (Calendar) today.clone();
        end.add(Calendar.DAY_OF_YEAR, LOOK_AHEAD_DAYS);

        executor.execute(() -> {
            List<CalendarEvent> events = repository.getEvents(
                    today.getTimeInMillis(), end.getTimeInMillis());

            // Group by day (using day-start millis as key)
            TreeMap<Long, List<CalendarEvent>> byDay = new TreeMap<>();
            for (CalendarEvent e : events) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(e.getStartMillis());
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                long key = c.getTimeInMillis();
                byDay.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
            }

            List<AgendaAdapter.AgendaGroup> groups = new ArrayList<>();
            for (Long dayMillis : byDay.keySet()) {
                List<CalendarEvent> dayEvents = byDay.get(dayMillis);
                String label = DateUtils.formatAgendaDateLabel(dayMillis);
                groups.add(new AgendaAdapter.AgendaGroup(dayMillis, label, dayEvents));
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.setGroups(groups);
                    boolean hasData = !groups.isEmpty();
                    rvAgenda.setVisibility(hasData ? View.VISIBLE : View.GONE);
                    emptyState.setVisibility(hasData ? View.GONE : View.VISIBLE);
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
