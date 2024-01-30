package com.ing.mint.lib

/** A probe that observes an aspect of an application, such as widget tree, memory usage, service calls, ... */
interface Probe<S : SUTState<S>> {
    val name: String
    val version: String
    val description: String
    val categories: Set<ProbeCategory>

    /** Start this probe (optional method)
     *
     * If the probe is dependent on some asynchronous state management or otherwise needs a way to
     * be initialised before it can be used, override this method. It will be only invoked once
     * for the lifetime of a probe */
    fun start() { }

    /** Stop this probe (optional method)
     *
     * Similar to `start()`, this method is only invoked once for the lifetime of a probe
     */
    fun stop() { }

    /** Execute the actual measurement that this probe produces, and annotate the sut state */
    fun measure(state: S): S
}
