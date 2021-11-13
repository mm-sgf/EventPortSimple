package com.sgf.eventrouter.module

import com.sgf.eventport.annotation.SingleEvent

@SingleEvent
interface ModuleMessageEvent {

    fun getModuleName() : String

    fun getModuleTime() : Long

}