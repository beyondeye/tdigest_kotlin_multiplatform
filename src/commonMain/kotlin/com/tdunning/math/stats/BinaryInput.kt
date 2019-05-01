package com.tdunning.math.stats

/**
 * interface inspired by kotlinx-io Input interface
 */
interface BinaryInput {
    fun readByte(): Byte
    fun readShort(): Short
    fun readInt(): Int
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double
    /**
     * release the data buffer
     */
    fun release()
}

/**
 * create a binary input from a input base64 encoded string
 */
expect fun buildBinaryInputFromB64(b64string:String):BinaryInput