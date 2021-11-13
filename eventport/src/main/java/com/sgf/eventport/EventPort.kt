package com.sgf.eventport

import android.util.Log

object EventPort {
    private const val TAG = "EventPort"
    private val eventManager = EventManager.instance
    private val eventProxyManager = EventProxyManager.instance

    fun <T> findEventHandler(eventClass : Class<T>): T? {
        return eventProxyManager.findEventProxy(eventClass)
    }

    fun inject(obj: Any) {
        Log.d(TAG, "inject: ${obj.javaClass.name}")
        eventManager.inject(obj)
    }
}