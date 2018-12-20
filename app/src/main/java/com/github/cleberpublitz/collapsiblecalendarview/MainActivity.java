package com.github.cleberpublitz.collapsiblecalendarview;

import android.os.Bundle;
import android.view.View;

import com.github.cleberpublitz.collapsiblecalendarview.widget.CollapsibleCalendar;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

import static android.graphics.Color.BLUE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        if (SDK_INT >= LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.google_red));
        }

        CollapsibleCalendar collapsibleCalendar = findViewById(R.id.collapsibleCalendarView);
        Calendar today=new GregorianCalendar();
        collapsibleCalendar.addEventTag(today.get(YEAR),today.get(MONTH),today.get(DAY_OF_MONTH));
        today.add(DATE,1);
        collapsibleCalendar.addEventTag(today.get(YEAR),today.get(MONTH),today.get(DAY_OF_MONTH),BLUE);

        System.out.println("Testing date "+collapsibleCalendar.getSelectedDay().getDay()+"/"+collapsibleCalendar.getSelectedDay().getMonth()+"/"+collapsibleCalendar.getSelectedDay().getYear());
        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {

            }

            @Override
            public void onItemClick(View v) {

            }

            @Override
            public void onDataUpdate() {

            }

            @Override
            public void onMonthChange() {

            }

            @Override
            public void onWeekChange(int position) {

            }
        });


    }
}
