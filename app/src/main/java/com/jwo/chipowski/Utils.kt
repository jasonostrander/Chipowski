package com.jwo.chipowski

/**
 * Created by j.ostrander on 1/3/17.
 */

fun toHex(number: Number): String = "%04X".format(number.toInt() and 0xffff)

fun printHex(number: Number) = println(toHex(number))
