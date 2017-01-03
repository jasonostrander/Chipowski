package com.jwo.chipowski

/**
 * Created by j.ostrander on 12/21/16.
 */

class Chip8() {
    var drawFlag = false
    var opcode: Short = 0
    val memory = ByteArray(4096)
    val V = ByteArray(16)
    var I: Short = 0
    var pc: Int = 0
    val gfx = ByteArray(64 * 32)
    var delay_timer: Short = 0
    var sound_timer: Short = 0
    val stack = IntArray(16)
    var sp: Int = 0
    val key = ByteArray(16)

//    val opMap = mapOf<Int, Function<Unit>>(
//            0xA000 to Chip8::ANNN
//    )

    fun init() {
        opcode = 0
        memory.fill(0, 0, memory.size)
        V.fill(0, 0, V.size)
        I = 0
        pc = 0x200
        gfx.fill(0, 0, gfx.size)
        delay_timer = 0
        sound_timer = 0
        stack.fill(0, 0, stack.size)
        sp = 0
        key.fill(0, 0, key.size)

        // TODO: load font set
        for (i in 0..80) {
//            memory[i] = chip8_fontset[i]
        }

        // TODO: Load program
//        for (i in 0..bufferSize) {
//            memory[i + 512] = buffer[i]
//        }
    }

    fun loadGame(s: String) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun emulateCycle() {
        // fetch opcode
        opcode = (memory[pc].toInt() shl 8 or memory[pc + 1].toInt()).toShort()

        // decode and execute opcode
        decodeAndExecuteOpcode()
//        val f = opMap[opcode.toInt() and 0xF000]
//        f.run {  }

        // update timers
        if (delay_timer > 0) --delay_timer
        if (sound_timer > 0) {
            if (sound_timer.toInt() == 1) {
                // TODO: make beep sound
            }
            --sound_timer // TODO: check for overflow
        }
    }

    fun setKeys() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun decodeAndExecuteOpcode()  = when (opcode.toInt() and 0xF000) {
        0x0000 -> {
            when (opcode.toInt() and 0x00ff) {
                0x00e0 -> {
                    // Clear screen
                    gfx.fill(0, 0, gfx.size)
                    pc += 2
                }
                0x00ee -> {
                    // returns from subroutine
                    pc = stack[sp] and 0x0fff
                    --sp
                }
                else -> {
                    throw UnsupportedOperationException("RCA 1802 program. Should not need this")
                }
            }
        }
        0x1000 -> {
            // goto NNN
            pc = opcode.toInt() and 0x0fff
        }
        0x2000 -> {
            // call subroutine at NNN
            stack[sp] = pc
            sp++
            pc = opcode.toInt() and 0x0fff
        }
        0xA000 -> {
            I = (opcode.toInt() and 0x0fff).toShort()
            pc += 2
        }
        else -> {
            throw UnsupportedOperationException("Unknown opcode $opcode")
        }
    }
}

val chip8: Chip8 = Chip8()

fun mainLoop() {
    // Setup render system and input
    setupGraphics()
    setupInput()

    // Init chip8 system and load game
    chip8.init()
    chip8.loadGame("pong")

    // game loop
    while (true) {
        // emulate one cycle
        chip8.emulateCycle()

        // if draw flag set, update the screen
        if (chip8.drawFlag)
            drawGraphics()

        // Store key press state
        chip8.setKeys()
    }
}

fun drawGraphics() {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
}

fun setupInput() {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
}

fun setupGraphics() {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
}