package com.basicio

import kotlin.js.JsExport

/**
 * interface inspired by kotlinx-io Output interface
 */
@JsExport
interface BinaryOutput {
    val size: Int
    fun writeByte(v: Byte)
    fun writeShort(v: Short)
    fun writeInt(v: Int)
    fun writeLong(v: kotlin.Long)
    fun writeFloat(v: Float)
    fun writeDouble(v: Double)
    fun toB64():String
    fun writeBoolean(v:Boolean) {
        writeByte(if(v) 1 else 0)
    }
}

/**
 * create a binary output that can be written to
 */
expect fun buildBinaryOutput(initialSize:Int,block: BinaryOutput.() -> Unit): BinaryInput