package com.calendar.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.GroupVH> {

    public static class AgendaGroup {
        public long dateMillis;
        public String dateLabel;
        public List<CalendarEvent> events;

        public AgendaGroup(long dateMillis, String dateLabel, List<CalendarEvent> events) {
            this.dateMillis = dateMillis;
            this.dateLabel = dateLabel;
            this.events = events;
        }
    }

    private List<AgendaGroup> groups = new ArrayList<>();

    public void setGroups(List<AgendaGroup> groups) {
        this.groups = groups != null ? groups : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<AgendaGroup> getGroups() {
        return groups;
    }

    @NonNull
    @Override
    public GroupVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_agenda_group, parent, false);
        return new GroupVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupVH holder, int position) {
        AgendaGroup group = groups.get(position);
        holder.tvDate.setText(group.dateLabel);

        holder.eventCardsContainer.removeAllViews();
        Context ctx = holder.itemView.getContext();
        LayoutInflater inflater = LayoutInflater.from(ctx);

        for (CalendarEvent event : group.events) {
            View card = inflater.inflate(R.layout.item_event_tile, holder.eventCardsContainer, false);

            View colorBar = card.findViewById(R.id.colorBar);
            TextView tvTitle = card.findViewById(R.id.tvEventTitle);
            TextView tvSubtitle = card.findViewById(R.id.tvEventSubtitle);

            tvTitle.setText(event.getTitle());

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
            tvSubtitle.setText(subtitle);

            int color = ColorUtils.resolveEventColor(event.getCalendarColor(), ctx);
            colorBar.setBackgroundColor(color);

            card.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, EventDetailActivity.class);
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
                ctx.startActivity(intent);
            });

            holder.eventCardsContainer.addView(card);
        }
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupVH extends RecyclerView.ViewHolder {
        TextView tvDate;
        LinearLayout eventCardsContainer;

        GroupVH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            eventCardsContainer = itemView.findViewById(R.id.eventCardsContainer);
        }
    }
}
