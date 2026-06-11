package com.calendar.app.ui.month;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.calendar.app.R;
import com.calendar.app.data.CalendarEvent;
import com.calendar.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthGridView extends View {

    public interface OnDayClickListener {
        void onDayClick(int year, int month, int day);
    }

    // Paints
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint todayCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint weekNumPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // State
    private int year;
    private int month; // 0-based
    private int selectedDay;
    private boolean showWeekNumbers = true;
    private final Map<Integer, List<Integer>> eventDots = new HashMap<>(); // day -> list of colors

    private OnDayClickListener listener;

    // Layout
    private float cellW;
    private float cellH;
    private float weekNumW;
    private int firstDayOfWeek; // 0=Sun, 1=Mon
    private int daysInMonth;
    private int startOffset; // weekday offset of day 1

    private static final float CELL_HEIGHT_DP = 44f;
    private static final float WEEK_NUM_WIDTH_DP = 28f;
    private static final float DOT_RADIUS_DP = 2.5f;
    private static final float CIRCLE_RADIUS_DP = 16f;

    public MonthGridView(Context context) {
        super(context);
        init();
    }

    public MonthGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MonthGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(16 * density);
        textPaint.setTextAlign(Paint.Align.CENTER);

        todayCirclePaint.setColor(getResources().getColor(R.color.accent_purple, null));
        todayCirclePaint.setStyle(Paint.Style.FILL);

        selectedCirclePaint.setColor(0x33704FFE);
        selectedCirclePaint.setStyle(Paint.Style.FILL);

        weekNumPaint.setColor(Color.argb(60, 255, 255, 255));
        weekNumPaint.setTextSize(11 * density);
        weekNumPaint.setTextAlign(Paint.Align.LEFT);

        dotPaint.setStyle(Paint.Style.FILL);

        // Set today as default
        Calendar today = Calendar.getInstance();
        this.year = today.get(Calendar.YEAR);
        this.month = today.get(Calendar.MONTH);
        this.selectedDay = today.get(Calendar.DAY_OF_MONTH);
        computeLayout();
    }

    private void computeLayout() {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        // Offset: what weekday does the 1st fall on? (0=Sun)
        startOffset = c.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public void setMonth(int year, int month) {
        this.year = year;
        this.month = month;
        computeLayout();
        requestLayout();
        invalidate();
    }

    public void setSelectedDay(int day) {
        this.selectedDay = day;
        invalidate();
    }

    public void setShowWeekNumbers(boolean show) {
        this.showWeekNumbers = show;
        invalidate();
    }

    public void setEventColors(Map<Integer, List<Integer>> dotColors) {
        this.eventDots.clear();
        this.eventDots.putAll(dotColors);
        invalidate();
    }

    public void setOnDayClickListener(OnDayClickListener l) {
        this.listener = l;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        float density = getResources().getDisplayMetrics().density;
        weekNumW = showWeekNumbers ? WEEK_NUM_WIDTH_DP * density : 0;
        cellW = (w - getPaddingLeft() - getPaddingRight() - weekNumW) / 7f;
        cellH = CELL_HEIGHT_DP * density;

        // Count rows
        int totalCells = startOffset + daysInMonth;
        int rows = (int) Math.ceil(totalCells / 7.0);
        int h = (int) (rows * cellH) + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float circleRadius = CIRCLE_RADIUS_DP * density;
        float dotRadius = DOT_RADIUS_DP * density;
        float dotY = cellH - 6 * density;

        int pLeft = getPaddingLeft();
        int pTop = getPaddingTop();

        int day = 1;
        int totalCells = startOffset + daysInMonth;
        int rows = (int) Math.ceil(totalCells / 7.0);

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);

        for (int row = 0; row < rows; row++) {
            float rowY = pTop + row * cellH + cellH / 2f;

            // Week number
            if (showWeekNumbers) {
                int dayOfRow = row == 0 ? 1 : (row * 7 - startOffset + 1);
                if (dayOfRow < 1) dayOfRow = 1;
                if (dayOfRow <= daysInMonth) {
                    cal.set(year, month, dayOfRow);
                    int weekNum = cal.get(Calendar.WEEK_OF_YEAR);
                    canvas.drawText(String.valueOf(weekNum),
                            pLeft, rowY + weekNumPaint.getTextSize() / 3, weekNumPaint);
                }
            }

            for (int col = 0; col < 7; col++) {
                int cell = row * 7 + col;
                if (cell < startOffset || day > daysInMonth) continue;

                float cx = pLeft + weekNumW + col * cellW + cellW / 2f;
                float cy = rowY;

                boolean isToday = DateUtils.isToday(year, month, day);
                boolean isSelected = (day == selectedDay) && !isToday;

                // Draw circle background
                if (isToday) {
                    canvas.drawCircle(cx, cy, circleRadius, todayCirclePaint);
                } else if (isSelected) {
                    canvas.drawCircle(cx, cy, circleRadius, selectedCirclePaint);
                }

                // Day number
                textPaint.setColor(Color.WHITE);
                textPaint.setFakeBoldText(isToday);
                canvas.drawText(String.valueOf(day), cx, cy + textPaint.getTextSize() / 3f, textPaint);
                textPaint.setFakeBoldText(false);

                // Event dots
                List<Integer> dots = eventDots.get(day);
                if (dots != null && !dots.isEmpty()) {
                    int maxDots = Math.min(dots.size(), 3);
                    float totalDotsW = maxDots * dotRadius * 2 + (maxDots - 1) * 3 * density;
                    float dotStartX = cx - totalDotsW / 2f + dotRadius;
                    for (int d = 0; d < maxDots; d++) {
                        dotPaint.setColor(dots.get(d));
                        canvas.drawCircle(dotStartX + d * (dotRadius * 2 + 3 * density),
                                cy + circleRadius + 4 * density, dotRadius, dotPaint);
                    }
                }

                day++;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && listener != null) {
            float x = event.getX() - getPaddingLeft() - weekNumW;
            float y = event.getY() - getPaddingTop();
            int col = (int) (x / cellW);
            int row = (int) (y / cellH);
            int cell = row * 7 + col;
            int tapDay = cell - startOffset + 1;
            if (tapDay >= 1 && tapDay <= daysInMonth && col >= 0 && col < 7) {
                selectedDay = tapDay;
                invalidate();
                listener.onDayClick(year, month, tapDay);
            }
        }
        return true;
    }
}
