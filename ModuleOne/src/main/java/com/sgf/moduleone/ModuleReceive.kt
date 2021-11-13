package com.sgf.moduleone

import android.util.Log
import android.widget.Toast
import com.sgf.eventport.EventPort
import com.sgf.eventport.annotation.ReceiveEvent
import com.sgf.eventrouter.AppContextEvent
import com.sgf.eventrouter.module.ModuleMultiEvent
import com.sgf.eventrouter.module.ModuleSingleEvent

@ReceiveEvent
class ModuleReceive : ModuleMultiEvent, ModuleSingleEvent {
    companion object {
        private const val TAG = "ModuleReceive"
    }

    private val message : ModuleMessage = ModuleMessage()

    init {
        EventPort.inject(this)
    }

    override fun printModuleMessage(msg : String) {
        val context = EventPort.findEventHandler(AppContextEvent::class.java)?.getAppContext()
        Log.d(TAG, "printModuleMessage =====> $msg")
        context?.let {
            Toast.makeText(it, "ModuleReceive printModuleMessage: $msg", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getCode(): Int {
        Log.d(TAG, "getCode =====>")
        return 1000
    }
}