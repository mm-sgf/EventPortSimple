package com.sgf.eventport

import com.sgf.eventport.callback.Call
import com.sgf.eventport.callback.EventCall
import com.sgf.eventport.callback.EventCallHandler
import com.sgf.eventport.callback.EventCallInterface


class EventHandlerImpl<T>(private val t: T ?) : EventCallHandler<T> {
    override fun <V> setEventCallback(call: Call<V>) : EventHandler<T> {
        if (t is EventCallInterface) {
            t.setCallback(object : EventCall {
                override fun call(key: String, any: Any) {
                    call.call(key,any as V)
                }
            })
        }
        return this
    }
    override fun get() : T? {
        return t
    }
}