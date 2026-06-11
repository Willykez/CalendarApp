package com.calendar.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calendar.app.R;
import com.calendar.app.ui.year.MiniMonthGridView;
import com.calendar.app.utils.DateUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class YearMonthAdapter extends RecyclerView.Adapter<YearMonthAdapter.MonthVH> {

    private int year;
    private Map<Integer, Set<Integer>> eventsByMonth; // month -> set of days with events

    public interface OnMonthClickListener {
        void onMonthClick(int month);
    }

    private OnMonthClickListener listener;

    public YearMonthAdapter(int year, Map<Integer, Set<Integer>> eventsByMonth, OnMonthClickListener listener) {
        this.year = year;
        this.eventsByMonth = eventsByMonth;
        this.listener = listener;
    }

    public void update(int year, Map<Integer, Set<Integer>> eventsByMonth) {
        this.year = year;
        this.eventsByMonth = eventsByMonth;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MonthVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mini_month, parent, false);
        return new MonthVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthVH holder, int position) {
        int month = position; // 0-based
        holder.tvMonthName.setText(DateUtils.formatMonthShort(month));

        // Highlight current month name
        boolean isCurrentMonth = isCurrentMonth(month);
        int color = isCurrentMonth
                ? holder.itemView.getContext().getColor(R.color.accent_purple)
                : holder.itemView.getContext().getColor(R.color.text_primary);
        holder.tvMonthName.setTextColor(color);

        holder.miniGrid.setMonth(year, month);
        Set<Integer> days = eventsByMonth != null ? eventsByMonth.get(month) : null;
        holder.miniGrid.setEventDays(days != null ? days : new HashSet<>());

        int pos = position;
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMonthClick(pos);
        });
    }

    private boolean isCurrentMonth(int month) {
        java.util.Calendar today = java.util.Calendar.getInstance();
        return today.get(java.util.Calendar.YEAR) == year
                && today.get(java.util.Calendar.MONTH) == month;
    }

    @Override
    public int getItemCount() {
        return 12;
    }

    static class MonthVH extends RecyclerView.ViewHolder {
        TextView tvMonthName;
        MiniMonthGridView miniGrid;

        MonthVH(@NonNull View itemView) {
            super(itemView);
            tvMonthName = itemView.findViewById(R.id.tvMonthName);
            miniGrid = itemView.findViewById(R.id.miniMonthGrid);
        }
    }
}
