package com.basicio

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket

class BinaryOutputFromBytePacketBuilder(val bpb:BytePacketBuilder) : BinaryOutput {
    override fun writeByte(v: Byte) {
        bpb.writeByte(v)
    }

    override val size: Int
        get() = bpb.size

    override fun writeShort(v: Short) {
        bpb.writeShort(v)
    }

    override fun writeInt(v: Int) {
        bpb.writeInt(v)
    }

    override fun writeLong(v: Long) {
        bpb.writeLong(v)
    }

    override fun writeFloat(v: Float) {
        bpb.writeFloat(v)
    }

    override fun writeDouble(v: Double) {
        bpb.writeDouble(v)
    }

    override fun toB64(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class BinaryInputFromByteReadPacket(val brp:ByteReadPacket): BinaryInput {
    override fun release() {
        brp.release()
    }

    override fun readByte(): Byte {
        return brp.readByte()
    }

    override fun readShort(): Short {
        return brp.readShort()
    }

    override fun readInt(): Int {
        return brp.readInt()
    }

    override fun readLong(): Long {
        return brp.readLong()
    }

    override fun readFloat(): Float {
        return brp.readFloat()
    }

    override fun readDouble(): Double {
        return brp.readDouble()
    }
}
fun BytePacketBuilder.toBinaryOutput(): BinaryOutput =
    BinaryOutputFromBytePacketBuilder(this)
fun ByteReadPacket.toBinaryInput(): BinaryInput =
    BinaryInputFromByteReadPacket(this)

//NOTE: that initialSize parameter is currently ignored
inline fun buildBinaryOutput(initialSize:Int,block: BinaryOutput.() -> Unit): BinaryInput {
    return buildPacket {
        block(this.toBinaryOutput())
    }.toBinaryInput()
}