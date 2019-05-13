package com.tdunning.math.stats

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
        return bb.readLong()
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