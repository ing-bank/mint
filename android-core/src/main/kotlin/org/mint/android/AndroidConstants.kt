package com.ing.mint.android

interface AndroidConstants {
    companion object {
        // Special namespaces for MINT/espresso
        val ABSTRACT_NS = "http://com.ing.mint/espresso/abstract"
        val ACTION_NS = "http://com.ing.mint/espresso/action"
        val ABSTRACT_ACTION_NS = "http://com.ing.mint/espresso/abstract/action"
        val RULE_NS = "http://com.ing.mint/espresso/rule"
        val CORRELATE_NS = "http://com.ing.mint/espresso/correlate"
        val PROBE_NS = "https://com.ing.mint/espresso/probe"
        val ORACLE_NS = "https://com.ing.mint/espresso/oracle"

        // Special XML elements
        val MODIFIER = "modifier"
        val MULTIPLICATIVE = "multiplicative"
        val CLASS = "class"
        val PRIORITY = "priority"
        val RESOURCE_NAME = "resourceName"
        val TEXT = "text"
        val POSITION = "position"
        val TAG = "tag"
        val ACTION = "action"
        val MISSING = "missing"
        val COUNT = "count"
        val APPLIED = "applied"
        val ERROR_MESSAGE = "errorMessage"
    }
}
