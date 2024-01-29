package com.ing.mint.espressoRunner

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.FailureHandler
import androidx.test.espresso.PerformException
import androidx.test.espresso.base.DefaultFailureHandler
import androidx.test.platform.app.InstrumentationRegistry
import com.ing.mint.android.ApplicationMonitor
import com.ing.mint.android.Observer
import org.hamcrest.Matcher

/**
 * Intercepts exceptions thrown by espresso during the test execution to notify the loop-specific observer of them, if configured,
 * delegating the handling of the exception to the original failure handler.
 */
class EspressoFailureMonitor private constructor() : FailureHandler, ApplicationMonitor<Throwable> {
    private var preexistingFailureHandler: FailureHandler? = null
    private var registeredObservers: MutableSet<Observer<Throwable>> = mutableSetOf()

    companion object {
        val instance = EspressoFailureMonitor()
    }

    override fun type(): Class<Throwable> {
        return Throwable::class.java
    }

    override fun initialize() {
        preexistingFailureHandler ?: run {
            preexistingFailureHandler =
                DefaultFailureHandler(InstrumentationRegistry.getInstrumentation().targetContext)
        }
        Espresso.setFailureHandler(instance)
    }

    override fun attach(observer: Observer<Throwable>) {
        registeredObservers.add(observer)
    }

    override fun detach(observer: Observer<Throwable>) {
        registeredObservers.remove(observer)
    }

    override fun tearDown() {
        Espresso.setFailureHandler(preexistingFailureHandler)
    }

    override fun handle(error: Throwable?, viewMatcher: Matcher<View>?) {
        notify(error)
        preexistingFailureHandler?.handle(error, viewMatcher)
    }

    override fun notify(t: Throwable?) {
        // distinguish between exceptions due to inability to perform the action or due to application exceptions
        // applications exceptions will be discernible as the cause of a PerformException
        if (t?.cause != null && t.cause !is PerformException) {
            registeredObservers.forEach { it.update(t) }
        }
    }
}
