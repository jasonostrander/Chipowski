package com.jwo.chipowski

import android.os.*
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {
    val TIMESTEP = 8L
    val chip8 = Chip8()
    lateinit var handler: GameLoopHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity_main.load_game.setOnClickListener { showGameMenu() }

        activity_main.keyboard.layoutManager = GridLayoutManager(this, 4)
        activity_main.keyboard.adapter = KeyAdapter()

        // Init emulator
        chip8.debug = true
        chip8.init()

//        // load game
        val inputstream = assets.open("c8games/INVADERS")
        val game = inputstream.readBytes(256)
        chip8.loadGame(game)

        // Start emulation
        val thread = HandlerThread("chip8 emulator")
        thread.start()
        handler = GameLoopHandler(thread.looper)
    }

    inner class KeyHolder(val button: Button) : RecyclerView.ViewHolder(button) {
        fun bind(position: Int) {
            button.text = "0123456789ABCDEF".substring(position, position+1)
            button.setOnTouchListener { view, motionEvent ->
                when(motionEvent.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        chip8.setKey(position, true)
                        false
                    }
                    MotionEvent.ACTION_UP -> {
                        chip8.setKey(position, false)
                        false
                    }
                    else -> {
                        false
                    }
                }
            }
        }
    }

    inner class KeyAdapter : RecyclerView.Adapter<KeyHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): KeyHolder {
            return KeyHolder(LayoutInflater.from(parent?.context).inflate(R.layout.key, parent, false) as Button)
        }

        override fun onBindViewHolder(holder: KeyHolder?, position: Int) {
            holder?.bind(position)
        }

        override fun getItemCount(): Int = 16
    }

    private fun showGameMenu() {
        val adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_single_choice,
                assets.list("c8games"));
        AlertDialog.Builder(this)
                .setTitle("Select a game")
                .setNegativeButton("Cancel", { dialogInterface, i -> dialogInterface.dismiss() })
                .setAdapter(adapter, { dialogInterface, i ->
                    chip8.init()
                    val inputstream = assets.open("c8games/${adapter.getItem(i)}")
                    val game = inputstream.readBytes(256)
                    chip8.loadGame(game)
                    dialogInterface.dismiss()
                })
                .show()
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

            // Run every 15ms
            sendMessageDelayed(Message.obtain(), TIMESTEP)
        }
    }
}
