package com.github.cleberpublitz.collapsiblecalendarview.data

import android.content.Context
import android.graphics.Color.TRANSPARENT
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import com.github.cleberpublitz.collapsiblecalendarview.R
import com.github.cleberpublitz.collapsiblecalendarview.widget.UICalendar.EVENT_DOT_BIG
import com.github.cleberpublitz.collapsiblecalendarview.widget.UICalendar.EVENT_DOT_SMALL
import java.util.*
import java.util.Calendar.*

/**
 * Created by shrikanthravi on 06/03/18.
 */

open class CalendarAdapter(context: Context, cal: Calendar) {
    private var mFirstDayOfWeek = 0
    val calendar: Calendar = cal.clone() as Calendar
    private val mInflater: LayoutInflater
    private var mEventDotSize = EVENT_DOT_BIG

    private var mViewMap: MutableMap<Day, View> = mutableMapOf()
    private var mEventMap: MutableMap<Day, MutableList<Event>> = mutableMapOf()

    // public methods
    val count: Int
        get() = mViewMap.size

    init {
        this.calendar.set(DAY_OF_MONTH, 1)
        mInflater = LayoutInflater.from(context)

        refresh()
    }

    fun getItem(position: Int): Day = mViewMap.keys.elementAt(position)

    fun getView(position: Int): View = mViewMap.values.elementAt(position)

    fun forEach(action: (View, Day) -> Unit) {
        for (m in mViewMap.iterator()) action(m.value, m.key)
    }

    fun forEachIndexed(action: (index: Int, View, Day) -> Unit) {
        mViewMap.entries.forEachIndexed { i, m -> action(i, m.value, m.key) }
    }

    fun setFirstDayOfWeek(firstDayOfWeek: Int) {
        mFirstDayOfWeek = firstDayOfWeek
    }

    fun setEventDotSize(eventDotSize: Int) {
        mEventDotSize = eventDotSize
    }

    fun addEvent(event: Event) {
        val day = Day(event.year, event.month, event.day)
        if (mEventMap.contains(day)) {
            if(mEventMap[day]?.isEmpty() == true) mEventMap[day] = mutableListOf()
        } else mEventMap[day] = mutableListOf()
        mEventMap[day]?.add(event)
    }

    open fun onCreateView(inflater: LayoutInflater, day: Day, eventList: List<Event>, eventDotSize: Int): View {
        val view: View = if (eventDotSize == EVENT_DOT_SMALL)
            inflater.inflate(R.layout.day_layout_small, null)
        else
            inflater.inflate(R.layout.day_layout, null)

        val txtDay = view.findViewById<View>(R.id.txt_day) as TextView
        val imgEventTag = view.findViewById<View>(R.id.img_event_tag) as ImageView

        txtDay.text = day.day.toString()
        if (day.month != calendar.get(MONTH)) {
            txtDay.alpha = 0.3f
        }

        for (j in eventList.indices) {
            val event = eventList[j]
            if (day.year == event.year
                    && day.month == event.month
                    && day.day == event.day) {
                imgEventTag.visibility = VISIBLE
                imgEventTag.setColorFilter(event.color, PorterDuff.Mode.SRC_ATOP)
            }
        }

        return view
    }

    open fun onRedrawView(view: View, @ColorInt textColorDefault: Int, applyConfigs: (TextView) -> Unit) {
        val txtDay = view.findViewById<TextView>(R.id.txt_day)
        txtDay.setBackgroundColor(TRANSPARENT)
        txtDay.setTextColor(textColorDefault)

        applyConfigs(txtDay)
    }

    open fun setOnClickListener(l: (View, Day) -> Unit) {
        for( i in mViewMap.iterator()) { i.value.setOnClickListener { v -> l(v, i.key) } }
    }

    internal fun setOnClickListener(position: Int, l: (View, Day) -> Unit) {
        mViewMap.values.elementAt(position).setOnClickListener { v -> l(v, mViewMap.keys.elementAt(position)) }
    }

    fun refresh() {
        // clear data
        mViewMap.clear()

        // set calendar
        val year = calendar.get(YEAR)
        val month = calendar.get(MONTH)

        calendar.set(year, month, 1)

        val lastDayOfMonth = calendar.getActualMaximum(DAY_OF_MONTH)
        val firstDayOfWeek = calendar.get(DAY_OF_WEEK) - 1

        // generate day list
        val offset = 0 - (firstDayOfWeek - mFirstDayOfWeek) + 1
        val length = Math.ceil(((lastDayOfMonth - offset + 1).toFloat() / 7).toDouble()).toInt() * 7
        for (i in offset until length + offset) {
            val numYear: Int
            val numMonth: Int
            val numDay: Int

            val tempCal = getInstance()
            when {
                i <= 0 -> { // prev month
                    if (month == 0) {
                        numYear = year - 1
                        numMonth = 11
                    } else {
                        numYear = year
                        numMonth = month - 1
                    }
                    tempCal.set(numYear, numMonth, 1)
                    numDay = tempCal.getActualMaximum(DAY_OF_MONTH) + i
                }
                i > lastDayOfMonth -> { // next month
                    if (month == 11) {
                        numYear = year + 1
                        numMonth = 0
                    } else {
                        numYear = year
                        numMonth = month + 1
                    }
                    tempCal.set(numYear, numMonth, 1)
                    numDay = i - lastDayOfMonth
                }
                else -> {
                    numYear = year
                    numMonth = month
                    numDay = i
                }
            }

            val day = Day(numYear, numMonth, numDay)
            mViewMap[day] = onCreateView(mInflater, day, mEventMap[day] ?: listOf(), mEventDotSize)
        }
    }
}
