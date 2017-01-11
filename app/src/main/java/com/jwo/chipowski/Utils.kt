package com.jwo.chipowski

import android.util.Log

/**
 * Created by j.ostrander on 1/3/17.
 */

fun toHex(number: Number): String = "%04X".format(number)

fun Int.toHexString(): String = "%08X".format(this)
fun Short.toHexString(): String = "%04X".format(this)
fun Byte.toHexString(): String = "%02X".format(this)

fun printHex(number: Number) = println(toHex(number))

fun logcat(s: String, e: Exception? = null, tag: String = "jason") = Log.d(tag, s, e)

fun convertGraphicsToText(gfx: ByteArray): String {
    var s = ""
    for (i in 0..(64*31) step 64) {
        s += gfx.slice(i..i+63).fold("") { s, b -> s + b.toHexString() }
        s += "\n"
    }
    return s
}
