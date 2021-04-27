package com.basicio

import kotlin.js.JsName

/**
 * interface inspired by kotlinx-io Output interface
 */
interface BinaryOutput {
    @JsName("size")
    val size: Int
    @JsName("writeByte")
    fun writeByte(v: Byte)
    @JsName("writeShort")
    fun writeShort(v: Short)
    @JsName("writeInt")
    fun writeInt(v: Int)
    @JsName("writeLong")
    fun writeLong(v: kotlin.Long)
    @JsName("writeFloat")
    fun writeFloat(v: Float)
    @JsName("writeDouble")
    fun writeDouble(v: Double)
    @JsName("toB64")
    fun toB64():String
    @JsName("writeBoolean")
    fun writeBoolean(v:Boolean) {
        writeByte(if(v) 1 else 0)
    }
}

/**
 * create a binary output that can be written to
 */
expect fun buildBinaryOutput(initialSize:Int,block: BinaryOutput.() -> Unit): BinaryInput