package com.calendar.app.utils;

import android.content.Context;
import android.graphics.Color;

import com.calendar.app.R;

public class ColorUtils {

    private static final int[] EVENT_COLORS = {
            0xFF704FFE, // purple
            0xFF3B82F6, // blue
            0xFF34C759, // green
            0xFFFF9800, // orange
            0xFFFF3B30, // red
            0xFF5AC8FA, // teal
            0xFFFFCC00, // yellow
    };

    /**
     * Get a display-safe color from a raw calendar color int.
     * Falls back to purple if the color is transparent or zero.
     */
    public static int resolveEventColor(int rawColor, Context context) {
        if (rawColor == 0 || Color.alpha(rawColor) == 0) {
            return context.getColor(R.color.accent_purple);
        }
        return rawColor;
    }

    /**
     * Rotate through event palette colors by index (for calendars with no color).
     */
    public static int getEventColorByIndex(int index) {
        return EVENT_COLORS[Math.abs(index) % EVENT_COLORS.length];
    }
}
