package com.ing.mint.espressoRunner.state.attributes

import android.view.View
import android.view.WindowManager
import androidx.test.espresso.Root
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.RootMatchers.isSystemAlertWindow
import org.w3c.dom.Element

object WindowAttributes {
    fun apply(node: Element, root: View) {
        root.windowId?.let { windowId ->
            node.setAttribute("isFocused", windowId.isFocused.toString())
        }

        (root.layoutParams as? WindowManager.LayoutParams)?.apply {
            node.setAttribute(
                "isFocusable",
                (
                    this.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        != WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    ).toString(),
            )

            val espressoRoot =
                Root.Builder()
                    .withDecorView(root)
                    .withWindowLayoutParams(this)
                    .build()
            when {
                isDialog().matches(espressoRoot) -> node.setAttribute("isDialog", "true")
                isPlatformPopup().matches(espressoRoot) -> node.setAttribute("isPlatformPopup", "true")
                isSystemAlertWindow().matches(espressoRoot) -> node.setAttribute("isSystemAlertWindow", "true")
            }
        }
    }
}
