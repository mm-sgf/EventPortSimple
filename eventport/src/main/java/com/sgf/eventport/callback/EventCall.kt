package com.sgf.eventport.callback

interface EventCall {

    fun call(key : String, any: Any)

}