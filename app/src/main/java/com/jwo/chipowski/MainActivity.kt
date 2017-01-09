package com.jwo.chipowski

import android.os.*
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    val TIMESTEP = 2000L // 2s
    val chip8 = Chip8()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chip8.init()
        // load game
        val inputstream = assets.open("c8games/PONG")
        val game = inputstream.readBytes(256)

        chip8.loadGame(game)
        chip8.memory.slice(0x200..0x200 + 20).forEach { logcat("${toHex(it)}") }

        val thread = HandlerThread("chip8 emulator")
        thread.start()
        val handler = MyHandler(thread.looper)
        handler.sendMessage(Message.obtain())
    }

    inner class MyHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            chip8.emulateCycle()

            if (chip8.drawFlag) {
                // update graphics
            }

            // set current keys state
//            chip8.setKeys()

            // Run every 15ms
            sendMessageDelayed(Message.obtain(), TIMESTEP)
        }
    }
}
