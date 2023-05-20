package com.subreax.lightclient

import kotlin.math.roundToInt

fun round2(float: Float) = (float * 100f).roundToInt() / 100f
fun round3(float: Float) = (float * 1000f).roundToInt() / 1000f
fun round4(float: Float) = (float * 10000f).roundToInt() / 10000f
