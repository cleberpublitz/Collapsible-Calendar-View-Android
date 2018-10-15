package com.github.cleberpublitz.collapsiblecalendarview.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.github.cleberpublitz.collapsiblecalendarview.R
import com.github.cleberpublitz.collapsiblecalendarview.data.Day
import com.github.cleberpublitz.collapsiblecalendarview.view.ExpandIconView
import com.github.cleberpublitz.collapsiblecalendarview.view.LockScrollView


abstract class UICalendar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    protected lateinit var mContext: Context
    protected lateinit var mInflater: LayoutInflater

    // UI
    protected lateinit var mLayoutRoot: LinearLayout
    protected lateinit var mTxtTitle: TextView
    protected lateinit var mTableHead: TableLayout
    protected lateinit var mScrollViewBody: LockScrollView
    protected lateinit var mTableBody: TableLayout
    protected lateinit var mLayoutBtnGroupMonth: RelativeLayout
    protected lateinit var mLayoutBtnGroupWeek: RelativeLayout
    protected lateinit var mBtnPrevMonth: ImageView
    protected lateinit var mBtnNextMonth: ImageView
    protected lateinit var mBtnPrevWeek: ImageView
    protected lateinit var mBtnNextWeek: ImageView
    protected lateinit var expandIconView: ExpandIconView

    // Attributes
    var isShowWeek = true
        set(showWeek) {
            field = showWeek

            mTableHead.visibility = if(showWeek) View.VISIBLE else GONE
        }
    var firstDayOfWeek = SUNDAY
        set(firstDayOfWeek) {
            field = firstDayOfWeek
            reload()
        }
    var state = STATE_COLLAPSED
        set(state) {
            field = state

            if (this.state == STATE_EXPANDED) {
                mLayoutBtnGroupMonth.visibility = View.VISIBLE
                mLayoutBtnGroupWeek.visibility = View.GONE
            }
            if (this.state == STATE_COLLAPSED) {
                mLayoutBtnGroupMonth.visibility = View.GONE
                mLayoutBtnGroupWeek.visibility = View.VISIBLE
            }
        }

    var textColor = Color.BLACK
        set(textColor) {
            field = textColor
            redraw()

            mTxtTitle.setTextColor(this.textColor)
        }
    var primaryColor = Color.WHITE
        set(primaryColor) {
            field = primaryColor
            redraw()

            mLayoutRoot.setBackgroundColor(this.primaryColor)
        }

    var todayItemTextColor = Color.BLACK
        set(todayItemTextColor) {
            field = todayItemTextColor
            redraw()
        }
    var todayItemBackgroundDrawable = resources.getDrawable(R.drawable.circle_black_stroke_background)
        set(todayItemBackgroundDrawable) {
            field = todayItemBackgroundDrawable
            redraw()
        }
    var selectedItemTextColor = Color.WHITE
        set(selectedItemTextColor) {
            field = selectedItemTextColor
            redraw()
        }
    var selectedItemBackgroundDrawable = resources.getDrawable(R.drawable.circle_black_solid_background)
        set(selectedItemBackground) {
            field = selectedItemBackground
            redraw()
        }

    var buttonLeftDrawable = resources.getDrawable(R.drawable.left_icon)
        set(buttonLeftDrawable) {
            field = buttonLeftDrawable
            mBtnPrevMonth.setImageDrawable(buttonLeftDrawable)
            mBtnPrevWeek.setImageDrawable(buttonLeftDrawable)
        }
    var buttonRightDrawable = resources.getDrawable(R.drawable.right_icon)
        set(buttonRightDrawable) {
            field = buttonRightDrawable
            mBtnNextMonth.setImageDrawable(buttonRightDrawable)
            mBtnNextWeek.setImageDrawable(buttonRightDrawable)
        }

    var selectedItem: Day? = null

    private var mButtonLeftDrawableTintColor = Color.BLACK
    private var mButtonRightDrawableTintColor = Color.BLACK

    private var mExpandIconColor = Color.BLACK
    var eventColor = Color.BLACK
        private set(eventColor) {
            field = eventColor
            redraw()

        }

    var eventDotSize = EVENT_DOT_BIG
        private set(eventDotSize) {
            field = eventDotSize
            redraw()

        }

    init {

        this.init(context)
        val attributes = context.theme.obtainStyledAttributes(
                attrs, R.styleable.UICalendar, defStyleAttr, 0)
        setAttributes(attributes)
        attributes.recycle()
    }

    protected abstract fun redraw()
    protected abstract fun reload()

    protected open fun init(context: Context) {
        mContext = context
        mInflater = LayoutInflater.from(mContext)

        // load rootView from xml
        val rootView = mInflater.inflate(R.layout.widget_collapsible_calendarview, this, true)

        // init UI
        mLayoutRoot = rootView.findViewById(R.id.layout_root)
        mTxtTitle = rootView.findViewById(R.id.txt_title)

        mTableHead = rootView.findViewById(R.id.table_head)
        mScrollViewBody = rootView.findViewById(R.id.scroll_view_body)
        mTableBody = rootView.findViewById(R.id.table_body)
        mLayoutBtnGroupMonth = rootView.findViewById(R.id.layout_btn_group_month)
        mLayoutBtnGroupWeek = rootView.findViewById(R.id.layout_btn_group_week)
        mBtnPrevMonth = rootView.findViewById(R.id.btn_prev_month)
        mBtnNextMonth = rootView.findViewById(R.id.btn_next_month)
        mBtnPrevWeek = rootView.findViewById(R.id.btn_prev_week)
        mBtnNextWeek = rootView.findViewById(R.id.btn_next_week)
        expandIconView = rootView.findViewById(R.id.expandIcon)


    }

    protected fun setAttributes(attrs: TypedArray) {
        // set attributes by the values from XML
        //setStyle(attrs.getInt(R.styleable.UICalendar_style, mStyle));
        isShowWeek = attrs.getBoolean(R.styleable.UICalendar_showWeek, isShowWeek)
        firstDayOfWeek = attrs.getInt(R.styleable.UICalendar_firstDayOfWeek, firstDayOfWeek)
        state = attrs.getInt(R.styleable.UICalendar_state, state)

        textColor = attrs.getColor(R.styleable.UICalendar_textColor, textColor)
        primaryColor = attrs.getColor(R.styleable.UICalendar_primaryColor, primaryColor)

        eventColor = attrs.getColor(R.styleable.UICalendar_eventColor, eventColor)
        eventDotSize = attrs.getInt(R.styleable.UICalendar_eventDotSize, eventDotSize)

        todayItemTextColor = attrs.getColor(
                R.styleable.UICalendar_todayItem_textColor, todayItemTextColor)
        var todayItemBackgroundDrawable = attrs.getDrawable(R.styleable.UICalendar_todayItem_background)
        todayItemBackgroundDrawable = todayItemBackgroundDrawable ?: this.todayItemBackgroundDrawable

        selectedItemTextColor = attrs.getColor(
                R.styleable.UICalendar_selectedItem_textColor, selectedItemTextColor)
        var selectedItemBackgroundDrawable = attrs.getDrawable(R.styleable.UICalendar_selectedItem_background)
        selectedItemBackgroundDrawable = selectedItemBackgroundDrawable ?: this.selectedItemBackgroundDrawable

        var buttonLeftDrawable = attrs.getDrawable(R.styleable.UICalendar_buttonLeft_drawable)
        buttonLeftDrawable = buttonLeftDrawable ?: this.buttonLeftDrawable

        var buttonRightDrawable = attrs.getDrawable(R.styleable.UICalendar_buttonRight_drawable)
        buttonRightDrawable = buttonRightDrawable ?: this.buttonRightDrawable

        setButtonLeftDrawableTintColor(attrs.getColor(R.styleable.UICalendar_buttonLeft_drawableTintColor, mButtonLeftDrawableTintColor))
        setButtonRightDrawableTintColor(attrs.getColor(R.styleable.UICalendar_buttonRight_drawableTintColor, mButtonRightDrawableTintColor))
        setExpandIconColor(attrs.getColor(R.styleable.UICalendar_expandIconColor, mExpandIconColor))
        val selectedItem: Day? = null
    }

    fun setButtonLeftDrawableTintColor(color: Int) {
        this.mButtonLeftDrawableTintColor = color
        mBtnPrevMonth.drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        mBtnPrevWeek.drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        redraw()
    }

    fun setButtonRightDrawableTintColor(color: Int) {

        this.mButtonRightDrawableTintColor = color
        mBtnNextMonth.drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        mBtnNextWeek.drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        redraw()
    }

    fun setExpandIconColor(color: Int) {
        this.mExpandIconColor = color
        expandIconView.setColor(color)
    }

    companion object {

        // Day of Week
        const val SUNDAY = 0
        const val MONDAY = 1
        const val TUESDAY = 2
        const val WEDNESDAY = 3
        const val THURSDAY = 4
        const val FRIDAY = 5
        const val SATURDAY = 6
        // State
        const val STATE_EXPANDED = 0
        const val STATE_COLLAPSED = 1
        const val STATE_PROCESSING = 2
        const val EVENT_DOT_BIG = 0
        const val EVENT_DOT_SMALL = 1
    }
}
