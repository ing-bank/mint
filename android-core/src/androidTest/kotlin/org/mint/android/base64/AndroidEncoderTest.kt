package org.mint.android.base64

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AndroidEncoderTest {

    @Test
    fun mintDefaultExampleTestRun() {
        val encoder = Encoder.apply()
        Assert.assertNotNull(encoder)
        Assert.assertEquals(encoder.encode("abc".encodeToByteArray()), "YWJj")
    }
}
