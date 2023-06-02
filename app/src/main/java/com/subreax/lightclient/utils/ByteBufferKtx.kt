package com.subreax.lightclient.utils

import java.nio.ByteBuffer

fun ByteBuffer.toPrettyString(start: Int, end: Int): String {
    val sb = StringBuilder()
    val arr = array()
    for (i in start until end) {
        val b = arr[i]
        val b1 = (b.toInt() + 256) and 0xff
        val byteStr = b1.toString(16)
        if (byteStr.length == 1) {
            sb.append('0')
        }
        sb.append(byteStr).append(" ")
    }
    return sb.toString()
}

fun ByteBuffer.getUtf8String(): String {
    mark()
    var len = 0
    while (true) {
        val b = get()
        if (b.toInt() == 0) {
            break
        }
        ++len
    }
    reset()

    val bytestr = ByteArray(len)
    get(bytestr)
    get()
    return bytestr.toString(Charsets.UTF_8)
}

fun ByteBuffer.getWrittenData(): ByteArray {
    val pos = position()
    val written = ByteArray(pos)
    position(0)
    get(written)
    position(pos)
    return written
}