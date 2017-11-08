package com.imallan.cloudclipboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class ClipboardSyncService : Service() {

    private val binder = ClipboardSyncBinder()

    companion object {
        private const val CHANNEL_ID = "default_channel_id"
    }

    var clipText: CharSequence = ""
    val clipboardManager by lazy {
        val cm = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.primaryClip = ClipData.newPlainText("", "")
//        val primaryClip = cm.primaryClip
//        clipText = if (primaryClip.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
//            primaryClip.getItemAt(0)?.text ?: "" else ""
        cm
    }
    val firebaseListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
        }

        override fun onDataChange(ds: DataSnapshot) {
            clipText = ds.getValue(String::class.java) ?: ""
            clipboardManager.primaryClip = ClipData.newPlainText("CloudSyncClipbaord", clipText)
        }
    }
    val db by lazy { FirebaseDatabase.getInstance() }

    private val listener = {
        if (clipboardManager.hasPrimaryClip()) {
            val primaryClip = clipboardManager.primaryClip
            if (primaryClip.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                val clip = "${primaryClip.getItemAt(0)?.text}"
                if (clip != clipText) {
                    db.getReference("clip").setValue(clip)
                    clipText = clip
                }
            } else if (primaryClip.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_URILIST)) {

                logd("${primaryClip.getItemAt(0)?.uri}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        logd("CREATE")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "CloudClipboard", NotificationManager.IMPORTANCE_DEFAULT)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        logd("ONBIND")
        return binder
    }

    var runningObservable: RunningObservable = RunningObservable()

    class RunningObservable : Observable() {
        var running = false
            set(value) {
                if (field != value) {
                    field = value
                    setChanged()
                    notifyObservers()
                }
            }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.getStringExtra("TYPE")) {
            "START" -> {
                val cancelIntent = Intent(this, this::class.java)
                val cancelAction = NotificationCompat.Action.Builder(
                        R.drawable.ic_content_paste_24dp,
                        "STOP", PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                        .build()
                val notification = NotificationCompat.Builder(this)
                        .setContentTitle("CloudClipboard is Syncing...")
                        .setSmallIcon(R.drawable.ic_sync_24dp)
                        .addAction(cancelAction)
                        .setContentIntent(PendingIntent.getActivity(
                                this, 0,
                                Intent(this, MainActivity::class.java),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .setAutoCancel(false)
                        .setChannelId(CHANNEL_ID)
                        .build()
                startForeground(12333, notification)
                runningObservable.running = true
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(12334)
                clipboardManager.removePrimaryClipChangedListener(listener)
                clipboardManager.addPrimaryClipChangedListener(listener)
                db.getReference("clip").addValueEventListener(firebaseListener)
            }
            else -> {
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
                        .setChannelId(CHANNEL_ID)
                        .build()
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(12334, notification)
                clipboardManager.removePrimaryClipChangedListener(listener)
                stopForeground(true)
                stopSelf()
                runningObservable.running = false
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logd("ONUNBIND")
        return true
    }

    override fun onRebind(intent: Intent?) {
        logd("ONREBIND")
        super.onRebind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        logd("DESTROY")
    }

    inner class ClipboardSyncBinder : Binder() {
        fun getService(): ClipboardSyncService {
            return this@ClipboardSyncService
        }
    }

}
