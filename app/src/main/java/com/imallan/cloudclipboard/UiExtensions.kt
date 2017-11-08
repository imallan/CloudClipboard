package com.imallan.cloudclipboard

import android.app.Activity
import android.support.annotation.IdRes
import android.view.View

fun <V : View> Activity.bind(@IdRes id: Int): Lazy<V> = lazy {
    findViewById<V>(id)
}
