package com.jwo.chipowski

import android.util.Log

/**
 * Created by j.ostrander on 1/3/17.
 */

fun toHex(number: Number): String = "%04X".format(number)

fun printHex(number: Number) = println(toHex(number))

fun logcat(s: String, e: Exception? = null) = Log.d("jason", s, e)