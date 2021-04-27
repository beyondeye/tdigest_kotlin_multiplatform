package com.basicio

import kotlin.js.JsName

/**
 * interface inspired by kotlinx-io Input interface
 */
interface BinaryInput {
    @JsName("readByte")
    fun readByte(): Byte
    @JsName("readShort")
    fun readShort(): Short
    @JsName("readInt")
    fun readInt(): Int
    @JsName("readLong")
    fun readLong(): kotlin.Long
    @JsName("readFloat")
    fun readFloat(): Float
    @JsName("readDouble")
    fun readDouble(): Double
    @JsName("readBoolean")
    fun readBoolean():Boolean {
        return if(readByte()==0.toByte()) false else true
    }
    /**
     * release the data buffer
     */
    @JsName("release")
    fun release()
}

/**
 * create a binary input from a input base64 encoded string
 */
expect fun buildBinaryInputFromB64(b64string:String): BinaryInput