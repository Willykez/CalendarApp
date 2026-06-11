package com.calendar.app.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.calendar.app.R;
import com.calendar.app.ui.agenda.AgendaFragment;
import com.calendar.app.ui.month.MonthFragment;
import com.calendar.app.ui.week.WeekFragment;
import com.calendar.app.ui.year.YearFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements YearFragment.YearMonthNavListener {

    private static final int REQUEST_CALENDAR = 100;

    private BottomNavigationView bottomNav;
    private View permissionBanner;

    // Fragment instances (kept alive to preserve state)
    private YearFragment yearFragment;
    private MonthFragment monthFragment;
    private WeekFragment weekFragment;
    private AgendaFragment agendaFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        permissionBanner = findViewById(R.id.permissionBanner);

        // Create fragments once
        yearFragment = new YearFragment();
        monthFragment = new MonthFragment();
        weekFragment = new WeekFragment();
        agendaFragment = new AgendaFragment();

        // Add all fragments, hide all but month (default)
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, yearFragment, "year").hide(yearFragment)
                .add(R.id.fragmentContainer, monthFragment, "month")
                .add(R.id.fragmentContainer, weekFragment, "week").hide(weekFragment)
                .add(R.id.fragmentContainer, agendaFragment, "agenda").hide(agendaFragment)
                .commit();

        activeFragment = monthFragment;
        bottomNav.setSelectedItemId(R.id.nav_month);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_year) showFragment(yearFragment);
            else if (id == R.id.nav_month) showFragment(monthFragment);
            else if (id == R.id.nav_week) showFragment(weekFragment);
            else if (id == R.id.nav_agenda) showFragment(agendaFragment);
            return true;
        });

        // Permission
        checkCalendarPermission();
        findViewById(R.id.btnGrantPermission).setOnClickListener(v ->
                requestCalendarPermission());
    }

    private void showFragment(Fragment target) {
        if (activeFragment == target) return;
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.hide(activeFragment).show(target).commit();
        activeFragment = target;
    }

    // ---- Called by YearFragment when user taps a month ----
    @Override
    public void navigateToMonth(int year, int month) {
        monthFragment.setMonth(year, month);
        bottomNav.setSelectedItemId(R.id.nav_month);
        showFragment(monthFragment);
    }

    // ---- Permission handling ----
    private void checkCalendarPermission() {
        boolean granted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
        permissionBanner.setVisibility(granted ? View.GONE : View.VISIBLE);
    }

    private void requestCalendarPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR
                },
                REQUEST_CALENDAR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALENDAR) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            permissionBanner.setVisibility(granted ? View.GONE : View.VISIBLE);
            if (granted) {
                // Reload all fragments
                refreshAllFragments();
            }
        }
    }

    private void refreshAllFragments() {
        // Recreate to force reload — lightweight since fragments are small
        Calendar today = Calendar.getInstance();
        monthFragment.setMonth(today.get(Calendar.YEAR), today.get(Calendar.MONTH));
    }
}
