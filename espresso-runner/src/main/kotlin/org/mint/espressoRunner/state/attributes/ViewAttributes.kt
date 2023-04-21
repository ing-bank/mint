package org.mint.espressoRunner.state.attributes

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Checkable
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.platform.app.InstrumentationRegistry
import org.mint.espressoRunner.state.EspressoViewVisibilityPredicate
import org.w3c.dom.Element

/** Apply all View attributes we are interested in to the given element.
 *
 * This is probably not an exhaustive list... */
object ViewAttributes {
    private val viewVisibilityPredicate = EspressoViewVisibilityPredicate()

    private const val TAG = "o.m.e.s.a.VA"

    @SuppressLint("NewApi", "LongLogTag")
    fun apply(node: Element, view: View) {
        node.setAttribute("width", view.width.toString())
        node.setAttribute("height", view.height.toString())

        node.setAttribute("x", view.x.toString())
        node.setAttribute("y", view.y.toString())
        node.setAttribute("z", view.z.toString())

        val location = IntArray(2)
        view.getLocationOnScreen(location)

        node.setAttribute("screenX", location[0].toString())
        node.setAttribute("screenY", location[1].toString())

        // https://developer.android.com/reference/android/view/View#getDrawingTime()
        node.setAttribute("drawingTime", view.drawingTime.toString())
        node.setAttribute("isFocused", view.isFocused.toString())
        node.setAttribute("isFocusableInTouchMode", view.isFocusableInTouchMode.toString())
        node.setAttribute("isActivated", view.isActivated.toString())
        node.setAttribute("isClickable", view.isClickable.toString())
        node.setAttribute("isEnabled", view.isEnabled.toString())
        node.setAttribute("isHovered", view.isHovered.toString())
        node.setAttribute("isLongClickable", view.isLongClickable.toString())
        node.setAttribute("isPressed", view.isPressed.toString())
        node.setAttribute("isSelected", view.isSelected.toString())
        node.setAttribute("isShown", view.isShown.toString())
        node.setAttribute("isVisible", view.isVisible.toString())
        node.setAttribute("isGone", view.isGone.toString())
        node.setAttribute("isInvisible", view.isInvisible.toString())
        // View visibility does not directly translate into whether the view is displayed on screen.
        // For example, the view and all of its ancestors can be visible,
        // but the view may need to be scrolled to in order to be actually visible to the user.
        node.setAttribute("isDisplayed", viewVisibilityPredicate.test(view).toString())
        node.setAttribute("hasOnClickListeners", view.hasOnClickListeners().toString())

        AccessibilityAttributes.apply(node, view)
        KeyboardAttributes.apply(node, view)
        view.tag?.let { node.setAttribute("tag", it.toString()) }

        when {
            view.canScrollHorizontally(-1) -> node.setAttribute("scrollableToLeft", "true")
            view.canScrollHorizontally(1) -> node.setAttribute("scrollableToRight", "true")
        }
        when {
            view.canScrollVertically(-1) -> node.setAttribute("scrollableToTop", "true")
            view.canScrollVertically(1) -> node.setAttribute("scrollableToBottom", "true")
        }
        node.setAttribute("scrollX", view.scrollX.toString())
        node.setAttribute("scrollY", view.scrollY.toString())

        when (view) {
            // eg. RadioButton, ToggleButton, CheckBox
            is Checkable -> {
                node.setAttribute("isCheckable", "true")
                node.setAttribute("isChecked", view.isChecked.toString())
            }
            is ProgressBar -> {
                node.setAttribute("isProgressBar", "true")
                node.setAttribute("progress", view.progress.toString())
            }
            // eg. Spinner (https://developer.android.com/guide/topics/ui/controls/spinner)
            is AdapterView<*> -> {
                node.setAttribute("isAdapterView", "true")
                node.setAttribute("itemCount", view.count.toString())
                node.setAttribute("firstVisiblePosition", view.firstVisiblePosition.toString())
                node.setAttribute("lastVisiblePosition", view.lastVisiblePosition.toString())
                view.selectedItem?.let { node.setAttribute("selectedItem", it.toString()) }
                node.setAttribute("selectedItemPosition", view.selectedItemPosition.toString())

                if (view is Spinner) {
                    node.setAttribute("isSpinner", "true")
                }
            }
            is RecyclerView -> RecyclerViewAttributes(viewVisibilityPredicate).apply(node, view)
            is DatePicker -> {
                node.setAttribute("isDatePicker", "true")
                node.setAttribute("dayOfMonth", view.dayOfMonth.toString())
                node.setAttribute("month", view.month.toString())
                node.setAttribute("year", view.year.toString())
            }
            is TimePicker -> {
                node.setAttribute("isTimePicker", "true")
                // 24h or AM/PM mode
                node.setAttribute("is24hView", view.is24HourView.toString())
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    node.setAttribute("hour", view.currentHour.toString())
                    node.setAttribute("minute", view.currentMinute.toString())
                } else {
                    node.setAttribute("hour", view.hour.toString())
                    node.setAttribute("minute", view.minute.toString())
                }
            }
            is ImageView -> {
                node.setAttribute("isImageView", "true")
                when (view) {
                    is ImageButton -> {
                        // ImageButtons are "decorated" ImageViews. This is to ensure that the
                        // current View's "identifier" is specific. So when we create rules only for
                        // ImageViews, these ImageButton views won't be included.
                        node.removeAttribute("isImageView")

                        node.setAttribute("isImageButton", "true")
                    }
                }
            }
            is Toolbar -> {
                node.setAttribute("isToolbar", "true")
            }
        }
        when (view) {
            // separate when statement because some TextViews are also Checkable
            is TextView -> {
                node.setAttribute("text", view.text.toString())
                node.setAttribute("isTextView", "true")

                val urls = view.urls
                val hasLinks = urls.isNotEmpty()
                node.setAttribute("hasLinks", hasLinks.toString())
                for (i in urls.indices) {
                    node.setAttribute("link$i", urls[i].toString())
                }
                when (view) {
                    is EditText -> {
                        node.setAttribute("isEditText", "true")
                        node.setAttribute("inputType", view.inputType.toString())

                        // To get the Locale, we need to access the resources. The try-catch block
                        // ensures that MINT does not crash when resources are not available.
                        try {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                node.setAttribute(
                                    "locale",
                                    InstrumentationRegistry.getInstrumentation()
                                        .targetContext
                                        .resources
                                        .configuration
                                        .locale
                                        .toString(),
                                )
                            } else {
                                node.setAttribute(
                                    "locale",
                                    InstrumentationRegistry.getInstrumentation()
                                        .targetContext
                                        .resources
                                        .configuration
                                        .locales
                                        .get(0)
                                        .toString(),
                                )
                            }
                        } catch (e: Resources.NotFoundException) {
                            node.setAttribute("locale", "unknown")
                            Log.e(
                                TAG,
                                "Failed to retrieve Resource Configuration (Locale). Details: $e",
                            )
                        }

                        view.filters?.let { filters ->
                            for (f in filters) {
                                if (f is InputFilter.LengthFilter) {
                                    node.setAttribute("maxLength", f.max.toString())
                                }
                            }
                        }
                    }
                    is Button -> {
                        node.setAttribute("isButton", "true")
                    }
                    is ActionMenuItemView -> {
                        node.setAttribute("isActionMenuItemView", "true")
                    }
                }
            }
        }
    }
}
