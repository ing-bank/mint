package android.util

/**
 * Needed to avoid mocking Android's Log in unit tests.
 */
@Suppress("UNUSED")
class Log {
    companion object {
        @JvmStatic
        fun d(tag: String, message: String): Int {
            return 0
        }
    }
}
