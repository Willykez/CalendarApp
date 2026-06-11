package com.calendar.app.ui.week;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.calendar.app.R;
import com.calendar.app.data.CalendarEvent;
import com.calendar.app.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WeekGridView extends View {

    private final Paint linePaint = new Paint();
    private final Paint hourLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eventTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nowLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int HOURS = 24;
    private static final float HOUR_HEIGHT_DP = 56f;
    private static final float GUTTER_W_DP = 44f;

    private float hourH;
    private float gutterW;
    private float colW;

    private List<CalendarEvent> events = new ArrayList<>();
    private Calendar weekStart;
    private int selectedColIndex = -1; // which day column is "today"

    public WeekGridView(Context context) {
        super(context);
        init();
    }

    public WeekGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        hourH = HOUR_HEIGHT_DP * density;
        gutterW = GUTTER_W_DP * density;

        linePaint.setColor(Color.argb(26, 255, 255, 255));
        linePaint.setStrokeWidth(1);

        hourLabelPaint.setColor(Color.argb(140, 255, 255, 255));
        hourLabelPaint.setTextSize(11 * density);
        hourLabelPaint.setTextAlign(Paint.Align.RIGHT);

        eventPaint.setStyle(Paint.Style.FILL);

        eventTextPaint.setColor(Color.WHITE);
        eventTextPaint.setTextSize(11 * density);

        nowLinePaint.setColor(getResources().getColor(R.color.accent_purple, null));
        nowLinePaint.setStrokeWidth(2 * density);

        // Default: current week
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        weekStart = c;
    }

    public void setWeekStart(Calendar weekStart) {
        this.weekStart = weekStart;
        computeSelectedCol();
        invalidate();
    }

    public void setEvents(List<CalendarEvent> events) {
        this.events = events != null ? events : new ArrayList<>();
        invalidate();
    }

    private void computeSelectedCol() {
        Calendar today = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) weekStart.clone();
            day.add(Calendar.DAY_OF_YEAR, i);
            if (day.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && day.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                selectedColIndex = i;
                return;
            }
        }
        selectedColIndex = -1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = (int) (HOURS * hourH);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        colW = (w - gutterW) / 7f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;

        // Horizontal hour lines + labels
        for (int hour = 0; hour < HOURS; hour++) {
            float y = hour * hourH;
            canvas.drawLine(gutterW, y, getWidth(), y, linePaint);

            String label;
            if (hour == 0) label = "12 AM";
            else if (hour < 12) label = hour + " AM";
            else if (hour == 12) label = "12 PM";
            else label = (hour - 12) + " PM";

            canvas.drawText(label, gutterW - 6 * density, y + hourLabelPaint.getTextSize(), hourLabelPaint);
        }

        // Vertical column lines
        for (int col = 0; col < 8; col++) {
            float x = gutterW + col * colW;
            canvas.drawLine(x, 0, x, getHeight(), linePaint);
        }

        // Highlight today column
        if (selectedColIndex >= 0) {
            Paint todayColPaint = new Paint();
            todayColPaint.setColor(Color.argb(15, 112, 79, 254));
            todayColPaint.setStyle(Paint.Style.FILL);
            float colX = gutterW + selectedColIndex * colW;
            canvas.drawRect(colX, 0, colX + colW, getHeight(), todayColPaint);
        }

        // Draw events
        if (weekStart != null && events != null) {
            for (CalendarEvent event : events) {
                drawEvent(canvas, event, density);
            }
        }

        // Now line
        Calendar now = Calendar.getInstance();
        if (selectedColIndex >= 0) {
            float nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60f + now.get(Calendar.MINUTE);
            float nowY = nowMinutes / 60f * hourH;
            float colX = gutterW + selectedColIndex * colW;
            // Dot
            Paint nowDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            nowDotPaint.setColor(getResources().getColor(R.color.accent_purple, null));
            nowDotPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(colX, nowY, 5 * density, nowDotPaint);
            canvas.drawLine(colX, nowY, colX + colW, nowY, nowLinePaint);
        }
    }

    private void drawEvent(Canvas canvas, CalendarEvent event, float density) {
        if (weekStart == null) return;

        Calendar eventStart = Calendar.getInstance();
        eventStart.setTimeInMillis(event.getStartMillis());
        Calendar eventEnd = Calendar.getInstance();
        eventEnd.setTimeInMillis(event.getEndMillis());

        // Determine which column
        int dayOffset = -1;
        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) weekStart.clone();
            day.add(Calendar.DAY_OF_YEAR, i);
            if (day.get(Calendar.YEAR) == eventStart.get(Calendar.YEAR)
                    && day.get(Calendar.DAY_OF_YEAR) == eventStart.get(Calendar.DAY_OF_YEAR)) {
                dayOffset = i;
                break;
            }
        }
        if (dayOffset < 0) return;

        float startMinutes = eventStart.get(Calendar.HOUR_OF_DAY) * 60f + eventStart.get(Calendar.MINUTE);
        float endMinutes = eventEnd.get(Calendar.HOUR_OF_DAY) * 60f + eventEnd.get(Calendar.MINUTE);
        if (endMinutes <= startMinutes) endMinutes = startMinutes + 30;

        float top = startMinutes / 60f * hourH + 2 * density;
        float bottom = endMinutes / 60f * hourH - 2 * density;
        float left = gutterW + dayOffset * colW + 2 * density;
        float right = gutterW + (dayOffset + 1) * colW - 2 * density;

        int color = ColorUtils.resolveEventColor(event.getCalendarColor(), getContext());
        eventPaint.setColor(color);
        eventPaint.setAlpha(200);

        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, 4 * density, 4 * density, eventPaint);

        // Event title
        if (bottom - top > 14 * density) {
            canvas.save();
            canvas.clipRect(left + 4 * density, top + 2 * density, right - 2 * density, bottom);
            canvas.drawText(event.getTitle(), left + 4 * density, top + 14 * density, eventTextPaint);
            canvas.restore();
        }
    }
}
