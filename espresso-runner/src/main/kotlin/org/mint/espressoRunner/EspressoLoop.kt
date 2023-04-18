package org.mint.espressoRunner

import android.app.Instrumentation.ActivityResult
import androidx.fragment.app.FragmentActivity.RESULT_OK
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.ViewPagerActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.withResourceName
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import com.google.common.base.Strings
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.hasToString
import org.hamcrest.Matchers.not
import org.mint.android.Action
import org.mint.android.AndroidConstants
import org.mint.android.AndroidCtx
import org.mint.android.AndroidLoop
import org.mint.android.AndroidState
import org.mint.android.TestMetadata
import org.mint.android.rule.input.datetime.DateInputSupplier
import org.mint.android.rule.input.datetime.TimeInputSupplier
import org.mint.android.xml.extend
import org.mint.espressoRunner.actions.CustomScrollTo
import org.mint.espressoRunner.actions.DeviceRotationChange
import org.mint.espressoRunner.actions.DeviceThemeChange
import org.mint.espressoRunner.matchers.ViewAtPosition
import org.mint.espressoRunner.state.EspressoStateToXML
import org.mint.espressoRunner.state.ExternalIntentDetails
import org.mint.espressoRunner.state.LoopMetaDetails
import org.mint.util.Either
import org.w3c.dom.Element
import java.text.ParseException
import java.util.*

/**
 * Implementation of the AndroidLoop, but with Espresso specifics that cannot be generalised
 */
data class EspressoLoop(private val _ctx: AndroidCtx, private val seq: String, private val m: TestMetadata?) :
    AndroidLoop(_ctx, seq, m) {

    override fun before() {
        super.before()
        // start recording the intents
        Intents.init()
        // Should this be configurable?
        // stubbing of external intents
        Intents.intending(not(isInternal()))
            .respondWith(ActivityResult(RESULT_OK, null))
    }

    override fun after() {
        super.after()
        Intents.release()
    }

    override fun obtainConcreteState(): Either<AndroidState, AndroidState> {
        val roots = GetRoots()
        val node = if (roots.isEmpty()) {
            val (_, node) = EspressoStateToXML.emptySUT()
            node.setAttribute(
                AndroidConstants.ERROR_MESSAGE,
                "Cannot obtain the root view, no concrete state available"
            )
            node
        } else {
            EspressoStateToXML.toXML(roots)
        }
        // Make sure the (current) state config is always included in the application state
        node.appendChild(ctx.config(node.ownerDocument))
        metadata?.let { node.appendChild(ctx.metadata(node.ownerDocument, it)) }

        // Add loop details
        node.extend(LoopMetaDetails(this))

        // Again, based on the value of 'root' we can decide to return left or right
        return if (roots.isEmpty()) {
            Either.Left(AndroidState(node, Random()))
        } else {
            Either.Right(AndroidState(node, Random()))
        }
    }

    override fun abstract(state: AndroidState): Either<AndroidState, AndroidState> {
        val abstract = state.copy()
        abstract.extend(ctx.abstract)
        abstract.extend(ctx.action_abstraction)
        abstract.createParentHashes()
        return Either.Right(abstract)
    }

    // perform action
    override fun perform(n: Element) {
        val type = n.tagName
        val resourceNameMatcher = withResourceName(n.getAttribute("resourceName"))
        val view = onView(resourceNameMatcher)
        when (type) {
            Action.CLICK.tagName -> view.perform(click())
            Action.SCROLL_TO_AND_CLICK_ITEM_AT_POSITION.tagName -> {
                // scrolling is especially needed when having recycler views
                val position = n.getAttribute("position")
                onView(ViewAtPosition(position)).perform(CustomScrollTo(), click())
            }
            Action.CLICK_ON_ITEM_WITH_TAG.tagName -> {
                onView(
                    allOf(
                        withTagValue(hasToString(n.getAttribute("tag"))),
                        withResourceName(n.getAttribute("resourceName"))
                    )
                ).perform(click())
            }
            Action.CLICK_ON_ITEM_AT_POSITION.tagName -> {
                val position = n.getAttribute("position")
                onView(ViewAtPosition(position)).perform(click())
            }
            Action.CLICK_ON_ITEM_AT_POSITION_IN_POPUP.tagName -> {
                val position = n.getAttribute("position")
                onView(ViewAtPosition(position))
                    .inRoot(RootMatchers.isPlatformPopup())
                    .perform(click())
            }
            Action.CLICK_ON_SPINNER_ITEM.tagName -> {
                // Grouping these actions together because the dropdown menu is displayed as a
                // platform pop-up, which makes it more difficult to get the correct root
                // of the view hierarchy while it is displayed.

                // just displaying the dropdown menu
                onData(anything())
                    .atPosition(0)
                    .perform(click())
                // selecting an item from the list
                onData(anything())
                    .atPosition(n.getAttribute("position").toInt())
                    .inRoot(RootMatchers.isPlatformPopup())
                    .perform(click())
            }
            Action.CLICK_ON_ADAPTER_VIEW_ITEM.tagName -> {
                onData(anything())
                    .inAdapterView(resourceNameMatcher)
                    .atPosition(n.getAttribute("position").toInt())
                    .perform(click())
            }
            Action.SCROLL_PAGER_TO_LEFT.tagName -> {
                view.perform(ViewPagerActions.scrollLeft())
            }
            Action.SCROLL_PAGER_TO_RIGHT.tagName -> {
                view.perform(ViewPagerActions.scrollRight())
            }
            Action.INPUT.tagName -> {
                val input = n.getAttribute("text")
                val position = n.getAttribute("position")
                val target: ViewInteraction = if (!Strings.isNullOrEmpty(position)) {
                    onView(ViewAtPosition(position))
                } else {
                    view
                }
                if (isTypeableRegex.matches(input)) {
                    target.perform(clearText(), typeText(input), closeSoftKeyboard())
                } else {
                    // We can't type this string on the (soft) keyboard, so just replace the existing text
                    target.perform(replaceText(input))
                }
            }
            Action.DATE_PICKER_INPUT.tagName -> {
                val input = n.getAttribute("text")
                val calendar: Calendar
                try {
                    val date = DateInputSupplier.defaultDateFormatter.parse(input)
                    calendar = Calendar.getInstance()
                    calendar.time = date
                } catch (e: ParseException) {
                    throw IllegalStateException("Could not parse input for date picker", e)
                }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                view.perform(PickerActions.setDate(year, month, day))
            }
            Action.TIME_PICKER_INPUT.tagName -> {
                val input = n.getAttribute("text")
                val calendar: Calendar?
                try {
                    val time = TimeInputSupplier.defaultTimeFormatter.parse(input)
                    calendar = Calendar.getInstance()
                    calendar.time = time
                } catch (e: ParseException) {
                    throw IllegalStateException("Could not parse input for time picker", e)
                }
                val hours = calendar.get(Calendar.HOUR)
                val minutes = calendar.get(Calendar.MINUTE)
                view.perform(PickerActions.setTime(hours, minutes))
            }
            Action.DEVICE_ROTATION_CHANGE.tagName -> {
                DeviceRotationChange.randomRotation()
            }

            Action.DEVICE_THEME_CHANGE.tagName -> {
                DeviceThemeChange.randomTheme()
            }
        }

        intended(not(isInternal()), ExternalIntentDetails(n))
    }
}
