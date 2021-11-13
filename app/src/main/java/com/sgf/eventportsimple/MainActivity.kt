package com.sgf.eventportsimple

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.sgf.eventport.EventPort
import com.sgf.eventport.annotation.ReceiveEvent
import com.sgf.eventrouter.module.ModuleMessageEvent
import com.sgf.eventrouter.module.ModuleMultiEvent
import com.sgf.moduleone.ModuleReceive
import com.sgf.eventrouter.module.ModuleSingleEvent


@ReceiveEvent
class MainActivity : BaseActivity() ,ModuleMultiEvent {
    companion object {
        private const val TAG = "MainActivity"
    }

    private var libEventTest = ModuleReceive()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_send_multi_event).setOnClickListener {
            EventPort.findEventHandler(ModuleMultiEvent::class.java)?.printModuleMessage("main message")
        }
        findViewById<Button>(R.id.btn_send_single_event).setOnClickListener {
            val eventHandler = EventPort.findEventHandler(ModuleSingleEvent::class.java)
            val moduleMessageHandler = EventPort.findEventHandler(ModuleMessageEvent::class.java)
            Toast.makeText(this,
                "module code : ${eventHandler?.getCode()} " +
                        "\n  module name : ${moduleMessageHandler?.getModuleName()} " +
                        "\n  module time : ${moduleMessageHandler?.getModuleTime()}" , Toast.LENGTH_SHORT).show()

        }

        EventPort.inject(this)
    }

    override fun printModuleMessage(msg: String) {
        Log.d(TAG, "printModuleMessage ==main===>")
        Toast.makeText(this, "multi call : $msg", Toast.LENGTH_SHORT).show()
    }

}