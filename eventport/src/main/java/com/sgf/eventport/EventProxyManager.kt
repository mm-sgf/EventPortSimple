package com.sgf.eventport

import android.util.Log
import java.lang.ref.SoftReference

class EventProxyManager {

    private val eventProxyMap = mutableMapOf<String, SoftReference<Any>>()
    private val eventHandlerMap = mutableMapOf<String, SoftReference<EventHandlerImpl<*>>>()

    companion object {
        private const val TAG = "EventManager"
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            EventProxyManager()
        }
    }

    fun <T> findEventProxy(eventClass : Class<T>): T? {
        var obj = eventProxyMap[eventClass.name]?.get()
        if (obj == null) {
            val proxyClassName = "${eventClass.name}_Proxy"
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

    fun <T> findEventCallProxy(eventClass : Class<T>): EventHandlerImpl<T> {
        var obj = eventHandlerMap[eventClass.name]?.get()
        if (obj == null) {
            val proxyClassName = "${eventClass.name}_Proxy"
            try {
                val eventProxy = Class.forName(proxyClassName).newInstance()
                obj = EventHandlerImpl(eventProxy as T)
                eventHandlerMap[proxyClassName] = SoftReference(obj)
            } catch (e : Exception) {
                Log.e(TAG, "create proxy class fail , proxy name  : $proxyClassName" )
            }
        }

        return if (obj == null) {
            Log.e(TAG, "don`t find proxy class , interface : ${eventClass.name}" )
            EventHandlerImpl(null)
        } else {
            obj as EventHandlerImpl<T>
        }
    }
}