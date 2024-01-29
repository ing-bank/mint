package com.ing.mint.espressoRunner.state.attributes

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.test.platform.app.InstrumentationRegistry
import org.w3c.dom.Element
import kotlin.math.roundToInt

internal object KeyboardAttributes {
    fun apply(node: Element, view: View) {
        if (view.id == android.R.id.content) {
            val applicationContext = InstrumentationRegistry.getInstrumentation().targetContext

            if (!isWindowAdjustableForSoftInput(applicationContext)) {
                node.setAttribute(
                    "isSoftKeyboardVisible",
                    isKeyboardVisible(view, applicationContext).toString(),
                )
            }
        }
    }

    private fun isKeyboardVisible(view: View, context: Context): Boolean {
        // https://proandroiddev.com/how-to-detect-if-the-android-keyboard-is-open-269b255a90f5
        val visibleBounds = Rect()
        view.getWindowVisibleDisplayFrame(visibleBounds)
        val heightDiff = view.height - visibleBounds.height()

        val marginOfErrorInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            50F,
            context.resources.displayMetrics,
        ).roundToInt()

        return heightDiff > marginOfErrorInPx
    }

    private fun isWindowAdjustableForSoftInput(context: Context): Boolean {
        val window = (context as? Activity)?.window

        // SOFT_INPUT_ADJUST_RESIZE deprecated in API 30
        return window?.attributes?.softInputMode ==
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
    }
}
