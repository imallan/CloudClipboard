package com.imallan.cloudclipboard

import android.util.Log


fun Any.logd(msg: String, tag: String? = javaClass.simpleName) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, msg)
    }
}

fun Any.logw(msg: String, tag: String? = javaClass.simpleName) {
    if (BuildConfig.DEBUG) {
        Log.w(tag, msg)
    }
}

fun Any.loge(msg: String, throwable: Throwable? = null, tag: String? = javaClass.simpleName) {
    if (BuildConfig.DEBUG) {
        if (throwable == null) {
            Log.e(tag, msg)
        } else {
            Log.e(tag, msg, throwable)
        }
    }
}

fun Any.logExecution(msg: String, tag: String? = javaClass.simpleName, func: () -> Unit) {
    if (BuildConfig.DEBUG) {
        val start = System.currentTimeMillis()
        logd("Before Execution: $msg", tag)
        func()
        logd("After Execution: $msg", tag)
        val dt = System.currentTimeMillis() - start
        logd("Time elapsed: $dt")
    } else {
        func()
    }
}

