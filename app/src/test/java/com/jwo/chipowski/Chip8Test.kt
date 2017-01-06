package com.jwo.chipowski

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
        // TODO: actually run some instructions, then verify reset
        chip8.init()

        assertEquals(0x200, chip8.pc)
        assertEquals(0.toShort(), chip8.opcode)
        assertEquals(0, chip8.I)
        assertEquals(0, chip8.sp)

        // TODO: verify reset of display, stack, registers, memory

        // TODO: verify load of fonts

        // TODO: verify reset timers
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
        // Set initial stack value
        val sp = chip8.sp
        val pc = 0xdef
        chip8.stack[chip8.sp] = pc
        chip8.opcode = 0x00ee
        chip8.decodeAndExecuteOpcode()
        assertEquals(pc, chip8.pc)
        assertEquals(sp - 1, chip8.sp)
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
        assertEquals(pc, chip8.stack[0])
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

    // TODO: implement graphics
//    @Test
//    fun testOpcodeDrawSpriteAtCoordinate() {
//        val I = chip8.I
//        // pixel has width 8 pixels, height n pixels
//        val n = 0x4
//        val x = 0x01
//        val y = 0x02
//        chip8.opcode = ((0xd000 + x.shl(8) + y.shl(4) + n).toShort())
//        chip8.decodeAndExecuteOpcode()
//
//        val vx = chip8.V[x] // vx in range 0 - 64
//        val vy = chip8.V[y] // vy in range 0 - 32
//        for (i in 0..n) {
//            // vx = 0, vy = 1, 0 + 1 * 64 * 0 = 0
//            assertEquals(chip8.memory[I + i], chip8.gfx[vx + vy + 64*n])
//        }
//        assertEquals(I, chip8.I) // I doesn't change
//        assertEquals(2, chip8.pc)
//
//        // VF flip
//
//        // VF doesn't flip
//    }

    @Test
    fun testOpcodeSkipIfKeyInVXPressed() {
        val x = 0x01
        chip8.opcode = ((0xe09e + x.shl(8)).toShort())
        val key = 0x1
        chip8.V[x] = key.toByte()

        chip8.key[key] = false
        chip8.decodeAndExecuteOpcode()
        assertEquals(2, chip8.pc)

        chip8.pc = 0
        chip8.key[key] = true
        chip8.decodeAndExecuteOpcode()
        assertEquals(4, chip8.pc)
    }

    @Test
    fun testOpcodeSkipIfKeyInVXNotPressed() {
        val x = 0x01
        chip8.opcode = ((0xe0a1 + x.shl(8)).toShort())
        val key = 0x1
        chip8.V[x] = key.toByte()

        chip8.key[key] = false
        chip8.decodeAndExecuteOpcode()
        assertEquals(4, chip8.pc)

        chip8.pc = 0
        chip8.key[key] = true
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

        chip8.key[0] = true
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
}