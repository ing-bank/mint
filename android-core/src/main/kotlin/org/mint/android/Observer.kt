package org.mint.android

/**
 * Observer of a specific aspect of the SUT, being informed of changes regarding it.
 */
interface Observer<T> {
    fun update(t: T?)
}
