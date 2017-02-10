package com.jwo.chipowski

import java.util.*

/**
 * Created by j.ostrander on 12/21/16.
 */

class Chip8() {
    var debug = false
    var drawFlag = false
    var beepFlag = false
    var opcode: Short = 0
    val memory = ByteArray(4096)
    val V = ByteArray(16)
    var I: Int = 0
    var pc: Int = 0
    val gfx = ByteArray(64 * 32)  // 64 x 32 pixel display
    var delay_timer: Byte = 0
    var sound_timer: Byte = 0
    val stack = ShortArray(16)
    var sp: Int = 0
    val keys = BooleanArray(16)
    val random = Random()

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
        keys.fill(false, 0, keys.size)
        drawFlag = false
        beepFlag = false

        // load font set
        chip8_fontset.forEachIndexed { i, b -> memory[i] = b.toByte() }
    }

    fun loadGame(game: ByteArray) {
        game.forEachIndexed { i, byte -> memory[i + 0x200] = byte }
    }

    fun nextOpcode(): Short = ((memory[pc].toInt() and 0xff) shl 8 or (memory[pc + 1].toInt() and 0xff)).toShort()

    fun emulateCycle() {
        // fetch opcode
        opcode = nextOpcode()

        if (debug) logcat("opcode($pc) ${toHex(opcode)}")

        // decode and execute opcode
        decodeAndExecuteOpcode()
        if (debug) logcat("registers: " + V.fold("") {s, b -> s + " ${b.toHexString()}"})
        if (debug) logcat("stack ($sp): " + stack.fold("") {s, b -> s + " $b"})
        if (debug) logcat("I = $I")
        if (debug) logcat("delay_timer=$delay_timer sound_timer=$sound_timer")

        // update timers
        if (delay_timer.toInt() and 0xff > 0) --delay_timer
        if (sound_timer.toInt() and 0xff > 0) {
            if (sound_timer.toInt() == 1) {
                beepFlag = true
            }
            --sound_timer
        }
    }

    fun setKey(position: Int, value: Boolean) {
        this.keys[position] = value
    }

    fun setKeys(keys: BooleanArray) {
        keys.forEachIndexed { i, b -> this.keys[i] = b }
    }

    fun decodeAndExecuteOpcode()  = when (opcode.toInt() and 0xF000) {
        0x0000 -> when (opcode.toInt() and 0x00ff) {
            0x00e0 -> {
                // Clear screen
                gfx.fill(0, 0, gfx.size)
                pc += 2
            }
            0x00ee -> {
                // returns from subroutine
                --sp
                pc = stack[sp].toInt() and 0xffff
                pc += 2
            }
            else -> {
                throw UnsupportedOperationException("RCA 1802 program. Should not need this")
            }
        }
        0x1000 -> {
            // goto NNN
            pc = opcode.toInt() and 0x0fff
        }
        0x2000 -> {
            // call subroutine at NNN
            stack[sp] = pc.toShort()
            sp++
            pc = opcode.toInt() and 0x0fff
        }
        0x3000 -> {
            val x = opcode.toInt().shr(8) and 0xf
            val nn: Byte = (opcode.toInt() and 0x00ff).toByte()
            pc += if (V[x] == nn) {
                4
            } else {
                2
            }
        }
        0x4000 -> {
            val x = opcode.toInt().shr(8) and 0xf
            val nn: Byte = (opcode.toInt() and 0x00ff).toByte()
            pc += if (V[x] != nn) {
                4
            } else {
                2
            }
        }
        0x5000 -> {
            val x = opcode.toInt().shr(8) and 0xf
            val y = opcode.toInt().shr(4) and 0xf
            pc += if (V[x] == V[y]) {
                4
            } else {
                2
            }
        }
        0x6000 -> {
            val nn = opcode.toInt() and 0xff
            val x = opcode.toInt().shr(8) and 0xf
            V[x] = nn.toByte()
            pc += 2
        }
        0x7000 -> {
            val nn = opcode.toInt() and 0xff
            val x = opcode.toInt().shr(8) and 0xf
            V[x] = (V[x] + nn.toByte()).toByte()
            pc += 2
        }
        0x8000 -> {
            val x = opcode.toInt().shr(8) and 0xf
            val y = opcode.toInt().shr(4) and 0xf
            when (opcode.toInt() and 0xf) {
                0x0 -> {
                    V[x] = V[y]
                }
                0x1 -> {
                    V[x] = (V[x].toInt() or V[y].toInt()).toByte()
                }
                0x2 -> {
                    V[x] = (V[x].toInt() and V[y].toInt()).toByte()
                }
                0x3 -> {
                    V[x] = (V[x].toInt() xor V[y].toInt()).toByte()
                }
                0x4 -> {
                    val a = (V[x].toInt() and 0xff) + (V[y].toInt() and 0xff)
                    V[0xf] = if (a and 0x100 == 0x100) 1 else 0 // carry bit
                    V[x] = a.toByte()
                }
                0x5 -> {
                    val a = (V[x].toInt() and 0xff) - (V[y].toInt() and 0xff)
                    V[0xf] = if (a and 0x100 == 0x100) 0 else 1 // borrow bit
                    V[x] = a.toByte()
                }
                0x6 -> {
                    V[0xf] = (V[x].toInt() and 0x1).toByte()
                    V[x] = V[x].toInt().shr(1).toByte()
                }
                0x7 -> {
                    val c = (V[y].toInt() and 0xff) - (V[x].toInt() and 0xff)
                    V[0xf] = if (c and 0x100 == 0x100) 0 else 1 // borrow bit
                    V[x] = c.toByte()
                }
                0xe -> {
                    V[0xf] = (V[x].toInt() and 0x80).shr(7).toByte()
                    V[x] = V[x].toInt().shl(1).toByte()
                }
                else -> {
                    throw UnsupportedOperationException("Unknown opcode $opcode")
                }
            }
            pc += 2
        }
        0x9000 -> {
            val x = opcode.toInt().shr(8) and 0xf
            val y = opcode.toInt().shr(4) and 0xf
            pc += if (V[x] == V[y]) 2 else 4
        }
        0xA000 -> {
            I = opcode.toInt() and 0x0fff
            pc += 2
        }
        0xB000 -> {
            pc = (opcode.toInt() and 0xfff) + V[0]
        }
        0xC000 -> {
            val x = opcode.toInt().shr(8) and 0xf
            val nn = opcode.toInt() and 0xff
            V[x] = (nn and random.nextInt()).toByte()
            pc += 2
        }
        0xd000 -> {
            val vx = V[opcode.toInt().shr(8) and 0xf].toInt() and 0xff
            val vy = V[opcode.toInt().shr(4) and 0xf].toInt() and 0xff
            val n = opcode.toInt() and 0xf
            V[0xf] = 0
            for (i in 0..n - 1) {
                val pixel = memory[I + i].toInt()
                for (b in 0..7) {
                    if (pixel and (0x80.shr(b)) != 0) {
                        val j = (vx + b).mod(64) + 64*(vy + i).mod(32)
                        if (gfx[j].toInt() == 1) {
                            V[0xf] = 1
                        }
                        gfx[j] = (gfx[j].toInt() xor 1).toByte()
                    }
                }
            }
            drawFlag = true
            pc += 2
        }
        0xe000 -> {
            val x = opcode.toInt().shr(8) and 0xf
            when (opcode.toInt() and 0x00ff) {
                0x9e -> {
                    pc += if (keys[V[x].toInt()]) 4 else 2
                }
                0xa1 -> {
                    pc += if (!keys[V[x].toInt()]) 4 else 2
                }
                else -> {
                    throw UnsupportedOperationException("Unknown opcode $opcode")
                }
            }
        }
        0xf000 -> {
            val x = opcode.toInt().shr(8) and 0xf
            when (opcode.toInt() and 0xff) {
                0x07 -> {
                    V[x] = delay_timer
                    pc += 2
                }
                0x0a -> {
                    for (i in 0..keys.size - 1) {
                        if (keys[i]) {
                            V[x] = i.toByte()
                            pc += 2
                        }
                    }
                }
                0x15 -> {
                    delay_timer = V[x]
                    pc += 2
                }
                0x18 -> {
                    sound_timer = V[x]
                    pc += 2
                }
                0x1e -> {
                    I += V[x].toInt() and 0xff
                    pc += 2
                }
                0x29 -> {
                    I = V[x] * 5
                    pc += 2
                }
                0x33 -> {
                    val vx = V[x].toInt() and 0xff
                    memory[I] = ((vx/100) % 10).toByte()
                    memory[I+1] = ((vx/10) % 10).toByte()
                    memory[I+2] = (vx % 10).toByte()
                    pc += 2
                }
                0x55 -> {
                    for (i in 0..x) {
                        memory[I + i] = V[i]
                    }
                    pc += 2
                }
                0x65 -> {
                    for (i in 0..x) {
                        V[i] = memory[I + i]
                    }
                    pc += 2
                }
                else -> {
                    throw UnsupportedOperationException("Unknown opcode $opcode")
                }
            }
        }
        else -> {
            throw UnsupportedOperationException("Unknown opcode $opcode")
        }
    }
}
