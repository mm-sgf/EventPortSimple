package com.sgf.eventrouter.module

import com.sgf.eventport.annotation.MultiEvent

@MultiEvent
interface ModuleMultiEvent {
    fun printModuleMessage(msg: String)
}