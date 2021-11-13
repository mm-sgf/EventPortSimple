package com.sgf.eventport

import android.util.Log
import java.lang.ref.SoftReference

class EventProxyManager {
    private val eventProxyMap = mutableMapOf<String, SoftReference<Any>>()
    companion object {
        private const val TAG = "EventManager"
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            EventProxyManager()
        }
    }

    fun <T> findEventProxy(eventClass : Class<T>): T? {
        var obj = eventProxyMap[eventClass.name]?.get()
        if (obj == null) {
            val proxyClassName = "${eventClass.name}_proxy"
            try {
                obj = Class.forName(proxyClassName).newInstance()
                eventProxyMap[proxyClassName] = SoftReference(obj)
            } catch (e : Exception) {
                Log.e(TAG, "create proxy class fail , proxy name  : $proxyClassName" )
            }
        }

        return if (obj == null) {
            Log.e(TAG, "don`t find proxy class , interface : ${eventClass.name}" )
            null
        } else {
            obj as T
        }
    }
}