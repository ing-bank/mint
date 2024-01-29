package com.ing.mint.android.rule.input

import com.github.curiousoddman.rgxgen.RgxGen
import com.ing.mint.android.AndroidState

data class RegexInputGenerator(val regex: String) : (AndroidState) -> String {
    private val rgx = RgxGen(regex)

    override fun invoke(s: AndroidState): String = rgx.generate()
}
