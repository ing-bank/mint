package com.ing.mint.android.base64

import org.junit.Assert
import org.junit.Test

class JDKEncoderTest {
    @Test
    fun jdkEncoderTest() {
        val encoder = Encoder.apply()
        Assert.assertNotNull(encoder)
        Assert.assertEquals(encoder.encode("abc".encodeToByteArray()), "YWJj")
    }
}
