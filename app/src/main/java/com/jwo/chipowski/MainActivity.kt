package com.jwo.chipowski

import android.os.*
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {
    val TIMESTEP = 20L // 2s
    val chip8 = Chip8()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init emulator
        chip8.init()

        // load game
        val inputstream = assets.open("c8games/PONG")
        val game = inputstream.readBytes(256)
        chip8.loadGame(game)

        // Start emulation
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
                val any = chip8.gfx.any { it == 1.toByte() }
                logcat("drawing $any")
                runOnUiThread { activity_main.chip8view.graphics = chip8.gfx }
            }

            // set current keys state
//            chip8.setKeys()

            // Run every 15ms
            sendMessageDelayed(Message.obtain(), TIMESTEP)
        }
    }
}
