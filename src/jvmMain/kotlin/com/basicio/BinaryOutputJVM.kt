package com.basicio

import java.nio.ByteBuffer
import java.util.*

class BinaryOutputFromByteBuffer(val bb: ByteBuffer) : BinaryOutput {
    override val size: Int
        get() = bb.position()

    override fun writeByte(v: Byte) {
        bb.put(v)
    }

    override fun writeShort(v: Short) {
        bb.putShort(v)
    }

    override fun writeInt(v: Int) {
        bb.putInt(v)
    }

    override fun writeLong(v: Long) {
        bb.putLong(v)
    }

    override fun writeFloat(v: Float) {
        bb.putFloat(v)
    }

    override fun writeDouble(v: Double) {
        bb.putDouble(v)
    }

    override fun toB64(): String {
        return Base64.getEncoder().encodeToString(bb.array())
    }
}

fun ByteBuffer.toBinaryOutput() = BinaryOutputFromByteBuffer(this)
/**
 * create a binary output that can be written to
 */
actual fun buildBinaryOutput(initialSize: Int, block: BinaryOutput.() -> Unit): BinaryInput {
    val buf = ByteBuffer.allocate(initialSize)
    block(buf.toBinaryOutput())
    return ByteBuffer.wrap(buf.array()).toBinaryInput()
}