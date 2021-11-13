package com.sgf.eventportsimple

import android.app.Application
import android.content.Context
import com.sgf.eventport.EventPort
import com.sgf.eventport.annotation.ReceiveEvent
import com.sgf.eventrouter.AppContextEvent

@ReceiveEvent
class App : Application() , AppContextEvent {

    init {
        EventPort.inject(this)
    }

    override fun getAppContext(): Context {
        return applicationContext
    }
}