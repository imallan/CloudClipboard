package com.imallan.cloudclipboard

import android.app.Application
import com.google.firebase.iid.FirebaseInstanceId

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val token: String? = FirebaseInstanceId.getInstance().token
        logd("TOKEN: $token")
    }

}
