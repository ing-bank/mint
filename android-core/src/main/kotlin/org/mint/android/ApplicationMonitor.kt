package org.mint.android

/**
 * Monitors the SUT of relevant changes for the whole the execution of the MINT test.
 */
interface ApplicationMonitor<T> {

    /**
     * Returns the class of its type
     */
    fun type(): Class<T>

    /**
     * Configures the set up prior to the start of the application
     */
    fun initialize()

    /**
     * Registers an observer to notify it of the application state during the run
     */
    fun attach(observer: Observer<T>)

    /**
     * Notifies the observers of relevant changes in state
     */
    fun notify(t: T?)

    /**
     * Deregisters the supplied observer
     */
    fun detach(observer: Observer<T>)

    /**
     * Reverts any configuration changes at the end of the test
     */
    fun tearDown()
}
