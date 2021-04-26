package com.basicio

import kotlin.js.JsExport

/**
 * interface inspired by kotlinx-io Input interface
 */
@JsExport
interface BinaryInput {
    fun readByte(): Byte
    fun readShort(): Short
    fun readInt(): Int
    fun readLong(): kotlin.Long
    fun readFloat(): Float
    fun readDouble(): Double
    fun readBoolean():Boolean {
        return if(readByte()==0.toByte()) false else true
    }
    /**
     * release the data buffer
     */
    fun release()
}

/**
 * create a binary input from a input base64 encoded string
 */
expect fun buildBinaryInputFromB64(b64string:String): BinaryInput