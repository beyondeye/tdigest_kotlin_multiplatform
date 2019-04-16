package com.tdunning.math.stats

object Utils {
    fun arraycopy(src: DoubleArray, srcPos: Int, dest: DoubleArray, destPos: Int, length: Int) =
        src.copyInto(dest, destPos, srcPos, srcPos + length)

    fun arraycopy(src: IntArray, srcPos: Int, dest: IntArray, destPos: Int, length: Int) =
        src.copyInto(dest, destPos, srcPos, srcPos + length)

    fun arraycopy(src: LongArray, srcPos: Int, dest: LongArray, destPos: Int, length: Int) =
        src.copyInto(dest, destPos, srcPos, srcPos + length)

}
fun mpassert(expectedTrue:Boolean) {
    mpassert(expectedTrue,{"assertion failed"})
}
//*DARIO* : since not all platform support assert, define a wrapper method
expect fun mpassert(value: Boolean, lazyMessage: () -> Any)
