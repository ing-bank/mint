package com.ing.mint.espressoRunner.state.attributes

import android.os.Build
import android.view.View
import android.widget.TextView
import com.ing.mint.android.xml.appendChild
import com.ing.mint.espressoRunner.view.shouldFocusView
import org.w3c.dom.Element

internal object AccessibilityAttributes {
    fun apply(node: Element, view: View) {
        applyDefaultAttributes(node, view)
        applyTextViewAttributes(node, view)
        applyAccessibilityInfo(node, view)
    }

    private fun applyDefaultAttributes(node: Element, view: View) {
        // https://developer.android.com/guide/topics/ui/accessibility/apps#describe-ui-element
        view.contentDescription?.let {
            node.setAttribute("contentDescription", it.toString())
        }
        node.setAttribute("isFocusable", view.isFocusable.toString())
        node.setAttribute("labelFor", view.labelFor.toString())
        node.setAttribute(
            "isImportantForAccessibility",
            view.isImportantForAccessibility.toString(),
        )
        // for touch target size calculation, minimum 48x48dp is recommended
        // https://developer.android.com/guide/topics/ui/accessibility/apps#large-controls
        node.setAttribute("paddingBottom", view.paddingBottom.toString())
        node.setAttribute("paddingTop", view.paddingTop.toString())
        node.setAttribute("paddingLeft", view.paddingLeft.toString())
        node.setAttribute("paddingRight", view.paddingRight.toString())
        node.setAttribute("minHeight", view.minimumHeight.toString())
        node.setAttribute("minWidth", view.minimumWidth.toString())

        // if a screen reader would choose to place accessibility focus on this view
        node.setAttribute("isScreenReaderAccessible", "${view.shouldFocusView()}")
    }

    private fun applyTextViewAttributes(node: Element, view: View) {
        when (view) {
            is TextView -> {
                view.hint?.let {
                    node.setAttribute("hint", it.toString())
                }
                node.setAttribute("textSize", view.textSize.toString())
                // https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html#dfn-contrast-ratio
                node.setAttribute("isTextBold", view.typeface.isBold.toString())
                node.setAttribute("color", view.currentTextColor.toString())
                node.setAttribute("opacity", view.alpha.toString())
                node.setAttribute("backgroundColor", view.solidColor.toString())
            }
        }
    }

    private fun applyAccessibilityInfo(node: Element, view: View) {
        val accessibilityNodeInfo = view.createAccessibilityNodeInfo()

        val accessibilityAttributes = mutableListOf(
            Pair("isAccessibilityFocused", "${accessibilityNodeInfo.isAccessibilityFocused}"),
            Pair("isCheckable", "${accessibilityNodeInfo.isCheckable}"),
            Pair("isChecked", "${accessibilityNodeInfo.isChecked}"),
            Pair("isClickable", "${accessibilityNodeInfo.isClickable}"),
            Pair("isContentInvalid", "${accessibilityNodeInfo.isContentInvalid}"),
            Pair("isDismissable", "${accessibilityNodeInfo.isDismissable}"),
            Pair("isEnabled", "${accessibilityNodeInfo.isEnabled}"),
            Pair("isEditable", "${accessibilityNodeInfo.isEditable}"),
            Pair("isFocusable", "${accessibilityNodeInfo.isFocusable}"),
            Pair("isFocused", "${accessibilityNodeInfo.isFocused}"),
            Pair("isLongClickable", "${accessibilityNodeInfo.isLongClickable}"),
            Pair("isMultiLine", "${accessibilityNodeInfo.isMultiLine}"),
            Pair("isPassword", "${accessibilityNodeInfo.isPassword}"),
            Pair("isSelected", "${accessibilityNodeInfo.isSelected}"),
            Pair("isScrollable", "${accessibilityNodeInfo.isScrollable}"),
            Pair("isVisibleToUser", "${accessibilityNodeInfo.isVisibleToUser}"),
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            accessibilityAttributes.add(
                Pair(
                    "isContextClickable",
                    "${accessibilityNodeInfo.isContextClickable}",
                ),
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            accessibilityAttributes.add(
                Pair(
                    "isImportantForAccessibility",
                    "${accessibilityNodeInfo.isImportantForAccessibility}",
                ),
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            accessibilityAttributes.add(
                Pair("isShowingHintText", "${accessibilityNodeInfo.isShowingHintText}"),
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            accessibilityAttributes.add(
                Pair("isScreenReaderFocusable", "${accessibilityNodeInfo.isScreenReaderFocusable}"),
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            accessibilityAttributes.add(
                Pair("isTextEntryKey", "${accessibilityNodeInfo.isTextEntryKey}"),
            )
        }

        node.appendChild(
            "accessibility-node-info",
            attributes = accessibilityAttributes,
        )
    }
}
