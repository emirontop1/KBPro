package com.kb

import android.annotation.SuppressLint
import android.graphics.*
import android.view.MotionEvent
import android.view.View

@SuppressLint("ClickableViewAccessibility")
class KeyboardPanel(private val service: IMEService) : View(service) {

    private var isShifted = false
    private var mode = MODE_LETTERS
    private val keys = mutableListOf<Key>()
    private var pressed: Key? = null

    companion object {
        const val MODE_LETTERS = 0
        const val MODE_NUMBERS = 1
        const val MODE_SYMBOLS = 2
    }

    private val bgPaint = Paint().apply { color = Color.parseColor("#1C1C1E") }
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2C2C2E") }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 18f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        buildKeys(w, h)
    }

    private fun buildKeys(w: Int, h: Int) {
        keys.clear()
        val kh = h / 5f
        val rows = when (mode) {
            MODE_LETTERS -> listOf(
                "qwertyuiop", "asdfghjkl", "zxcvbnm",
                listOf("123", " ", "↵")
            )
            MODE_NUMBERS -> listOf(
                "1234567890", "@#$%&-+()",
                "*/.,!?;:\"", listOf("ABC", " ", "↵")
            )
            MODE_SYMBOLS -> listOf(
                "~`|•√π÷×¶∆", "£¢€¥°={}[]",
                "<>«»_…„""", listOf("ABC", " ", "↵")
            )
            else -> emptyList()
        }

        var y = 0f
        for (row in rows) {
            val items = if (row is String) row.map { it.toString() } else row as List<String>
            val kw = w / items.size.toFloat()
            items.forEachIndexed { i, label ->
                keys += Key(label, i * kw, y, kw, kh)
            }
            y += kh
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        keys.forEach { key ->
            val paint = if (key == pressed) Paint(keyPaint).apply { color = Color.parseColor("#545458") } else keyPaint
            canvas.drawRect(key.x + 2, key.y + 2, key.x + key.w - 2, key.y + key.h - 2, paint)
            canvas.drawText(
                if (isShifted && key.label.length == 1) key.label.uppercase() else key.label,
                key.x + key.w / 2, key.y + key.h / 2 + 6, textPaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressed = keys.firstOrNull { it.contains(event.x, event.y) }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                pressed?.let { handleKey(it) }
                pressed = null
                invalidate()
            }
        }
        return true
    }

    private fun handleKey(key: Key) {
        when {
            key.label == "↵" -> service.sendEnter()
            key.label == " " -> service.sendText(" ")
            key.label == "⌫" -> service.delete()
            key.label == "123" || key.label == "ABC" -> {
                mode = if (key.label == "123") MODE_NUMBERS else MODE_LETTERS
                buildKeys(width, height)
                invalidate()
            }
            key.label.length == 1 -> {
                val text = if (isShifted && key.label[0].isLetter()) key.label.uppercase() else key.label
                service.sendChar(text[0])
                if (isShifted) { isShifted = false; invalidate() }
            }
        }
    }

    fun reset() {
        isShifted = service.shouldCapitalize()
        mode = MODE_LETTERS
        buildKeys(width, height)
        invalidate()
    }

    data class Key(val label: String, val x: Float, val y: Float, val w: Float, val h: Float) {
        fun contains(px: Float, py: Float) = px >= x && px < x + w && py >= y && py < y + h
    }
}
