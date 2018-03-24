package com.jaredrummler.cyanea.tinting

import android.app.Activity
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.v4.view.ViewPager
import android.support.v4.widget.EdgeEffectCompat

import android.support.v4.widget.NestedScrollView
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.AbsListView
import android.widget.EdgeEffect
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.utils.Reflection

/**
 * Provides utility methods to set the color of an [EdgeEffect].
 *
 * Example usage:
 *
 * ```kotlin
 * if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
 *     EdgeEffectTint(activity).tint(ContextCompat.getColor(activity, R.color.color_primary))
 * }
 * ```
 */
class EdgeEffectTint(private val view: ViewGroup) {
  constructor(activity: Activity) : this(activity.findViewById<View>(android.R.id.content).rootView as ViewGroup)

  /**
   * Sets the color on all edge effects for the [ViewGroup] *and* its children passed to the constructor.
   *
   * @param color The color to be applied on the [EdgeEffect]
   */
  fun tint(@ColorInt color: Int) {
    setEdgeTint(view, color)
  }

  private fun setEdgeTint(viewGroup: ViewGroup, @ColorInt color: Int) {
    setEdgeTint(viewGroup, color)
    var i = 0
    val count = viewGroup.childCount
    while (i < count) {
      val child = viewGroup.getChildAt(i)
      if (!setEdgeGlowColor(child, color) && child is ViewGroup) {
        setEdgeTint(child, color)
      }
      i++
    }
  }

  companion object {

    private val TAG = "EdgeEffectTint"

    /**
     * Set the color of an [EdgeEffect]. This uses reflection on pre-L.
     *
     * @param edgeEffect The EdgeEffect to apply the color on.
     * @param color The color value
     */
    fun setEdgeEffectColor(edgeEffect: EdgeEffect, @ColorInt color: Int) {
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          edgeEffect.color = color
          return
        }
        for (name in arrayOf("mEdge", "mGlow")) {
          val drawable = Reflection.getFieldValue<Drawable?>(edgeEffect, name)
          drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
          drawable?.setCallback(null) // free up any references
        }
      } catch (e: Exception) {
        Cyanea.log(TAG, "Error setting edge effect color", e)
      }
    }

    /**
     * Set the edge-effect glow color for a view.
     *
     * Supported views:
     *
     *  * [AbsListView]
     *  * [HorizontalScrollView]
     *  * [ScrollView]
     *  * [NestedScrollView]
     *  * [ViewPager]
     *  * [WebView]
     *
     * @param view The view to set the edge color
     * @param color The color value
     * @return `true` if the view was one of the supported views, `false` otherwise
     */
    fun setEdgeGlowColor(view: View, @ColorInt color: Int): Boolean {
      when (view) {
        is AbsListView -> setEdgeGlowColor(view, color)
        is HorizontalScrollView -> setEdgeGlowColor(view, color)
        is ScrollView -> setEdgeGlowColor(view, color)
        is NestedScrollView -> setEdgeGlowColor(view, color)
        is ViewPager -> setEdgeGlowColor(view, color)
        is WebView -> setEdgeGlowColor(view, color)
        else -> return false
      }
      return true
    }

    /**
     * Set the edge-effect color on a [NestedScrollView].
     *
     * @param scrollView The [NestedScrollView] to set the edge color on
     * @param color The color value
     */
    private fun setEdgeGlowColor(scrollView: NestedScrollView, @ColorInt color: Int) {
      try {
        Reflection.invoke<Any?>(scrollView, "ensureGlows", types = emptyArray())
        for (name in arrayOf("mEdgeGlowTop", "mEdgeGlowBottom")) {
          val edgeEffectCompat = Reflection.getFieldValue<EdgeEffectCompat?>(scrollView, name)
          val edgeEffect = Reflection.getFieldValue<EdgeEffect>(edgeEffectCompat, "mEdgeEffect")
          edgeEffect?.let { setEdgeEffectColor(it, color) }
        }
      } catch (e: Exception) {
        Cyanea.log(TAG, "Error setting edge glow color on NestedScrollView", e)
      }
    }

    /**
     * Set the edge-effect color on a [HorizontalScrollView].
     *
     * @param hsv The view to set the edge color
     * @param color The color value
     */
    private fun setEdgeGlowColor(hsv: HorizontalScrollView, @ColorInt color: Int) {
      try {
        for (name in arrayOf("mEdgeGlowLeft", "mEdgeGlowRight")) {
          val edgeEffect = Reflection.getFieldValue<EdgeEffect?>(hsv, name)
          edgeEffect?.let { setEdgeEffectColor(it, color) }
        }
      } catch (e: Exception) {
        Cyanea.log(TAG, "Error setting edge glow color on HorizontalScrollView", e)
      }
    }

    /**
     * Set the edge-effect color on a [ScrollView].
     *
     * @param scrollView The view to set the edge color
     * @param color The color value
     */
    private fun setEdgeGlowColor(scrollView: ScrollView, color: Int) {
      try {
        for (name in arrayOf("mEdgeGlowTop", "mEdgeGlowBottom")) {
          val edgeEffect = Reflection.getFieldValue<EdgeEffect?>(scrollView, name)
          edgeEffect?.let { setEdgeEffectColor(it, color) }
        }
      } catch (e: Exception) {
        Cyanea.log(TAG, "Error setting edge glow color on ScrollView", e)
      }
    }

    /**
     * Set the edge-effect color on a [ViewPager].
     *
     * @param viewPager the ViewPager
     * @param color The color value
     */
    private fun setEdgeGlowColor(viewPager: ViewPager, color: Int) {
      try {
        for (name in arrayOf("mLeftEdge", "mRightEdge")) {
          val edgeEffectCompat = Reflection.getFieldValue<EdgeEffectCompat?>(viewPager, name)
          val edgeEffect = Reflection.getFieldValue<EdgeEffect?>(edgeEffectCompat, "mEdgeEffect")
          edgeEffect?.let { setEdgeEffectColor(it, color) }
        }
      } catch (e: Exception) {
        Cyanea.log(TAG, "Error setting edge glow color on ViewPager", e)
      }
    }

    /**
     * Set the edge-effect color on a [WebView].
     *
     * @param webView
     * the WebView
     * @param color
     * the color value
     */
    private fun setEdgeGlowColor(webView: WebView, color: Int) {
      try {
        val provider = Reflection.invoke<Any?>(webView, "getWebViewProvider", types = emptyArray())
        val delegate = Reflection.invoke<Any?>(provider, "getViewDelegate", types = emptyArray())
        val mAwContents = Reflection.getFieldValue<Any?>(delegate, "mAwContents")
        val mOverScrollGlow = Reflection.getFieldValue<Any?>(mAwContents, "mOverScrollGlow")
        for (name in arrayOf("mEdgeGlowTop", "mEdgeGlowBottom", "mEdgeGlowLeft", "mEdgeGlowRight")) {
          val edgeEffect = Reflection.getFieldValue<EdgeEffect?>(mOverScrollGlow, name)
          edgeEffect?.let { setEdgeEffectColor(it, color) }
        }
      } catch (e: Exception) {
        Cyanea.log(TAG, "Error setting edge glow color on WebView", e)
      }

    }

  }

}