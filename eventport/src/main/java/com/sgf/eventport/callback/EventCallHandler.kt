package com.sgf.eventport.callback

import com.sgf.eventport.EventHandler


interface EventCallHandler<T> : EventHandler<T> {

    fun <V> setEventCallback(call: Call<V>) : EventHandler<T>
}