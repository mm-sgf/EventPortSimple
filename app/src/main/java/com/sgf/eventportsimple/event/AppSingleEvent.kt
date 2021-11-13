package com.sgf.eventportsimple.event

import com.sgf.eventport.annotation.SingleEvent

@SingleEvent
interface AppSingleEvent {
    fun getAppMessage() : String
}