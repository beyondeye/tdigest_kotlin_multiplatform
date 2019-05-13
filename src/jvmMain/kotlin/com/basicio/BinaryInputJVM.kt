package com.basicio

import sun.nio.ch.DirectBuffer
import java.nio.ByteBuffer
import java.util.*

class BinaryInputFromByteBuffer(val bb: ByteBuffer) : BinaryInput {
    override fun readByte(): Byte {
        return bb.get()
    }

    override fun readShort(): Short {
        return bb.getShort()
    }

    override fun readInt(): Int {
        return bb.getInt()
    }

    override fun readLong(): Long {
        return bb.getLong()
    }

    override fun readFloat(): Float {
        return bb.getFloat()
    }

    override fun readDouble(): Double {
        return bb.getDouble()
    }

    override fun release() {
        //see https://stackoverflow.com/questions/8462200/examples-of-forcing-freeing-of-native-memory-direct-bytebuffer-has-allocated-us
        val cleaner = (bb as? DirectBuffer)?.cleaner()
        cleaner?.clean()
    }
}

fun ByteBuffer.toBinaryInput() = BinaryInputFromByteBuffer(this)

/**
 * create a binary input from a input base64 encoded string
 */
actual fun buildBinaryInputFromB64(b64string: String): BinaryInput {
    val decoded = Base64.getDecoder().decode(b64string)
    return ByteBuffer.wrap(decoded).toBinaryInput()
}