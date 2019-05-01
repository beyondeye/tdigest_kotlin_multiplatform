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
    fun fromB64String(b64string:String)
}