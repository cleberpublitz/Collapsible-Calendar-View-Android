package com.github.cleberpublitz.collapsiblecalendarview.widget

/**
 * Created by shrikanthravi on 07/03/18.
 */


import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.github.cleberpublitz.collapsiblecalendarview.R
import com.github.cleberpublitz.collapsiblecalendarview.data.CalendarAdapter
import com.github.cleberpublitz.collapsiblecalendarview.data.Day
import com.github.cleberpublitz.collapsiblecalendarview.data.Event
import com.github.cleberpublitz.collapsiblecalendarview.listener.OnSwipeTouchListener
import com.github.cleberpublitz.collapsiblecalendarview.view.ExpandIconView.LESS
import com.github.cleberpublitz.collapsiblecalendarview.view.ExpandIconView.MORE
import java.text.SimpleDateFormat
import java.util.Calendar.*
import java.util.Locale.getDefault

class CollapsibleCalendar : UICalendar {

    private var mAdapter: CalendarAdapter? = null
    private var mListener: CalendarListener? = null

    private var expanded = false

    private var mInitHeight = 0

    private val mHandler = Handler()
    private var mIsWaitingForUpdate = false

    private var mCurrentWeekIndex: Int = 0

    private val swipeTouchListener: OnSwipeTouchListener
        get() = object : OnSwipeTouchListener(context) {
            override fun onSwipeTop() {
                collapse(400)
            }

            override fun onSwipeLeft() {
                if (state == STATE_COLLAPSED)
                    nextWeek()
                else if (state == STATE_EXPANDED)
                    nextMonth()
            }

            override fun onSwipeRight() {
                if (state == STATE_COLLAPSED) {
                    prevWeek()
                } else if (state == STATE_EXPANDED) {
                    prevMonth()
                }
            }

            override fun onSwipeBottom() {
                expand(400)
            }
        }

    private val suitableRowIndex: Int
        get() = when {
            selectedItemPosition != -1 -> {
                val view = mAdapter!!.getView(selectedItemPosition)
                val row = view.parent as TableRow

                mTableBody.indexOfChild(row)
            }
            todayItemPosition != -1 -> {
                val view = mAdapter!!.getView(todayItemPosition)
                val row = view.parent as TableRow

                mTableBody.indexOfChild(row)
            }
            else -> 0
        }

    val year: Int
        get() = mAdapter!!.calendar.get(YEAR)

    val month: Int
        get() = mAdapter!!.calendar.get(MONTH)

    val selectedDay: Day
        get() {
            if (selectedItem == null) {
                val cal = getInstance()
                val day = cal.get(DAY_OF_MONTH)
                val month = cal.get(MONTH)
                val year = cal.get(YEAR)
                return Day(year, month + 1, day)
            }
            return Day(
                    selectedItem.year,
                    selectedItem.month,
                    selectedItem.day)
        }

    val selectedItemPosition: Int
        get() {
            var position = -1
            for (i in 0 until mAdapter!!.count) {
                val day = mAdapter!!.getItem(i)

                if (isSelectedDay(day)) {
                    position = i
                    break
                }
            }
            return position
        }

    val todayItemPosition: Int
        get() {
            var position = -1

            mAdapter!!.forEachIndexed { i, _, day ->
                if (isToday(day)) {
                    position = i
                    return@forEachIndexed
                }
            }

            return position
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun init(context: Context) {
        super.init(context)

        val cal = getInstance()
        val adapter = CalendarAdapter(context, cal)
        adapter.setEventDotSize(eventDotSize)
        setAdapter(adapter)

        // bind events
        mLayoutRoot.setOnTouchListener(swipeTouchListener)
        mBtnPrevMonth.setOnClickListener { prevMonth() }

        mBtnNextMonth.setOnClickListener { nextMonth() }

        mBtnPrevWeek.setOnClickListener { prevWeek() }

        mBtnNextWeek.setOnClickListener { nextWeek() }

        expandIconView.setState(MORE, true)

        expandIconView.setOnClickListener {
            if (expanded) {
                collapse(400)
            } else {
                expand(400)
            }
        }

        this.post { collapseTo(mCurrentWeekIndex) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        mInitHeight = mTableBody.measuredHeight

        if (mIsWaitingForUpdate) {
            redraw()
            mHandler.post { collapseTo(mCurrentWeekIndex) }
            mIsWaitingForUpdate = false
            mListener?.onDataUpdate()
        }
    }

    override fun redraw() {
        // redraw all views of week
        val rowWeek = mTableHead.getChildAt(0) as TableRow
        if (rowWeek != null) {
            for (i in 0 until rowWeek.childCount) {
                (rowWeek.getChildAt(i) as TextView).setTextColor(textColor)
            }
        }
        // redraw all views of day
        mAdapter?.forEach { v, d ->
            mAdapter?.onRedrawView(v, textColor) { txtDay ->
                if (isToday(d)) {
                    txtDay.setBackgroundDrawable(todayItemBackgroundDrawable)
                    txtDay.setTextColor(todayItemTextColor)
                }

                // set the selected item
                if (isSelectedDay(d)) {
                    txtDay.setBackgroundDrawable(selectedItemBackgroundDrawable)
                    txtDay.setTextColor(selectedItemTextColor)
                }
            }
        }
    }

    override fun reload() {
        if (mAdapter != null) {
            mAdapter!!.setEventDotSize(eventDotSize)
            mAdapter!!.refresh()

            // reset UI
            val dateFormat = SimpleDateFormat("MMM yyyy", getDefault())
            dateFormat.timeZone = mAdapter!!.calendar.timeZone
            mTxtTitle.text = dateFormat.format(mAdapter!!.calendar.time)
            mTableHead.removeAllViews()
            mTableBody.removeAllViews()

            var rowCurrent: TableRow

            // set day of week
            val dayOfWeekIds = intArrayOf(R.string.sunday, R.string.monday, R.string.tuesday, R.string.wednesday, R.string.thursday, R.string.friday, R.string.saturday)
            rowCurrent = TableRow(mContext)
            rowCurrent.layoutParams = TableLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            for (i in 0..6) {
                val view = mInflater.inflate(R.layout.layout_day_of_week, null)
                val txtDayOfWeek = view.findViewById<TextView>(R.id.txt_day_of_week)
                if (SDK_INT >= JELLY_BEAN_MR1) {
                    txtDayOfWeek.textLocale = getDefault()
                }
                txtDayOfWeek.setText(dayOfWeekIds[(i + firstDayOfWeek) % 7])
                view.layoutParams = TableRow.LayoutParams(0, WRAP_CONTENT, 1f)
                rowCurrent.addView(view)
            }
            mTableHead.addView(rowCurrent)

            // set day view
            for (i in 0 until mAdapter!!.count) {

                if (i % 7 == 0) {
                    rowCurrent = TableRow(mContext)
                    rowCurrent.layoutParams = TableLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    mTableBody.addView(rowCurrent)
                }
                mAdapter!!.setOnClickListener(i) { v, d -> onItemClicked(v, d) }
                rowCurrent.addView(mAdapter!!.getView(i).apply {
                    layoutParams = TableRow.LayoutParams(0, WRAP_CONTENT, 1f)
                    setOnTouchListener(swipeTouchListener)
                })
            }

            redraw()
            mIsWaitingForUpdate = true
        }
    }

    fun onItemClicked(view: View, day: Day) {
        select(day)

        val cal = mAdapter!!.calendar

        val newYear = day.year
        val newMonth = day.month
        val oldYear = cal.get(YEAR)
        val oldMonth = cal.get(MONTH)
        if (newMonth != oldMonth) {
            cal.set(day.year, day.month, 1)

            if (newYear > oldYear || newMonth > oldMonth) {
                mCurrentWeekIndex = 0
            }
            if (newYear < oldYear || newMonth < oldMonth) {
                mCurrentWeekIndex = -1
            }
            mListener?.onMonthChange()
            reload()
        }

        mListener?.onItemClick(view)
    }

    // public methods
    fun setAdapter(adapter: CalendarAdapter) {
        mAdapter = adapter
        adapter.setFirstDayOfWeek(firstDayOfWeek)

        reload()

        // init week
        mCurrentWeekIndex = suitableRowIndex
    }

    fun addEventTag(numYear: Int, numMonth: Int, numDay: Int, color: Int = eventColor,
                    borderColor: Int? = null) {
        mAdapter!!.addEvent(Event(numYear, numMonth, numDay, color, borderColor))

        reload()
    }

    fun prevMonth() {
        val cal = mAdapter!!.calendar
        if (cal.get(MONTH) == cal.getActualMinimum(MONTH)) {
            cal.set(cal.get(YEAR) - 1, cal.getActualMaximum(MONTH), 1)
        } else {
            cal.set(MONTH, cal.get(MONTH) - 1)
        }
        reload()
        mListener?.onMonthChange()
    }

    fun nextMonth() {
        val cal = mAdapter!!.calendar
        if (cal.get(MONTH) == cal.getActualMaximum(MONTH)) {
            cal.set(cal.get(YEAR) + 1, cal.getActualMinimum(MONTH), 1)
        } else {
            cal.set(MONTH, cal.get(MONTH) + 1)
        }
        reload()
        mListener?.onMonthChange()
    }

    fun prevWeek() {
        if (mCurrentWeekIndex - 1 < 0) {
            mCurrentWeekIndex = -1
            prevMonth()
        } else {
            mCurrentWeekIndex--
            collapseTo(mCurrentWeekIndex)
        }
    }

    fun nextWeek() {
        if (mCurrentWeekIndex + 1 >= mTableBody.childCount) {
            mCurrentWeekIndex = 0
            nextMonth()
        } else {
            mCurrentWeekIndex++
            collapseTo(mCurrentWeekIndex)
        }
    }

    fun isSelectedDay(day: Day?): Boolean {
        return (day != null
                && selectedItem != null
                && day.year == selectedItem.year
                && day.month == selectedItem.month
                && day.day == selectedItem.day)
    }

    fun isToday(day: Day?): Boolean {
        val todayCal = getInstance()
        return (day != null
                && day.year == todayCal.get(YEAR)
                && day.month == todayCal.get(MONTH)
                && day.day == todayCal.get(DAY_OF_MONTH))
    }

    fun collapse(duration: Int) {
        if (state == STATE_EXPANDED) {
            state = STATE_PROCESSING

            mLayoutBtnGroupMonth.visibility = View.GONE
            mLayoutBtnGroupWeek.visibility = View.VISIBLE
            mBtnPrevWeek.isClickable = false
            mBtnNextWeek.isClickable = false

            val index = suitableRowIndex
            mCurrentWeekIndex = index

            val currentHeight = mInitHeight
            val targetHeight = mTableBody.getChildAt(index).measuredHeight
            var tempHeight = 0
            for (i in 0 until index) {
                tempHeight += mTableBody.getChildAt(i).measuredHeight
            }
            val topHeight = tempHeight

            val anim = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {

                    mScrollViewBody.layoutParams.height = if (interpolatedTime == 1f)
                        targetHeight
                    else
                        currentHeight - ((currentHeight - targetHeight) * interpolatedTime).toInt()
                    mScrollViewBody.requestLayout()

                    if (mScrollViewBody.measuredHeight < topHeight + targetHeight) {
                        val position = topHeight + targetHeight - mScrollViewBody.measuredHeight
                        mScrollViewBody.smoothScrollTo(0, position)
                    }

                    if (interpolatedTime == 1f) {
                        state = STATE_COLLAPSED

                        mBtnPrevWeek.isClickable = true
                        mBtnNextWeek.isClickable = true
                    }
                }
            }
            anim.duration = duration.toLong()
            startAnimation(anim)
        }

        expandIconView.setState(MORE, true)
    }

    private fun collapseTo(index: Int) {
        var index = index
        if (state == STATE_COLLAPSED) {
            if (index == -1) {
                index = mTableBody.childCount - 1
            }
            mCurrentWeekIndex = index

            val targetHeight = mTableBody.getChildAt(index).measuredHeight
            var tempHeight = 0
            for (i in 0 until index) {
                tempHeight += mTableBody.getChildAt(i).measuredHeight
            }
            val topHeight = tempHeight

            mScrollViewBody.layoutParams.height = targetHeight
            mScrollViewBody.requestLayout()

            mHandler.post { mScrollViewBody.smoothScrollTo(0, topHeight) }

            mListener?.onWeekChange(mCurrentWeekIndex)
        }
    }

    fun expand(duration: Int) {
        if (state == STATE_COLLAPSED) {
            state = STATE_PROCESSING

            mLayoutBtnGroupMonth.visibility = View.VISIBLE
            mLayoutBtnGroupWeek.visibility = View.GONE
            mBtnPrevMonth.isClickable = false
            mBtnNextMonth.isClickable = false

            val currentHeight = mScrollViewBody.measuredHeight
            val targetHeight = mInitHeight

            val anim = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {

                    mScrollViewBody.layoutParams.height = if (interpolatedTime == 1f)
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    else
                        currentHeight - ((currentHeight - targetHeight) * interpolatedTime).toInt()
                    mScrollViewBody.requestLayout()

                    if (interpolatedTime == 1f) {
                        state = STATE_EXPANDED

                        mBtnPrevMonth.isClickable = true
                        mBtnNextMonth.isClickable = true
                    }
                }
            }
            anim.duration = duration.toLong()
            startAnimation(anim)
        }

        expandIconView.setState(LESS, true)
    }

    override fun setState(state: Int) {
        super.setState(state)
        if (state == STATE_COLLAPSED) {
            expanded = false
        }
        if (state == STATE_EXPANDED) {
            expanded = true
        }
    }

    fun select(day: Day) {
        selectedItem = Day(day.year, day.month, day.day)

        redraw()

        mListener?.onDaySelect()
    }

    fun setStateWithUpdateUI(state: Int) {
        setState(state)

        if (state != state) {
            mIsWaitingForUpdate = true
            requestLayout()
        }
    }

    // callback
    fun setCalendarListener(listener: CalendarListener) {
        mListener = listener
    }

    interface CalendarListener {

        // triggered when a day is selected programmatically or clicked by user.
        fun onDaySelect()

        // triggered only when the views of day on calendar are clicked by user.
        fun onItemClick(v: View)

        // triggered when the data of calendar are updated by changing month or adding events.
        fun onDataUpdate()

        // triggered when the month are changed.
        fun onMonthChange()

        // triggered when the week position are changed.
        fun onWeekChange(position: Int)
    }
}

