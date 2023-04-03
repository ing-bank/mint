package org.mint.android.rule.input

import com.github.curiousoddman.rgxgen.RgxGen
import org.mint.android.AndroidState

data class RegexInputGenerator(val regex: String) : (AndroidState) -> String {
    private val rgx = RgxGen(regex)

    override fun invoke(s: AndroidState): String = rgx.generate()
}
