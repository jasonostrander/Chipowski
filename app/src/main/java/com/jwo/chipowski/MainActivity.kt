package com.jwo.chipowski

import android.os.*
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {
    val TIMESTEP = 20L // 2s
    val chip8 = Chip8()
    lateinit var handler: GameLoopHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init emulator
        chip8.init()

        // load game
        val inputstream = assets.open("c8games/INVADERS")
        val game = inputstream.readBytes(256)
        chip8.loadGame(game)

        // Start emulation
        val thread = HandlerThread("chip8 emulator")
        thread.start()
        handler = GameLoopHandler(thread.looper)
    }

    override fun onResume() {
        super.onResume()
        handler.sendMessage(Message.obtain())
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    inner class GameLoopHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            chip8.emulateCycle()

            if (chip8.drawFlag) {
                // update graphics
                runOnUiThread { activity_main.chip8view.graphics = chip8.gfx }
            }

            // set current keys state
//            chip8.setKeys()

            // Run every 15ms
            sendMessageDelayed(Message.obtain(), TIMESTEP)
        }
    }
}
