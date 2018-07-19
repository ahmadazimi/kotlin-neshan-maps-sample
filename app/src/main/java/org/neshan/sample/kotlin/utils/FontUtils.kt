package org.neshan.apksample.activity.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class FontUtils private constructor() {
    companion object {
        private var instance: FontUtils? = null
        private var context: Context? = null

        fun getInstance(context: Context): FontUtils = instance ?: FontUtils().also {
            instance = it
            this@Companion.context = context
        }

        fun setViewsFont(activity: Activity) {
            setViewsFont(activity, (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup)
        }

        fun setViewsFont(context: Context?, viewGroup: ViewGroup?) {
            if (context != null && viewGroup != null) {
                for (i in 0 until viewGroup.childCount) {
                    val view = viewGroup.getChildAt(i)
                    if (view != null && view is ViewGroup) {
                        setViewsFont(context, view)
                    } else if (view != null && view is TextView) {
                        if (view.typeface != null) {
                            if (view.typeface.isBold) {
                                view.typeface = FontUtils.getInstance(context).getPubicBoldFont()
                            } else if (!view.typeface.isItalic) {
                                view.typeface = FontUtils.getInstance(context).getPubicFont()
                            }
                        } else {
                            view.typeface = FontUtils.getInstance(context).getPubicFont()
                        }
                    }
                }
            }
        }


        fun applyFontToMenuItem(context: Context, mi: MenuItem) {
            val font = Typeface.createFromAsset(context.assets, "fonts/IRANSMLFN.ttf")
            val mNewTitle = SpannableString(mi.title)
            mNewTitle.setSpan(CustomTypeFaceSpan("", font, Color.BLACK), 0, mNewTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            mi.title = mNewTitle
        }

    }



    fun getPubicFont(): Typeface {
        return Typeface.createFromAsset(context?.assets, "fonts/IRANSMLFN.ttf")
    }

    fun getPubicBoldFont(): Typeface {
        return Typeface.createFromAsset(context?.assets, "fonts/IRANSMBFN.ttf")
    }
}