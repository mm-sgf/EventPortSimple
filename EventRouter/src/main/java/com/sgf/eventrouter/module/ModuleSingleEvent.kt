package com.sgf.eventrouter.module

import com.sgf.eventport.annotation.SingleEvent

@SingleEvent
interface ModuleSingleEvent {
    fun getCode() : Int
}