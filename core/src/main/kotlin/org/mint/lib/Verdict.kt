package com.ing.mint.lib

enum class Verdict {
    /**
     * No issues found during the evaluation.
     */
    OK,

    /**
     * Clearly an issue that needs investigation.
     */
    FAIL,

    /**
     * Information that may be helpful for creating a better customer experience.
     */
    INFO,

    /**
     * A potential issue that requires attention.
     */
    WARNING,

    /**
     * No known way to process the type of information acquired.
     */
    DONT_KNOW,
}
