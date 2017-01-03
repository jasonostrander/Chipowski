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
        assertEquals(0.toShort(), chip8.I)
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
        val NN = 0xdf
        chip8.opcode = (0x3000 + NN).toShort()
        chip8.decodeAndExecuteOpcode()
    }
}