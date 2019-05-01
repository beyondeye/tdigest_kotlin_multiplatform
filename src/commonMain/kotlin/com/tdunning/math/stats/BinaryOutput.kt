package com.tdunning.math.stats

/**
 * interface inspired by kotlinx-io Output interface
 */
interface BinaryOutput {
    fun writeByte(v: Byte)
    fun writeShort(v: Short)
    fun writeInt(v: Int)
    fun writeLong(v: Long)
    fun writeFloat(v: Float)
    fun writeDouble(v: Double)
    fun toB64():String
}