package com.ing.mint.espressoRunner.state

import android.view.View
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPresetAndroid
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType.ERROR
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType.INFO
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType.RESOLVED
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType.WARNING
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult
import com.google.android.apps.common.testing.accessibility.framework.Parameters
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata
import com.ing.mint.android.xml.appendChild
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.util.*
import kotlin.reflect.KClass

/**
 * Scans the view hierarchy using the Accessibility Test Framework and adds all the findings to the
 * XML state.
 */
internal object AccessibilityViewCheck {
    private const val ELEMENT_ACCESSIBILITY_CHECKS = "accessibility-checks"
    private const val ELEMENT_VIEW_CHECK = "view-check"

    private const val ATTR_RESULT = "result"
    private const val ATTR_TYPE = "type"
    private const val ATTR_MESSAGE = "message"
    private const val ATTR_METADATA = "metadata"

    private const val STRING_UNDERSCORE = "_"
    private const val STRING_DASH = "-"

    @Suppress("DEPRECATION")
    fun apply(node: Element, view: View) {
        val viewHierarchyChecks = AccessibilityCheckPresetAndroid.getViewChecksForPreset(AccessibilityCheckPreset.LATEST).asList()
        val viewCheckResults = viewHierarchyChecks
            .flatMap { it?.runCheckOnViewHierarchy(view, Parameters()).orEmpty() }

        if (viewCheckResults.isEmpty()) {
            return
        }

        var accessibilityCheckElement: Node? = null
        viewCheckResults.forEach { result ->
            when (result.type) {
                ERROR, WARNING, INFO, RESOLVED -> {
                    result.view?.let { resultView ->
                        if (resultView == view) {
                            // There are findings for this current view.
                            if (accessibilityCheckElement == null) {
                                accessibilityCheckElement =
                                    node.appendChild(ELEMENT_ACCESSIBILITY_CHECKS)
                            }
                            accessibilityCheckElement?.appendResult(result)
                        }
                    }
                }
                else -> {
                    // DO NOTHING for NOT_RUN and SUPPRESSED
                }
            }
        }
    }

    private fun Node.appendResult(result: AccessibilityViewCheckResult) {
        val viewCheckNode = appendChild(
            ELEMENT_VIEW_CHECK,
            attributes = mutableListOf(
                Pair(ATTR_RESULT, "${result.type}"),
                Pair(ATTR_TYPE, result.sourceCheckClass.simpleName),
                Pair(ATTR_MESSAGE, result.getMessage(Locale.ENGLISH).toString()),
            ),
        )

        result.metadata?.let { metadata ->
            viewCheckNode.appendMetadata(metadata)
        }
    }

    private fun Node.appendMetadata(
        metadata: ResultMetadata,
    ) {
        val attributes = ArrayList<Pair<String, String>>()
        MetadataKeys.values().forEach { key ->
            if (metadata.containsKey(key.name)) {
                attributes.add(
                    Pair(key.name.kebabCase(), metadata.get(key)),
                )
            }
        }

        appendChild(
            ATTR_METADATA,
            attributes = attributes,
        )
    }

    private fun String.kebabCase(): String {
        return lowercase().replace(STRING_UNDERSCORE, STRING_DASH)
    }

    private fun ResultMetadata.get(key: MetadataKeys): String {
        return when (key.type) {
            Boolean::class -> {
                "${getBoolean(key.name)}"
            }
            Byte::class -> {
                "${getByte(key.name)}"
            }
            Short::class -> {
                "${getShort(key.name)}"
            }
            Char::class -> {
                "${getChar(key.name)}"
            }
            Int::class -> {
                "${getInt(key.name)}"
            }
            Float::class -> {
                "${getFloat(key.name)}"
            }
            Long::class -> {
                "${getLong(key.name)}"
            }
            Double::class -> {
                "${getDouble(key.name)}"
            }
            String::class -> {
                getString(key.name)
            }
            Array<String>::class -> {
                "${getStringList(key.name)}"
            }
            Array<Int>::class -> {
                "${getIntegerList(key.name)}"
            }
            else -> {
                ""
            }
        }
    }

    enum class MetadataKeys(val type: KClass<*>) {
        // ClassNameCheck
        KEY_ACCESSIBILITY_CLASS_NAME(String::class),

        // DuplicateClickableBoundsCheck
        KEY_CONFLICTS_BECAUSE_CLICKABLE(Boolean::class),
        KEY_CONFLICTS_BECAUSE_LONG_CLICKABLE(Boolean::class),
        KEY_CONFLICTING_VIEW_COUNT(Int::class),
        KEY_CONFLICTING_LOCATION_LEFT(Int::class),
        KEY_CONFLICTING_LOCATION_TOP(Int::class),
        KEY_CONFLICTING_LOCATION_RIGHT(Int::class),
        KEY_CONFLICTING_LOCATION_BOTTOM(Int::class),

        // DuplicateSpeakableTextCheck
        KEY_SPEAKABLE_TEXT(String::class),

        // LinkPurposeUnclearCheck
        KEY_LINK_TEXT(String::class),

        // RedundantDescriptionCheck
        KEY_CONTENT_DESCRIPTION(String::class),
        KEY_REDUNDANT_WORD(String::class),

        // EditableContentDescCheck, TextContrastCheck
        KEY_BACKGROUND_COLOR(Int::class),
        KEY_BACKGROUND_OPACITY(Float::class),
        KEY_CONTRAST_RATIO(Double::class),
        KEY_FOREGROUND_COLOR(Int::class),
        KEY_REQUIRED_CONTRAST_RATIO(Double::class),
        KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO(Double::class),
        KEY_SCREENSHOT_BOUNDS_STRING(String::class),
        KEY_TEXT_COLOR(Int::class),
        KEY_TEXT_OPACITY(Float::class),
        KEY_TOLERANT_CONTRAST_RATIO(Double::class),
        KEY_VIEW_BOUNDS_STRING(String::class),
        KEY_IS_AGAINST_SCROLLABLE_EDGE(Boolean::class),
        KEY_ADDITIONAL_FOREGROUND_COLORS(Array<String>::class),
        KEY_ADDITIONAL_CONTRAST_RATIOS(Array<String>::class),
        KEY_IS_POTENTIALLY_OBSCURED(Boolean::class),
        KEY_IS_LARGE_TEXT(Boolean::class),

        // TextSizeCheck
        KEY_TEXT_SIZE_UNIT(Int::class),
        KEY_ESTIMATED_TEXT_SIZE_DP(Float::class),
        KEY_TEXT(String::class),
        KEY_OCCUPIED_FRACTION_OF_WIDTH(Float::class),
        KEY_OCCUPIED_FRACTION_OF_HEIGHT(Float::class),

        // TouchTargetSizeCheck
        KEY_HAS_TOUCH_DELEGATE(Boolean::class),
        KEY_HAS_TOUCH_DELEGATE_WITH_HIT_RECT(Boolean::class),
        KEY_HAS_CLICKABLE_ANCESTOR(Boolean::class),
        KEY_IS_CLIPPED_BY_ANCESTOR(Boolean::class),
        KEY_IS_WEB_CONTENT(Boolean::class),
        KEY_HEIGHT(Int::class),
        KEY_WIDTH(Int::class),
        KEY_NONCLIPPED_HEIGHT(Int::class),
        KEY_NONCLIPPED_WIDTH(Int::class),
        KEY_REQUIRED_HEIGHT(Int::class),
        KEY_REQUIRED_WIDTH(Int::class),
        KEY_CUSTOMIZED_REQUIRED_WIDTH(Int::class),
        KEY_CUSTOMIZED_REQUIRED_HEIGHT(Int::class),
        KEY_HIT_RECT_WIDTH(Int::class),
        KEY_HIT_RECT_HEIGHT(Int::class),

        // UnexposedTextCheck
        KEY_UNEXPOSED_TEXT(String::class),
        KEY_UNEXPOSED_TEXTS(Array<String>::class),
        KEY_OCR_BOUNDS(String::class),
        KEY_TEXT_DETECTED_IN_IMAGE_VIEW(String::class),
    }
}
