package com.sgf.moduleone

import com.sgf.eventport.EventPort
import com.sgf.eventport.annotation.ReceiveEvent
import com.sgf.eventrouter.module.ModuleMessageEvent

@ReceiveEvent
class ModuleMessage : ModuleMessageEvent {

    init {
        EventPort.inject(this)
    }

    override fun getModuleName(): String {
        return "Module One"
    }

    override fun getModuleTime(): Long {
        return System.currentTimeMillis()
    }
}