package com.imallan.cloudclipboard

import android.app.Activity
import android.arch.lifecycle.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.widget.CompoundButton
import android.widget.Switch
import java.util.*
import java.util.Observer

class MainActivity : AppCompatActivity(), LifecycleRegistryOwner {

    private val registry = LifecycleRegistry(this)


    override fun getLifecycle(): LifecycleRegistry = registry


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycle.addObserver(MyObserver(this))
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

}

class MyObserver(val activity: Activity) : LifecycleObserver, Observer {

    var boundService: ClipboardSyncService? = null
    val switch: Switch by activity.bind(R.id.switch_sync)

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {
            boundService = null
            switch.isChecked = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ClipboardSyncService.ClipboardSyncBinder
            boundService = binder.getService()
            switch.isChecked = boundService?.runningObservable?.running ?: false
            boundService?.runningObservable?.addObserver(this@MyObserver)
        }
    }

    override fun update(o: Observable?, arg: Any?) {
        switch.isChecked = boundService?.runningObservable?.running ?: false
    }

    val checkedListener: (CompoundButton, Boolean) -> Unit = { _, isChecked ->
        val serviceIntent = Intent(activity, ClipboardSyncService::class.java)
        when (isChecked) {
            true -> serviceIntent.putExtra("TYPE", "START")
            false -> serviceIntent.putExtra("TYPE", "STOP")
        }
        activity.startService(serviceIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        val serviceIntent = Intent(activity, ClipboardSyncService::class.java)
        activity.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        switch.setOnCheckedChangeListener(checkedListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        boundService?.runningObservable?.deleteObserver(this)
        activity.unbindService(serviceConnection)
        boundService = null
    }

}

