package com.sgf.eventport.callback

interface Call<T> {

    fun call(key : String , t : T?)
}