package com.sgf.eventport

import android.util.Log
import java.lang.ref.WeakReference

class EventManager {

    private val eventObjMapping = mutableMapOf<String, WeakReference<Any>>()

    private var eventMapping : EventMapping? = null

    companion object {
        private const val TAG = "EventManager"
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            EventManager()
        }
    }

    fun inject(obj : Any) {
        val key = obj.javaClass.name
        Log.d(TAG,"registerEvent key: $key" )
        if (!eventObjMapping.containsKey(key)) {
            eventObjMapping[key] = WeakReference(obj)
        }
    }

    fun unregisterEvent(obj: Any) {
        val key = obj::class.java.name
        if (eventObjMapping.containsKey(key)) {
            eventObjMapping.remove(key)?.clear()
        }
    }

    fun findEvent(key : String) : Any? {
        Log.d(TAG,"findEvent key: $key" )
        return eventObjMapping[key]?.get()
    }

    fun <T> findEventInterfaceList(eventClass : Class<T>): List<T>? {
        loadEventMappingIfNeed()
        val receiveClassNameList = eventMapping?.getMultiReceive(eventClass.name)
        val anyList = mutableListOf<T>()
        receiveClassNameList?.forEach { receiveClassName ->
            eventObjMapping[receiveClassName]?.get()?.let {
              anyList.add(it as T)
            }
        }
        return anyList
    }

    fun <T> findEventInterface(eventClass : Class<T>): T? {
        loadEventMappingIfNeed()
        val receiveClassName = eventMapping?.getSingleReceive(eventClass.name)
        val obj = eventObjMapping[receiveClassName]?.get()
        return if (obj == null) {
            Log.e(TAG, "don`t find event interface : ${eventClass.name}" )
            null
        } else {
            obj as T
        }
    }

    private fun loadEventMappingIfNeed() {
        if (eventMapping == null) {
            try {
                eventMapping = Class.forName("com.cfox.fastevent.ReceiveEventMapManger").newInstance() as EventMapping?
            } catch (e : Exception) {
                Log.e(TAG, "create proxy class fail , proxy name  :com.cfox.fastevent.ReceiveEventMapManger" )
            }
        }
    }
}