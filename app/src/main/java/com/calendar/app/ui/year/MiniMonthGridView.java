package com.calendar.app.ui.year;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.calendar.app.R;
import com.calendar.app.utils.DateUtils;

import java.util.Calendar;
import java.util.Set;

public class MiniMonthGridView extends View {

    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint todayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int year;
    private int month;
    private int daysInMonth;
    private int startOffset;
    private Set<Integer> eventDays;

    private static final float CELL_SIZE_DP = 20f;
    private static final float CIRCLE_RADIUS_DP = 9f;

    public MiniMonthGridView(Context context) {
        super(context);
        init();
    }

    public MiniMonthGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float d = getResources().getDisplayMetrics().density;

        textPaint.setTextSize(9 * d);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);

        todayPaint.setColor(getResources().getColor(R.color.accent_purple, null));
        todayPaint.setStyle(Paint.Style.FILL);

        dotPaint.setColor(getResources().getColor(R.color.accent_purple, null));
        dotPaint.setStyle(Paint.Style.FILL);
    }

    public void setMonth(int year, int month) {
        this.year = year;
        this.month = month;
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        startOffset = c.get(Calendar.DAY_OF_WEEK) - 1;
        requestLayout();
        invalidate();
    }

    public void setEventDays(Set<Integer> days) {
        this.eventDays = days;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float density = getResources().getDisplayMetrics().density;
        float cellSize = CELL_SIZE_DP * density;
        int totalCells = startOffset + daysInMonth;
        int rows = (int) Math.ceil(totalCells / 7.0);
        int h = (int) (rows * cellSize);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float cellSize = CELL_SIZE_DP * density;
        float circleR = CIRCLE_RADIUS_DP * density;
        float w = getWidth();
        float cellW = w / 7f;

        int day = 1;
        int totalCells = startOffset + daysInMonth;
        int rows = (int) Math.ceil(totalCells / 7.0);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 7; col++) {
                int cell = row * 7 + col;
                if (cell < startOffset || day > daysInMonth) continue;

                float cx = col * cellW + cellW / 2f;
                float cy = row * cellSize + cellSize / 2f;

                boolean isToday = DateUtils.isToday(year, month, day);
                if (isToday) {
                    canvas.drawCircle(cx, cy, circleR, todayPaint);
                }

                textPaint.setColor(isToday ? Color.WHITE : Color.argb(179, 255, 255, 255));
                canvas.drawText(String.valueOf(day), cx,
                        cy + textPaint.getTextSize() / 3f, textPaint);

                // Small dot for events
                if (!isToday && eventDays != null && eventDays.contains(day)) {
                    canvas.drawCircle(cx, cy + circleR + 2 * density, 2 * density, dotPaint);
                }

                day++;
            }
        }
    }
}
