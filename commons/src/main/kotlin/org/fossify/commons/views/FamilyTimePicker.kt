package org.fossify.commons.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.fossify.commons.extensions.applyFontToTextView
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.pillDrawable
import org.fossify.commons.helpers.UiRole
import org.fossify.commons.helpers.uiColor
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

/**
 * shiroikuma (sk19): a time picker drawn entirely by us, the counterpart of [FamilyDatePicker] — the
 * platform's clock face and header take their colours from theme attributes that have no setter.
 *
 * Two columns, hour and minute, each with an arrow pill above and below its digits; a held arrow keeps
 * stepping, the minutes by five once it runs. A tap on the minutes' digits rounds them to the next five.
 * In the 12-hour form an AM / PM pill sits at the end. The API mirrors the platform TimePicker's:
 * [hour] (0–23), [minute], [setTime], [is24HourView], [onTimeChanged].
 */
class FamilyTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {
    /** The hour of the day, 0–23, whatever the form shown. */
    var hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        private set
    var minute = Calendar.getInstance().get(Calendar.MINUTE)
        private set

    var is24HourView = DateFormat.is24HourFormat(context)
        set(value) {
            field = value
            refresh()
        }

    /** Called whenever the chosen time changes. */
    var onTimeChanged: ((hour: Int, minute: Int) -> Unit)? = null

    private val density = resources.displayMetrics.density
    private val hourDigits = digits()
    private val minuteDigits = digits()
    private val amPm = pill("") { shiftHour(HOURS / 2) }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        addView(column(hourDigits, { shiftHour(1) }, { shiftHour(-1) }, { 1 }))
        addView(separator())
        // a held minute arrow goes by five, snapped to a multiple of five, once it repeats
        addView(column(minuteDigits, { shiftMinute(it) }, { shiftMinute(-it) }, { repeat -> if (repeat) MINUTE_LEAP else 1 }))
        addView(amPm, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply { marginStart = dp(AM_PM_GAP_DP) })
        minuteDigits.setOnClickListener {
            shiftMinute(MINUTE_LEAP - minute % MINUTE_LEAP)
        }
        refresh()
    }

    fun setTime(hour: Int, minute: Int) {
        this.hour = Math.floorMod(hour, HOURS)
        this.minute = Math.floorMod(minute, MINUTES)
        refresh()
    }

    private fun shiftHour(by: Int) {
        setTime(hour + by, minute)
        onTimeChanged?.invoke(hour, minute)
    }

    // The minutes wrap inside their hour: stepping past :59 does not move the hour, as on the platform's wheels.
    private fun shiftMinute(by: Int) {
        val snapped = if (Math.abs(by) == MINUTE_LEAP && minute % MINUTE_LEAP != 0) {
            if (by > 0) minute + (MINUTE_LEAP - minute % MINUTE_LEAP) else minute - minute % MINUTE_LEAP
        } else {
            minute + by
        }
        setTime(hour, snapped)
        onTimeChanged?.invoke(hour, minute)
    }

    private fun refresh() {
        val shownHour = when {
            is24HourView -> hour
            hour % (HOURS / 2) == 0 -> HOURS / 2
            else -> hour % (HOURS / 2)
        }
        hourDigits.text = if (is24HourView) twoDigits(shownHour) else shownHour.toString()
        minuteDigits.text = twoDigits(minute)
        amPm.beVisibleIf(!is24HourView)
        amPm.text = DateFormatSymbols.getInstance().amPmStrings[if (hour < HOURS / 2) Calendar.AM else Calendar.PM]
    }

    private fun twoDigits(value: Int) = String.format(Locale.ROOT, "%02d", value)

    private fun dp(value: Float) = (value * density).toInt()

    private fun column(digits: TextView, up: (Int) -> Unit, down: (Int) -> Unit, step: (repeating: Boolean) -> Int) =
        LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(arrow("⌃") { repeating -> up(step(repeating)) })
            addView(digits, LayoutParams(dp(DIGITS_WIDTH_DP), LayoutParams.WRAP_CONTENT))
            addView(arrow("⌄") { repeating -> down(step(repeating)) })
        }

    private fun digits() = TextView(context).apply {
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, DIGITS_SP)
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(context.uiColor(UiRole.PICKER_TIME_DIGITS))
        val v = dp(DIGITS_PAD_DP)
        setPadding(0, v, 0, v)
        context.applyFontToTextView(this)
    }

    private fun separator() = TextView(context).apply {
        text = ":"
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, DIGITS_SP)
        setTextColor(context.uiColor(UiRole.PICKER_TIME_SEPARATOR))
        val h = dp(SEPARATOR_PAD_DP)
        setPadding(h, 0, h, 0)
    }

    // A small outlined pill, like every other action in the family.
    private fun pill(label: String, onClick: () -> Unit) = TextView(context).apply {
        text = label
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, ARROW_SP)
        setTextColor(context.uiColor(UiRole.PICKER_AM_PM))
        background = context.pillDrawable(context.uiColor(UiRole.PICKER_ARROW_FRAME), context.uiColor(UiRole.DIALOG_GROUND), false)
        minWidth = dp(ARROW_WIDTH_DP)
        val pad = dp(ARROW_PAD_DP)
        setPadding(pad * 2, pad, pad * 2, pad)
        setOnClickListener { onClick() }
    }

    // An arrow pill that steps once on a tap and keeps stepping while it is held.
    @SuppressLint("ClickableViewAccessibility")
    private fun arrow(glyph: String, onStep: (repeating: Boolean) -> Unit): View {
        val view = pill(glyph) { onStep(false) }
        view.setTextColor(context.uiColor(UiRole.PICKER_ARROW))
        val repeater = object : Runnable {
            override fun run() {
                onStep(true)
                view.postDelayed(this, REPEAT_EVERY_MS)
            }
        }
        view.setOnLongClickListener {
            view.post(repeater)
            true
        }
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                view.removeCallbacks(repeater)
            }
            false
        }
        return view
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler?.removeCallbacksAndMessages(null)
    }

    private companion object {
        const val HOURS = 24
        const val MINUTES = 60
        const val MINUTE_LEAP = 5
        const val DIGITS_WIDTH_DP = 76f
        const val DIGITS_SP = 40f
        const val DIGITS_PAD_DP = 6f
        const val SEPARATOR_PAD_DP = 6f
        const val ARROW_SP = 18f
        const val ARROW_WIDTH_DP = 56f
        const val ARROW_PAD_DP = 6f
        const val AM_PM_GAP_DP = 12f
        const val REPEAT_EVERY_MS = 140L
    }
}
