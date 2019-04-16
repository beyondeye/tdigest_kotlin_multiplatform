package com.tdunning.math.stats

object Utils {
    fun arraycopy(src: DoubleArray, srcPos: Int, dest: DoubleArray, destPos: Int, length: Int) =
        src.copyInto(dest, destPos, srcPos, srcPos + length)

    fun arraycopy(src: IntArray, srcPos: Int, dest: IntArray, destPos: Int, length: Int) =
        src.copyInto(dest, destPos, srcPos, srcPos + length)
}