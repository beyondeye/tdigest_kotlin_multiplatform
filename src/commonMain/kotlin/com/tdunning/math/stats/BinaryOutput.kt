package com.tdunning.math.stats

/**
 * interface inspired by kotlinx-io Output interface
 */
interface BinaryOutput {
    val size: Int
    fun writeByte(v: Byte)
    fun writeShort(v: Short)
    fun writeInt(v: Int)
    fun writeLong(v: Long)
    fun writeFloat(v: Float)
    fun writeDouble(v: Double)
    fun toB64():String
}

/**
 * create a binary output that can be written to
 */
expect fun buildBinaryOutput(initialSize:Int,block: BinaryOutput.() -> Unit): BinaryInput