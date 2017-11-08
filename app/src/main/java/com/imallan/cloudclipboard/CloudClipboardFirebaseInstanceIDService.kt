package com.imallan.cloudclipboard

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * User: yilun
 * Date: 11/07/2017
 */
class CloudClipboardFirebaseInstanceIDService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        super.onTokenRefresh()

        val refreshToken = FirebaseInstanceId.getInstance().token

        logd("REFRESH TOKEN: $refreshToken")
    }
}
