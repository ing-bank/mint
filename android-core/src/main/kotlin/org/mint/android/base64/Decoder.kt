package com.ing.mint.android.base64

import android.annotation.SuppressLint

object Decoder {
    fun apply(): Base64Decoder {
        return JDKBase64Decoder()
    }

    interface Base64Decoder {
        fun decode(s: String): ByteArray
    }

    // note that we can suppress the NewApi warnings because this is only executed on the JVM
    class JDKBase64Decoder : Base64Decoder {
        @SuppressLint("NewApi")
        val decoder = java.util.Base64.getDecoder()

        @SuppressLint("NewApi")
        override fun decode(s: String): ByteArray = decoder.decode(s)
    }
}
