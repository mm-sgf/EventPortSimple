package com.sgf.eventport

interface EventHandler<T> {
    fun get() : T?
}