package com.jwo.chipowski

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Created by j.ostrander on 1/10/17.
 */

class Chip8View : View {
    var graphics: ByteArray = ByteArray(64 * 32)
        set(value) {
            field = value.copyOf()
            invalidate()
        }

    var pixelWidth: Float = 0f
    var pixelHeight: Float = 0f
    lateinit var white: Paint
    lateinit var black: Paint

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet)
    constructor(context: Context?, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        white = Paint()
        white.color = Color.WHITE
        black = Paint()
        black.color = Color.BLACK
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        pixelWidth = (width / 64).toFloat()
        pixelHeight = (height / 32).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (j in 0..32 - 1) {
            for (i in 0..64 - 1) {
                val index = i + 64 * j
                canvas.drawRect(i * pixelWidth, j * pixelHeight, i * pixelWidth + pixelWidth, j * pixelHeight + pixelHeight, if (graphics[index] == 1.toByte()) white else black)
            }
        }
    }
}