package com.tdunning.math.stats

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket

class BinaryOutputFromBytePacketBuilder(val bpb:BytePacketBuilder) :BinaryOutput{
    override fun writeByte(v: Byte) {
        bpb.writeByte(v)
    }

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

class BinaryInputFromByteReadPacket(val brp:ByteReadPacket):BinaryInput {
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

    override fun fromB64String(b64string: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
fun BytePacketBuilder.toBinaryOutput():BinaryOutput = BinaryOutputFromBytePacketBuilder(this)
fun ByteReadPacket.toBinaryInput():BinaryInput = BinaryInputFromByteReadPacket(this)