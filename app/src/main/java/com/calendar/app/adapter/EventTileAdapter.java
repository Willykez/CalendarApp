package com.calendar.app.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calendar.app.R;
import com.calendar.app.data.CalendarEvent;
import com.calendar.app.ui.event.EventDetailActivity;
import com.calendar.app.utils.ColorUtils;
import com.calendar.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class EventTileAdapter extends RecyclerView.Adapter<EventTileAdapter.EventVH> {

    private List<CalendarEvent> events = new ArrayList<>();

    public void setEvents(List<CalendarEvent> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_tile, parent, false);
        return new EventVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventVH holder, int position) {
        CalendarEvent event = events.get(position);

        holder.tvTitle.setText(event.getTitle());

        // Subtitle: time or "All-day", plus calendar name
        String subtitle;
        if (event.isAllDay()) {
            subtitle = "All-day";
        } else {
            subtitle = DateUtils.formatTime(event.getStartMillis())
                    + " – " + DateUtils.formatTime(event.getEndMillis());
        }
        if (event.getCalendarName() != null && !event.getCalendarName().isEmpty()) {
            subtitle += "  ·  " + event.getCalendarName();
        }
        holder.tvSubtitle.setText(subtitle);

        int color = ColorUtils.resolveEventColor(event.getCalendarColor(), holder.itemView.getContext());
        holder.colorBar.setBackgroundColor(color);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
            intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.getId());
            intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, event.getTitle());
            intent.putExtra(EventDetailActivity.EXTRA_START_MILLIS, event.getStartMillis());
            intent.putExtra(EventDetailActivity.EXTRA_END_MILLIS, event.getEndMillis());
            intent.putExtra(EventDetailActivity.EXTRA_ALL_DAY, event.isAllDay());
            intent.putExtra(EventDetailActivity.EXTRA_DESCRIPTION, event.getDescription());
            intent.putExtra(EventDetailActivity.EXTRA_LOCATION, event.getLocation());
            intent.putExtra(EventDetailActivity.EXTRA_CALENDAR_NAME, event.getCalendarName());
            intent.putExtra(EventDetailActivity.EXTRA_ORGANIZER, event.getOrganizer());
            intent.putExtra(EventDetailActivity.EXTRA_COLOR, color);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventVH extends RecyclerView.ViewHolder {
        View colorBar;
        TextView tvTitle;
        TextView tvSubtitle;

        EventVH(@NonNull View itemView) {
            super(itemView);
            colorBar = itemView.findViewById(R.id.colorBar);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvSubtitle = itemView.findViewById(R.id.tvEventSubtitle);
        }
    }
}
