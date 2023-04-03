package org.mint

/**
 * The
 */
interface MINTApi {
    /** Explore an application, based in the current MINT configuration.
     *
     * All steps that have been registered will be executed first, after which the remaining # steps
     * will be explored according to the current MINT configuration. */
    fun explore()

    /** Register a manually defined that always is executed at the start of the current sequence.
     *
     * Can be invoked multiple times, meaning a series of steps will be configured
     */
    fun step(action: () -> Unit): MINTApi
}
