package com.jwo.chipowski

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message

/**
 * Created by j.ostrander on 1/30/17.
 */
interface GameView {
    fun update(gfx: ByteArray)
}

class GameHandler(val view: GameView, debug: Boolean = false) {
    val TIMESTEP = 1L
    val chip8 = Chip8()
    val handler:GameLoop
    val uiHandler = Handler(Looper.getMainLooper())

    init {
        chip8.debug = debug
        chip8.init()

        val thread = HandlerThread("GameLoop")
        thread.start()
        handler = GameLoop(thread.looper)
    }

    fun loadGame(data: ByteArray) {
        handler.post {
            chip8.init()
            chip8.loadGame(data)
        }
    }

    fun setKey(position: Int, value: Boolean) {
        handler.post { chip8.setKey(position, value) }
    }

    fun start() = handler.sendMessage(Message.obtain())
    fun pause() = handler.removeCallbacksAndMessages(null)

    inner class GameLoop(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            chip8.emulateCycle()

            if (chip8.drawFlag) {
                // update graphics
                val updatedGfx = chip8.gfx
                uiHandler.post { view.update(updatedGfx) }
            }

            // Run every 15ms
            sendMessageDelayed(Message.obtain(), TIMESTEP)
        }
    }
}
