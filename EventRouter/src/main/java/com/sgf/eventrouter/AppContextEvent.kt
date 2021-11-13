package com.sgf.eventrouter

import android.content.Context
import com.sgf.eventport.annotation.SingleEvent

@SingleEvent
interface AppContextEvent {
    fun getAppContext() : Context
}