package org.neshan.apksample.activity.utils

import android.graphics.Paint
import android.graphics.Typeface
import android.support.annotation.ColorInt
import android.text.TextPaint
import android.text.style.TypefaceSpan

class CustomTypeFaceSpan(family: String, typeface: Typeface, @ColorInt color: Int): TypefaceSpan(family) {
    private var mColor: Int = color
    private var newType: Typeface = typeface

    override fun updateDrawState(ds: TextPaint) {
        ds.color = mColor
        applyCustomTypeFace(ds, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }

    override fun getSpanTypeId(): Int {
        return super.getSpanTypeId()
    }

    @ColorInt
    fun getForegroundColor(): Int {
        return mColor
    }


    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        val oldStyle: Int
        val old = paint.typeface
        oldStyle = old?.style ?: 0
        val fake = oldStyle and tf.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }
        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }
        paint.typeface = tf
    }

}