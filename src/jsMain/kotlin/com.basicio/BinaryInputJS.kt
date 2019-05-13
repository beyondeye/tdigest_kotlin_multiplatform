package com.basicio

//see https://discuss.kotlinlang.org/t/approaches-to-convert-to-from-longs-in-kotlin-js/8075
fun LongFromBits(lowBits: Int, highBits: Int): Long
        = js("Kotlin").Long.fromBits(lowBits, highBits) as Long

class BinaryInputFromByteBuffer(val bb: ByteBuffer) : BinaryInput {
    override fun readByte(): Byte {
        return bb.readByte()
    }

    override fun readShort(): Short {
        return bb.readShort()
    }

    override fun readInt(): Int {
        return bb.readInt()
    }

    override fun readLong(): Long {
        //need to convert longjs representation to kotlin representation, so pass through string representation
        //we use here the toString(radix) method from https://github.com/dcodeIO/long.js#methods
        // (it is not kotlin.toString() method)
        val value:LongJs= bb.readLongJs()
        return LongFromBits(value.getLowBits(),value.getHighBits())
    }

    override fun readFloat(): Float {
        return bb.readFloat()
    }

    override fun readDouble(): Double {
        return bb.readDouble()
    }

    override fun release() {
        //do nothing
    }
}
@JsName("toBinaryInput")
fun ByteBuffer.toBinaryInput() = BinaryInputFromByteBuffer(this)

/**
 * create a binary input from a input base64 encoded string
 */
actual fun buildBinaryInputFromB64(b64string: String): BinaryInput {
    return ByteBuffer.fromBase64(b64string).toBinaryInput()
}