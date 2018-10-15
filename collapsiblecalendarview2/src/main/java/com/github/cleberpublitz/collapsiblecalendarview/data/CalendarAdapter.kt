package com.github.cleberpublitz.collapsiblecalendarview.data

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.cleberpublitz.collapsiblecalendarview.R
import com.github.cleberpublitz.collapsiblecalendarview.widget.UICalendar
import com.github.cleberpublitz.collapsiblecalendarview.widget.UICalendar.EVENT_DOT_SMALL
import java.util.*
import java.util.Calendar.*

/**
 * Created by shrikanthravi on 06/03/18.
 */

class CalendarAdapter(context: Context, cal: Calendar) {
    private var mFirstDayOfWeek = 0
    val calendar: Calendar = cal.clone() as Calendar
    private val mInflater: LayoutInflater
    private var mEventDotSize = UICalendar.EVENT_DOT_BIG

    private var mItemList: MutableList<Day> = ArrayList()
    private var mViewList: MutableList<View> = ArrayList()
    private var mEventList: MutableList<Event> = ArrayList()

    // public methods
    val count: Int
        get() = mItemList.size

    init {
        this.calendar.set(DAY_OF_MONTH, 1)
        mInflater = LayoutInflater.from(context)

        refresh()
    }

    fun getItem(position: Int): Day {
        return mItemList[position]
    }

    fun getView(position: Int): View {
        return mViewList[position]
    }

    fun setFirstDayOfWeek(firstDayOfWeek: Int) {
        mFirstDayOfWeek = firstDayOfWeek
    }

    fun setEventDotSize(eventDotSize: Int) {
        mEventDotSize = eventDotSize
    }

    fun addEvent(event: Event) {
        mEventList.add(event)
    }

    fun refresh() {
        // clear data
        mItemList.clear()
        mViewList.clear()

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
            val view: View = if (mEventDotSize == EVENT_DOT_SMALL)
                mInflater.inflate(R.layout.day_layout_small, null)
            else
                mInflater.inflate(R.layout.day_layout, null)

            val txtDay = view.findViewById<View>(R.id.txt_day) as TextView
            val imgEventTag = view.findViewById<View>(R.id.img_event_tag) as ImageView

            txtDay.text = day.day.toString()
            if (day.month != calendar.get(MONTH)) {
                txtDay.alpha = 0.3f
            }

            for (j in mEventList.indices) {
                val event = mEventList[j]
                if (day.year == event.year
                        && day.month == event.month
                        && day.day == event.day) {
                    imgEventTag.visibility = View.VISIBLE
                    imgEventTag.setColorFilter(event.color, PorterDuff.Mode.SRC_ATOP)
                }
            }

            mItemList.add(day)
            mViewList.add(view)
        }
    }
}
