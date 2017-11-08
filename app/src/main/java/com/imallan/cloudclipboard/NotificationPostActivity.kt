package com.imallan.cloudclipboard

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity

class NotificationPostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val startIntent = Intent(this, this::class.java).apply {
            putExtra("TYPE", "START")
        }
        val startAction = NotificationCompat.Action.Builder(
                R.drawable.ic_content_paste_24dp,
                "START",
                PendingIntent.getService(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build()
        val notification = NotificationCompat.Builder(this)
                .setContentTitle("CloudClipboard")
                .setSmallIcon(R.drawable.ic_sync_disabled_24dp)
                .setContentIntent(PendingIntent.getActivity(
                        this, 0,
                        Intent(this, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .addAction(startAction)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build()
        nm.notify(123133, notification)

        finish()
    }

}
