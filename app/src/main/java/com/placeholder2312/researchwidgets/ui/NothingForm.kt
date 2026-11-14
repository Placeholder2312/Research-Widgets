package com.placeholder2312.researchwidgets.ui

import android.content.Context
import android.graphics.Typeface
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.placeholder2312.researchwidgets.R

/** Small builder for the Nothing-styled setup screens (programmatic, so the widget screens stay compact). */
internal class NothingForm(private val context: Context) {
    val ink = context.getColor(R.color.nothing_ink)
    val muted = context.getColor(R.color.nothing_muted)
    val page = context.getColor(R.color.nothing_page)
    private val light: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
    private val mono: Typeface = Typeface.MONOSPACE

    fun dp(value: Int) = (value * context.resources.displayMetrics.density).toInt()

    fun root() = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(page)
        setPadding(dp(24), dp(40), dp(24), dp(24))
    }

    fun caption(label: String, top: Int = 0, bottom: Int = 0) =
        text(label, 11f, muted, top, bottom, mono).apply { letterSpacing = 0.12f }

    fun text(
        label: String, size: Float, color: Int = ink, top: Int = 0, bottom: Int = 0, face: Typeface = light
    ) = TextView(context).apply {
        text = label
        typeface = face
        textSize = size
        setTextColor(color)
        setPadding(0, dp(top), 0, dp(bottom))
    }

    fun status(initial: String) = text(initial, 12f, muted, top = 18, bottom = 18, face = mono)

    fun optionGroup() = RadioGroup(context).apply {
        orientation = RadioGroup.VERTICAL
        setBackgroundResource(R.drawable.nothing_card_bg)
        setPadding(dp(16), dp(8), dp(16), dp(8))
    }

    fun option(optionId: Int, label: String) = RadioButton(context).apply {
        id = optionId
        text = label
        typeface = light
        textSize = 15f
        setTextColor(ink)
        setButtonDrawable(R.drawable.nothing_radio)
        setPadding(dp(14), dp(14), 0, dp(14))
    }

    fun pill(label: String, filled: Boolean) = Button(context).apply {
        text = label
        typeface = mono
        textSize = 13f
        letterSpacing = 0.1f
        stateListAnimator = null
        setBackgroundResource(if (filled) R.drawable.nothing_button_filled else R.drawable.nothing_button_outline)
        setTextColor(if (filled) page else ink)
    }

    /** Cancel / confirm row. */
    fun actions(confirmLabel: String, onCancel: () -> Unit, onConfirm: () -> Unit) =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(
                pill("Cancel", filled = false).apply { setOnClickListener { onCancel() } },
                LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginEnd = dp(8) }
            )
            addView(
                pill(confirmLabel, filled = true).apply { setOnClickListener { onConfirm() } },
                LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginStart = dp(8) }
            )
        }
}
