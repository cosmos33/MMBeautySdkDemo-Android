package com.cosmos.beautycomposition

object ClassHelper {
    fun getClass(className: String): Class<Any>? {
        var activityClass: Class<Any>? = null
        try {
            activityClass = Class.forName(className) as Class<Any>?
        } catch (e: Exception) {
        }
        return activityClass
    }
}