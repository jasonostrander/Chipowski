package com.jwo.chipowski

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Created by j.ostrander on 1/3/17.
 */

class Chip8Test {
    lateinit var chip8: Chip8

    @Before
    fun before() {
        chip8 = Chip8()
    }

    @Test
    fun testInit() {
        chip8.init()

        assertEquals(0x200, chip8.pc)
        assertEquals(0.toShort(), chip8.opcode)
        assertEquals(0, chip8.I)
        assertEquals(0, chip8.sp)
        assertEquals(false, chip8.drawFlag)

        chip8.gfx.forEach { assertEquals(0.toByte(), it) }
        chip8.stack.forEach { assertEquals(0.toShort(), it) }
        chip8.V.forEach { assertEquals(0.toByte(), it) }
        chip8.memory.slice(80..chip8.memory.size - 1).forEach { assertEquals(0.toByte(), it) }
        chip8.keys.forEach { assertEquals(false, it) }

        // verify load of fonts
        for (i in 0..80-1) {
            assertEquals(chip8_fontset[i], chip8.memory[i].toInt() and 0xff)
        }

        // verify timers
        assertEquals(0.toByte(), chip8.delay_timer)
        assertEquals(0.toByte(), chip8.sound_timer)
    }

    @Test
    fun testInitAfterRunning() {
        // Load game code
        val bytes = File("src/main/assets/c8games/VERS").readBytes()
        chip8.init()
        chip8.loadGame(bytes)
        for (i in 0..2000) {
            chip8.opcode = chip8.nextOpcode()
            chip8.decodeAndExecuteOpcode()
        }
        chip8.setKey(0, true)

        // re-run init test
        testInit()
    }

    @Test
    fun testLoadGame() {
        val game = byteArrayOf(0xd, 0xe, 0xa, 0xd)
        chip8.loadGame(game)
        game.forEachIndexed { i, byte -> assertEquals(byte, chip8.memory[0x200 + i]) }
    }

    @Test
    fun testSetKeys() {
        val keys = booleanArrayOf(true, false, true)
        chip8.setKeys(keys)
        keys.forEachIndexed { i, b -> assertEquals(b, chip8.keys[i]) }
    }

    @Test
    fun testNextOpcode() {
        chip8.memory[0] = 0x6a
        chip8.memory[1] = 0x02
        chip8.memory[2] = 0xa2.toByte()
        chip8.memory[3] = 0xea.toByte()
        assertEquals(0x6a02.toShort(), chip8.nextOpcode())
        chip8.pc += 2
        assertEquals(0xa2ea.toShort(), chip8.nextOpcode())
    }

    @Test
    fun testOpcodeDisplayClear() {
        chip8.opcode = 0x00e0
        chip8.decodeAndExecuteOpcode()
        assertTrue(chip8.gfx.all { it == 0.toByte() })
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeReturn() {
        chip8.sp = 1
        val pc = 0xdef
        chip8.stack[0] = pc.toShort()
        chip8.opcode = 0x00ee
        chip8.decodeAndExecuteOpcode()
        assertEquals(pc + 2, chip8.pc)
        assertEquals(0, chip8.sp)
    }

    @Test
    fun testOpcodeGoto() {
        val NNN = 0xfea
        chip8.opcode = (0x1000 + NNN).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(NNN, chip8.pc)
    }

    @Test
    fun testOpcodeCallSubroutine() {
        val NNN = 0xead
        val pc = chip8.pc
        chip8.opcode = (0x2000 + NNN).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(pc.toShort(), chip8.stack[0])
        assertEquals(NNN, chip8.pc)
        assertEquals(1, chip8.sp)
    }

    @Test
    fun testOpcodeSkipNextIfVXEqualsNN() {
        val X = 1
        val NN = 0xdf
        chip8.opcode = (0x3000 + X.shl(8) + NN).toShort()

        // Test success
        chip8.V[X] = NN.toByte()
        chip8.decodeAndExecuteOpcode()
        assertEquals(4, chip8.pc)

        // test failure
        chip8.pc = 0
        chip8.V[X] = 0
        chip8.decodeAndExecuteOpcode()
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSkipNextIfVXNotEqualsNN() {
        val X = 1
        val NN = 0xdf
        chip8.opcode = (0x4000 + X.shl(8) + NN).toShort()

        // Test success
        chip8.V[X] = NN.toByte()
        chip8.decodeAndExecuteOpcode()
        assertEquals(2, chip8.pc)

        // test failure
        chip8.pc = 0
        chip8.V[X] = 0
        chip8.decodeAndExecuteOpcode()
        assertEquals(4, chip8.pc)
    }

    @Test
    fun testOpcodeSkipNextIfVXEqualVY() {
        val X = 1
        val Y = 3
        chip8.opcode = (0x5000 + X.shl(8) + Y.shl(4)).toShort()

        // Test success
        chip8.V[X] = 0x4
        chip8.V[Y] = 0x4
        chip8.decodeAndExecuteOpcode()
        assertEquals(4, chip8.pc)

        // test failure
        chip8.pc = 0
        chip8.V[Y] = 0
        chip8.decodeAndExecuteOpcode()
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSetVXtoNN() {
        val NN = 0xad
        val X = 1
        chip8.opcode = (0x6000 + X.shl(8) + NN).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(NN.toByte(), chip8.V[X])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeAddNNToVX() {
        val nn = 0xad
        val x = 1
        chip8.V[x] = 0x01
        chip8.opcode = (0x7000 + x.shl(8) + nn).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals((nn + 0x01).toByte(), chip8.V[x])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSetVXtoVY() {
        val x = 0x01
        val y = 0x02
        chip8.V[y] = 0x03
        chip8.opcode = (0x8000 + x.shl(8) + y.shl(4)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x03.toByte(), chip8.V[x])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSetVXtoVXorVY() {
        val x = 0x01
        val y = 0x02
        chip8.V[x] = 0x04
        chip8.V[y] = 0x03
        chip8.opcode = (0x8001 + x.shl(8) + y.shl(4)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x07.toByte(), chip8.V[x])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSetVXtoVXAndVY() {
        val x = 0x01
        val y = 0x02
        chip8.V[x] = 0x05
        chip8.V[y] = 0x03
        chip8.opcode = (0x8002 + x.shl(8) + y.shl(4)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x01.toByte(), chip8.V[x])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSetVXToVXXorVY() {
        val x = 0x01
        val y = 0x02
        chip8.V[x] = 0x05
        chip8.V[y] = 0x03
        chip8.opcode = (0x8003 + x.shl(8) + y.shl(4)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x06.toByte(), chip8.V[x])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeAddVXToVYWithoutCarry() {
        val x = 0x01
        val y = 0x02
        chip8.V[x] = 0x05
        chip8.V[y] = 0x03
        chip8.opcode = (0x8004 + x.shl(8) + y.shl(4)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x08.toByte(), chip8.V[x])
        assertEquals(0.toByte(), chip8.V[0xf])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeAddVXToVYWithCarry() {
        val x = 0x01
        val y = 0x02
        chip8.V[x] = 0xff.toByte()
        chip8.V[y] = 0x01
        chip8.opcode = (0x8004 + x.shl(8) + y.shl(4)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0.toByte(), chip8.V[x])
        assertEquals(1.toByte(), chip8.V[0xf])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSubtractVYFromVXWithoutBorrow() {
        val x = 0x01
        val y = 0x02
        chip8.V[x] = 0x02
        chip8.V[y] = 0x01
        chip8.opcode = (0x8005 + x.shl(8) + y.shl(4)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(1.toByte(), chip8.V[x])
        assertEquals(0.toByte(), chip8.V[0xf])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSubtractVYFromVXWithBorrow() {
        val x = 0x01
        val y = 0x02
        chip8.V[x] = 0x01
        chip8.V[y] = 0x02
        chip8.opcode = (0x8005 + x.shl(8) + y.shl(4)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0xff.toByte(), chip8.V[x])
        assertEquals(1.toByte(), chip8.V[0xf])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeShiftVXRightByOne() {
        val x = 0x01
        chip8.V[x] = 0x05
        chip8.opcode = (0x8006 + x.shl(8)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x2.toByte(), chip8.V[x])
        assertEquals(1.toByte(), chip8.V[0xf])
        assertEquals(2, chip8.pc)

        chip8.V[x] = 0x04
        chip8.opcode = (0x8006 + x.shl(8)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x2.toByte(), chip8.V[x])
        assertEquals(0.toByte(), chip8.V[0xf])
        assertEquals(4, chip8.pc)
    }

    @Test
    fun testOpcodeSetVXToVYMinusVXWithoutBorrow() {
        val x = 0x01
        val y = 0x02
        chip8.V[x] = 0x01
        chip8.V[y] = 0x02
        chip8.opcode = (0x8007 + x.shl(8) + y.shl(4)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x1.toByte(), chip8.V[x])
        assertEquals(0.toByte(), chip8.V[0xf])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSetVXToVYMinusVXWithBorrow() {
        val x = 0x01
        val y = 0x02
        chip8.V[x] = 0x02
        chip8.V[y] = 0x01
        chip8.opcode = (0x8007 + x.shl(8) + y.shl(4)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0xff.toByte(), chip8.V[x])
        assertEquals(1.toByte(), chip8.V[0xf])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeShiftVXLeftByOne() {
        val x = 0x01
        chip8.V[x] = 0x84.toByte()
        chip8.opcode = (0x800e + x.shl(8)).toShort()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x08.toByte(), chip8.V[x])
        assertEquals(1.toByte(), chip8.V[0xf])
        assertEquals(2, chip8.pc)

        chip8.V[x] = 0x04.toByte()
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x08.toByte(), chip8.V[x])
        assertEquals(0.toByte(), chip8.V[0xf])
        assertEquals(4, chip8.pc)
    }

    @Test
    fun testOpcodeSkipIfVXDoesNotEqualVY() {
        val x = 0x01
        val y = 0x02
        chip8.opcode = (0x9000 + x.shl(8) + y.shl(4)).toShort()

        // Test failure
        chip8.V[x] = 0x02
        chip8.V[y] = 0x01
        chip8.decodeAndExecuteOpcode()
        assertEquals(4, chip8.pc)

        // Test success
        chip8.pc = 0
        chip8.V[x] = 0x01
        chip8.V[y] = 0x01
        chip8.decodeAndExecuteOpcode()
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSetAToNNN() {
        val nnn = 0x102
        chip8.opcode = ((0xa000 + nnn).toShort())
        chip8.decodeAndExecuteOpcode()
        assertEquals(nnn, chip8.I)
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeJumpToNNNPlusV0() {
        val nnn = 0x102
        chip8.V[0] = 0xf
        chip8.opcode = ((0xb000 + nnn).toShort())
        chip8.decodeAndExecuteOpcode()
        assertEquals(nnn + chip8.V[0], chip8.pc)
    }

    @Test
    fun testOpcodeSetVXToBitwiseRandAndNN() {
        val nn = 0x12
        val x = 0x01
        chip8.opcode = ((0xc000 + x.shl(8) + nn).toShort())
        chip8.decodeAndExecuteOpcode()
        assertEquals(0, chip8.V[x].toInt() and nn.inv()) // can't have any bits that aren't in nn
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeDrawSpriteAtCoordinate() {
        val I = chip8.I
        // pixel has width 8 pixels, height n pixels
        val n = 0x4
        val x = 0x01
        val y = 0x02
        chip8.V[x] = 1
        chip8.V[y] = 1
        chip8.memory[I] = 0x8
        chip8.memory[I + 1] = 0x4
        chip8.memory[I + 2] = 0xf
        chip8.memory[I + 3] = 0x1
        val gfx_exp = 0x08040f01
        chip8.opcode = ((0xd000 + x.shl(8) + y.shl(4) + n).toShort())
        chip8.decodeAndExecuteOpcode()

        // VF no flip
        val gfx_copy = chip8.gfx.copyOf()
        val vx = chip8.V[x] // vx in range 0 - 64
        val vy = chip8.V[y] // vy in range 0 - 32
        var index = n * 8 - 1
        for (i in 0..n - 1) {
            for (b in 0..7) {
                val j = vx + b + 64 * (i + vy)
                val exp = gfx_exp shr(index--) and 0x1
                assertEquals(exp.toByte(), gfx_copy[j])
                gfx_copy[j] = 0 // reset so I can verify other indices are not set
            }
        }
        assertTrue(gfx_copy.all { it.toInt() == 0 }) // verify that we didn't white any other pixels
        assertEquals(I, chip8.I) // I doesn't change
        assertEquals(chip8.V[0xf], 0.toByte()) // no pixel flip
        assertEquals(2, chip8.pc)
        assertTrue(chip8.drawFlag)

        // VF flip
        chip8.memory[I] = 0x8
        chip8.memory[I + 1] = 0x3
        chip8.memory[I + 2] = 0x1
        chip8.memory[I + 3] = 0xf
        chip8.decodeAndExecuteOpcode()
        assertEquals(chip8.V[0xf], 1.toByte()) // pixel flip

        // VF unchanged from previous
        chip8.memory[I] = 0x0
        chip8.memory[I + 1] = 0x0
        chip8.memory[I + 2] = 0x0
        chip8.memory[I + 3] = 0x0
        chip8.decodeAndExecuteOpcode()
        assertEquals(chip8.V[0xf], 0.toByte()) // no pixel flip
    }

    @Test
    fun testOpcodeSkipIfKeyInVXPressed() {
        val x = 0x01
        chip8.opcode = ((0xe09e + x.shl(8)).toShort())
        val key = 0x1
        chip8.V[x] = key.toByte()

        chip8.keys[key] = false
        chip8.decodeAndExecuteOpcode()
        assertEquals(2, chip8.pc)

        chip8.pc = 0
        chip8.keys[key] = true
        chip8.decodeAndExecuteOpcode()
        assertEquals(4, chip8.pc)
    }

    @Test
    fun testOpcodeSkipIfKeyInVXNotPressed() {
        val x = 0x01
        chip8.opcode = ((0xe0a1 + x.shl(8)).toShort())
        val key = 0x1
        chip8.V[x] = key.toByte()

        chip8.keys[key] = false
        chip8.decodeAndExecuteOpcode()
        assertEquals(4, chip8.pc)

        chip8.pc = 0
        chip8.keys[key] = true
        chip8.decodeAndExecuteOpcode()
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSetVXToDelayTimer() {
        chip8.delay_timer = 0x7
        val x = 0x01
        chip8.opcode = ((0xf007 + x.shl(8)).toShort())
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x7.toByte(), chip8.V[x])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeAwaitKeyPressThenStoreInVX() {
        // Should be a blocking operation, all instructions halted
        val x = 0x01
        chip8.opcode = ((0xf00a + x.shl(8)).toShort())
        chip8.decodeAndExecuteOpcode()
        assertEquals(0, chip8.pc)

        chip8.decodeAndExecuteOpcode()
        assertEquals(0, chip8.pc)

        chip8.keys[0] = true
        chip8.decodeAndExecuteOpcode()
        assertEquals(0.toByte(), chip8.V[x])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSetDelayTimerToVX() {
        val x = 0x01
        chip8.V[x] = 0x7
        chip8.opcode = ((0xf015 + x.shl(8)).toShort())
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x7.toByte(), chip8.delay_timer)
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSetSoundTimerToVX() {
        val x = 0x01
        chip8.V[x] = 0x7
        chip8.opcode = ((0xf018 + x.shl(8)).toShort())
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x7.toByte(), chip8.sound_timer)
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeAddVXtoI() {
        val x = 0x01
        chip8.V[x] = 0x7
        chip8.I = 0x1
        chip8.opcode = ((0xf01e + x.shl(8)).toShort())
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x8, chip8.I)
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeSetIToLocationOfSprite() {
        val x = 0x01
        chip8.opcode = ((0xf029 + x.shl(8)).toShort())

        var pc = 0
        for (i in 0..0xf) {
            pc += 2
            chip8.V[x] = i.toByte()
            chip8.decodeAndExecuteOpcode()
            assertEquals(i * 5, chip8.I)
            assertEquals(pc, chip8.pc)
        }
    }

    @Test
    fun testOpcodeStoreBinaryCodedVX() {
        val x = 0x01
        chip8.V[x] = 0x95.toByte() // 149
        chip8.opcode = ((0xf033 + x.shl(8)).toShort())
        chip8.decodeAndExecuteOpcode()
        assertEquals(1.toByte(), chip8.memory[chip8.I])
        assertEquals(4.toByte(), chip8.memory[chip8.I + 1])
        assertEquals(9.toByte(), chip8.memory[chip8.I + 2])
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeStoreVInMemory() {
        val x = 0x01
        chip8.V[0] = 0x1
        chip8.V[x] = 0xf
        chip8.opcode = ((0xf055 + x.shl(8)).toShort())
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x1.toByte(), chip8.memory[chip8.I])
        assertEquals(0xf.toByte(), chip8.memory[chip8.I + 1])
        for (i in 2..0xe) {
            assertEquals(0.toByte(), chip8.memory[chip8.I + i])
        }
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testOpcodeFillVFromMemory() {
        val x = 0x01
        chip8.I = 0xf
        chip8.memory[chip8.I] = 0x1
        chip8.memory[chip8.I + 1] = 0xf
        chip8.opcode = ((0xf065 + x.shl(8)).toShort())
        chip8.decodeAndExecuteOpcode()
        assertEquals(0x1.toByte(), chip8.V[0])
        assertEquals(0xf.toByte(), chip8.V[1])
        for (i in 2..0xe) {
            assertEquals(0.toByte(), chip8.V[i])
        }
        assertEquals(2, chip8.pc)
    }

    @Test
    fun testBugInDrawingCast() {
        // 6300 6700 00e0 a217 6000 6100 d011 71ff
        val bytes = File("src/main/assets/c8games/VERS").readBytes()
        chip8.init()
        chip8.loadGame(bytes)
        for (i in 0..200) {
            chip8.opcode = chip8.nextOpcode()
            chip8.decodeAndExecuteOpcode()
        }
    }

    @Test
    fun test15Puzzle() {
        val bytes = File("src/main/assets/c8games/15PUZZLE").readBytes()
        chip8.init()
        chip8.loadGame(bytes)
        for (i in 0..500) {
            chip8.opcode = chip8.nextOpcode()
            println("op(${chip8.pc.toShort().toHexString()}) ${chip8.opcode.toHexString()} I=${chip8.I.toHexString()} V=${chip8.V.map { it.toHexString() }} stack=${chip8.stack.map { it.toHexString() }}")
            chip8.decodeAndExecuteOpcode()
        }
    }
}