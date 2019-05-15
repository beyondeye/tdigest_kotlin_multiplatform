package com.basicio
//see https://github.com/dcodeIO/long.js#methods
@JsModule("long")
external class Long {
    fun getHighBits(): Int
    fun getLowBits(): Int
    companion object {
        fun fromBits(lowBits: Int, highBits: Int, unsigned: Boolean = definedExternally): Long
    }
}

//see https://github.com/protobufjs/bytebuffer.js/wiki/API
@JsModule("bytebuffer")
external class ByteBuffer(initialCapacity:Int) {
    fun readByte():Byte
    fun readShort():Short
    fun readInt():Int
    fun readLong():Long
    fun readFloat():Float
    fun readDouble():Double
    fun writeByte(v:Byte)
    fun writeShort(v:Short)
    fun writeInt(v:Int)
    fun writeLong(v:Long) /* number | string | longjs*/
    fun writeFloat(v:Float)
    fun writeDouble(v:Double)
    fun toBase64():String
    fun flip(): ByteBuffer
    var offset:Int
    companion object {
        fun fromBase64(str:String): ByteBuffer
    }
}

