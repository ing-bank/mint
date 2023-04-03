package org.mint.android.base64

import android.annotation.SuppressLint
import android.util.Base64
import java.lang.RuntimeException

/**
 * This object creates a Base64 Encoder instance.
 *
 * On the JVM and on ART, this needs to be done in a different manner.
 * We assume we run on ART, and fall back to the JVM one.
 */
object Encoder {
    fun apply(): Base64Encoder {
        return try {
            Class.forName("android.util.Base64")
            // now it might be a mocked object, let's verify
            try {
                // If this invocation works, we know it's there and also not mocked
                AndroidBase64Encoder().encode("".encodeToByteArray())
                return AndroidBase64Encoder()
            } catch (e: RuntimeException) {
                if (e.message?.startsWith("Method encodeToString in") == true) {
                    return JDKBase64Encoder()
                } else {
                    throw IllegalStateException("I don't know how to get a valid Base64 encoder anymore")
                }
            }
        } catch (e: ClassNotFoundException) {
            JDKBase64Encoder()
        }
    }

    interface Base64Encoder {
        fun encode(b: ByteArray): String
    }

    class AndroidBase64Encoder : Base64Encoder {
        private val opts = Base64.NO_CLOSE + Base64.NO_WRAP + Base64.NO_PADDING
        override fun encode(b: ByteArray): String =
            Base64.encodeToString(b, opts)
    }

    // note that we can suppress the NewApi warnings because this is only executed on the JVM
    class JDKBase64Encoder : Base64Encoder {
        @SuppressLint("NewApi")
        val encoder = java.util.Base64.getEncoder().withoutPadding()

        @SuppressLint("NewApi")
        override fun encode(b: ByteArray): String =
            encoder.encodeToString(b)
    }
}
