package org.fossify.commons.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.fossify.commons.extensions.applyFontToTextView
import org.fossify.commons.extensions.pillDrawable
import org.fossify.commons.helpers.UiRole
import org.fossify.commons.helpers.uiColor
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * shiroikuma (sk18): a date picker drawn entirely by us, because the platform's takes the colours of its
 * day grid from theme attributes that have no setter. Everything here comes from a role: the month and
 * year heading, the arrow pills, the weekday names, the days, the ring on the chosen day (an outline —
 * never a filled disc) and the short line under today.
 *
 * The year row steps by one and by ten, so a birth year decades back is a few taps away. The API mirrors
 * the platform DatePicker's: [year], [month] (0-based), [dayOfMonth], [updateDate].
 */
class FamilyDatePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {
    var year = Calendar.getInstance().get(Calendar.YEAR)
        private set
    var month = Calendar.getInstance().get(Calendar.MONTH)
        private set
    var dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        private set

    /** Called whenever the chosen date changes. */
    var onDateChanged: ((year: Int, month: Int, dayOfMonth: Int) -> Unit)? = null

    private val density = resources.displayMetrics.density
    private val monthLabel = heading()
    private val yearLabel = heading()
    private val grid = DayGrid(context)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        addView(row(arrow("‹") { shiftMonth(-1) }, monthLabel, arrow("›") { shiftMonth(1) }))
        addView(
            row(
                arrow("«") { shiftYear(-YEAR_LEAP) }, arrow("‹") { shiftYear(-1) }, yearLabel,
                arrow("›") { shiftYear(1) }, arrow("»") { shiftYear(YEAR_LEAP) },
            )
        )
        addView(grid, LayoutParams(dp(CELL_DP * DAYS_IN_WEEK), LayoutParams.WRAP_CONTENT))
        refresh()
    }

    fun updateDate(year: Int, month: Int, dayOfMonth: Int) {
        this.year = year.coerceIn(MIN_YEAR, MAX_YEAR)
        this.month = month.coerceIn(Calendar.JANUARY, Calendar.DECEMBER)
        this.dayOfMonth = dayOfMonth.coerceIn(1, daysIn(this.year, this.month))
        refresh()
    }

    private fun shiftMonth(by: Int) {
        val total = year * MONTHS + month + by
        updateDate(Math.floorDiv(total, MONTHS), Math.floorMod(total, MONTHS), dayOfMonth)
        onDateChanged?.invoke(year, month, dayOfMonth)
    }

    private fun shiftYear(by: Int) {
        updateDate(year + by, month, dayOfMonth)
        onDateChanged?.invoke(year, month, dayOfMonth)
    }

    private fun refresh() {
        monthLabel.text = DateFormatSymbols.getInstance().months[month]
        yearLabel.text = year.toString()
        grid.invalidate()
    }

    private fun daysIn(year: Int, month: Int) = GregorianCalendar(year, month, 1).getActualMaximum(Calendar.DAY_OF_MONTH)

    private fun dp(value: Float) = (value * density).toInt()

    private fun row(vararg views: View) = LinearLayout(context).apply {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val gap = dp(ROW_GAP_DP)
        setPadding(0, gap, 0, gap)
        views.forEach { view ->
            val weight = if (view is TextView && view.tag == HEADING_TAG) 1f else 0f
            addView(view, LayoutParams(if (weight > 0f) 0 else LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, weight))
        }
        layoutParams = LayoutParams(dp(CELL_DP * DAYS_IN_WEEK), LayoutParams.WRAP_CONTENT)
    }

    private fun heading() = TextView(context).apply {
        tag = HEADING_TAG
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, HEADING_SP)
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(context.uiColor(UiRole.PICKER_HEADING))
        context.applyFontToTextView(this)
    }

    // A small outlined pill, like every other action in the family.
    private fun arrow(glyph: String, onClick: () -> Unit) = TextView(context).apply {
        text = glyph
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, ARROW_SP)
        setTextColor(context.uiColor(UiRole.PICKER_ARROW))
        background = context.pillDrawable(context.uiColor(UiRole.PICKER_ARROW_FRAME), context.uiColor(UiRole.DIALOG_GROUND), false)
        minWidth = dp(ARROW_WIDTH_DP)
        val v = dp(ARROW_PAD_DP)
        setPadding(v, v / 2, v, v / 2)
        (layoutParams as? MarginLayoutParams)?.marginEnd = v
        setOnClickListener { onClick() }
    }.also { view ->
        view.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            val side = dp(ARROW_GAP_DP)
            marginStart = side
            marginEnd = side
        }
    }

    /** The weekday names and the six weeks of the shown month. */
    private inner class DayGrid(context: Context) : View(context) {
        private val cell = dp(CELL_DP)
        private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
        private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        private val firstWeekday = Calendar.getInstance().firstDayOfWeek
        private val weekdayNames = DateFormatSymbols.getInstance().shortWeekdays

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(cell * DAYS_IN_WEEK, cell * (WEEKS + 1))
        }

        // How many cells lie before the 1st in the first week.
        private fun lead(): Int {
            val weekdayOfFirst = GregorianCalendar(year, month, 1).get(Calendar.DAY_OF_WEEK)
            return Math.floorMod(weekdayOfFirst - firstWeekday, DAYS_IN_WEEK)
        }

        override fun onDraw(canvas: Canvas) {
            val baselineShift = { paint: Paint -> -(paint.descent() + paint.ascent()) / 2f }
            text.typeface = Typeface.DEFAULT
            text.textSize = WEEKDAY_SP * resources.displayMetrics.scaledDensity
            text.color = context.uiColor(UiRole.PICKER_WEEKDAY)
            for (column in 0 until DAYS_IN_WEEK) {
                val weekday = Math.floorMod(firstWeekday - 1 + column, DAYS_IN_WEEK) + 1
                canvas.drawText(weekdayNames[weekday], cell * (column + 0.5f), cell * 0.5f + baselineShift(text), text)
            }

            val today = Calendar.getInstance()
            val showsToday = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
            val dayColor = context.uiColor(UiRole.PICKER_DAY)
            val chosenColor = context.uiColor(UiRole.PICKER_DAY_CHOSEN)
            text.textSize = DAY_SP * resources.displayMetrics.scaledDensity
            val lead = lead()
            for (day in 1..daysIn(year, month)) {
                val index = lead + day - 1
                val cx = cell * (index % DAYS_IN_WEEK + 0.5f)
                val cy = cell * (index / DAYS_IN_WEEK + 1.5f)
                val chosen = day == dayOfMonth
                text.color = if (chosen) chosenColor else dayColor
                text.typeface = if (chosen) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                canvas.drawText(day.toString(), cx, cy + baselineShift(text), text)
                if (chosen) {
                    line.color = chosenColor
                    line.strokeWidth = RING_DP * density
                    canvas.drawCircle(cx, cy, cell * RING_RADIUS, line)
                }
                if (showsToday && day == today.get(Calendar.DAY_OF_MONTH)) {
                    line.color = context.uiColor(UiRole.PICKER_TODAY)
                    line.strokeWidth = TODAY_LINE_DP * density
                    val half = cell * TODAY_LINE_HALF
                    val y = cy + cell * TODAY_LINE_DROP
                    canvas.drawLine(cx - half, y, cx + half, y, line)
                }
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_UP) {
                val column = (event.x / cell).toInt().coerceIn(0, DAYS_IN_WEEK - 1)
                val week = (event.y / cell).toInt() - 1
                val day = week * DAYS_IN_WEEK + column - lead() + 1
                if (week >= 0 && day in 1..daysIn(year, month)) {
                    dayOfMonth = day
                    invalidate()
                    performClick()
                    onDateChanged?.invoke(year, month, dayOfMonth)
                }
            }
            return true
        }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }
    }

    private companion object {
        const val HEADING_TAG = "heading"
        const val DAYS_IN_WEEK = 7
        const val WEEKS = 6
        const val MONTHS = 12
        const val YEAR_LEAP = 10
        const val MIN_YEAR = 1
        const val MAX_YEAR = 9999
        const val CELL_DP = 40f
        const val ROW_GAP_DP = 4f
        const val ARROW_WIDTH_DP = 36f
        const val ARROW_PAD_DP = 8f
        const val ARROW_GAP_DP = 3f
        const val HEADING_SP = 18f
        const val ARROW_SP = 18f
        const val WEEKDAY_SP = 12f
        const val DAY_SP = 15f
        const val RING_DP = 2f
        const val RING_RADIUS = 0.42f
        const val TODAY_LINE_DP = 1.5f
        const val TODAY_LINE_HALF = 0.18f
        const val TODAY_LINE_DROP = 0.3f
    }
}
