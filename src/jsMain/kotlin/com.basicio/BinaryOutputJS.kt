package com.basicio


class BinaryOutputFromByteBuffer(val bb: ByteBuffer) : BinaryOutput {
    override val size: Int
        get() = bb.offset

    override fun writeByte(v: Byte) {
        bb.writeByte(v)
    }

    override fun writeShort(v: Short) {
        bb.writeShort(v)
    }

    override fun writeInt(v: Int) {
        bb.writeInt(v)
    }

    override fun writeLong(v: Long) {
        val v_ =v.asDynamic()
        bb.writeLongJs(LongJs.fromBits(v_.low_,v_.high_))
    }

    override fun writeFloat(v: Float) {
        bb.writeFloat(v)
    }

    override fun writeDouble(v: Double) {
        bb.writeDouble(v)
    }

    override fun toB64(): String {
        bb.flip()
        return bb.toBase64()
    }
}
@JsName("toBinaryOutput")
fun ByteBuffer.toBinaryOutput() = BinaryOutputFromByteBuffer(this)
/**
 * create a binary output that can be written to
 */
actual fun buildBinaryOutput(initialSize: Int, block: BinaryOutput.() -> Unit): BinaryInput {
    val buf = ByteBuffer(initialSize)
    block(buf.toBinaryOutput())
    return buf.flip().toBinaryInput()
}