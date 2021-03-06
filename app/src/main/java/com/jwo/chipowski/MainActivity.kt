package com.jwo.chipowski

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {
    val keys = listOf(1, 2, 3, 0xc, 4, 5, 6, 0xd, 7, 8, 9, 0xe, 0xa, 0, 0xb, 0xf)
    val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

    val gameHandler = GameHandler(object : GameInterface {
        override fun beep() {
            tg.startTone(ToneGenerator.TONE_PROP_BEEP)
        }

        override fun update(gfx: ByteArray) {
            activity_main.chip8view.graphics = gfx
        }
    })

    val keyHeight: Int by lazy {
        activity_main.keyboard.height / 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity_main.keyboard.layoutManager = GridLayoutManager(this, 4)
        activity_main.keyboard.adapter = KeyAdapter()

        // load game
        val inputstream = assets.open("c8games/INVADERS")
        val game = inputstream.readBytes(256)
        gameHandler.loadGame(game)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.action_load -> {
            showGameMenu()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    inner class KeyHolder(val button: Button) : RecyclerView.ViewHolder(button) {
        fun bind(position: Int) {
            button.text = "%X".format(keys[position])
            button.height = keyHeight
            button.setOnTouchListener { view, motionEvent ->
                when(motionEvent.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        gameHandler.setKey(keys[position], true)
                        false
                    }
                    MotionEvent.ACTION_UP -> {
                        gameHandler.setKey(keys[position], false)
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
                assets.list("c8games"))
        AlertDialog.Builder(this)
                .setTitle("Select a game")
                .setNegativeButton("Cancel", { dialogInterface, i -> dialogInterface.dismiss() })
                .setAdapter(adapter, { dialogInterface, i ->
                    val inputstream = assets.open("c8games/${adapter.getItem(i)}")
                    val game = inputstream.readBytes(256)
                    gameHandler.loadGame(game)
                    dialogInterface.dismiss()
                })
                .show()
    }

    override fun onResume() {
        super.onResume()
        gameHandler.start()
    }

    override fun onPause() {
        super.onPause()
        gameHandler.pause()
    }

}
