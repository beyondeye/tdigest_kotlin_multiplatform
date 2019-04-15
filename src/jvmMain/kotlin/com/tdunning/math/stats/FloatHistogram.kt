/*
 * Licensed to Ted Dunning under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tdunning.math.stats

import java.io.IOException
import java.io.InvalidObjectException
import java.io.ObjectStreamException
import java.io.Serializable
import java.nio.ByteBuffer
import java.nio.LongBuffer

/**
 * Maintains histogram buckets that are constant width
 * in base-2 floating point representation space. This is close
 * to exponential binning, but should be much faster.
 */
class FloatHistogram @JvmOverloads constructor(min: Double, max: Double, binsPerDecade: Double = 50.0) :
    Histogram(min, max) {
    private var bitsOfPrecision: Int = 0
    private var shift: Int = 0
    private var offset: Int = 0

    init {
        if (max <= 2 * min) {
            throw IllegalArgumentException(String.format("Illegal/nonsensical min, max (%.2f, %.2g)", min, max))
        }
        if (min <= 0 || max <= 0) {
            throw IllegalArgumentException("Min and max must be positive")
        }
        if (binsPerDecade < 5 || binsPerDecade > 10000) {
            throw IllegalArgumentException(
                String.format(
                    "Unreasonable number of bins per decade %.2g. Expected value in range [5,10000]",
                    binsPerDecade
                )
            )
        }

        // convert binsPerDecade into bins per octave, then figure out how many bits that takes
        bitsOfPrecision = Math.ceil(Math.log(binsPerDecade * Math.log10(2.0)) / Math.log(2.0)).toInt()
        // we keep just the required amount of the mantissa
        shift = 52 - bitsOfPrecision
        // The exponent in a floating point number is offset
        offset = 0x3ff shl bitsOfPrecision

        setupBins(min, max)
    }

    override fun bucketIndex(x: Double): Int {
        var x = x
        x = x / min
        val floatBits = java.lang.Double.doubleToLongBits(x)
        return floatBits.ushr(shift).toInt() - offset
    }

    // exposed for testing
    internal override fun lowerBound(k: Int): Double {
        return min * java.lang.Double.longBitsToDouble(k + (0x3ffL shl bitsOfPrecision) shl 52 - bitsOfPrecision) /* / fuzz */
    }

    public override fun getCompressedCounts(): LongArray {
        val buf = LongBuffer.allocate(counts.size)
        Simple64.compress(buf, counts, 0, counts.size)
        val r = LongArray(buf.position())
        buf.flip()
        buf.get(r)
        return r
    }

    @Throws(IOException::class)
    public override fun writeObject(out: java.io.ObjectOutputStream) {
        out.writeDouble(min)
        out.writeDouble(max)
        out.writeByte(bitsOfPrecision)
        out.writeByte(shift)

        val buf = ByteBuffer.allocate(8 * counts.size)
        val longBuffer = buf.asLongBuffer()
        Simple64.compress(longBuffer, counts, 0, counts.size)
        buf.position(8 * longBuffer.position())
        val r = ByteArray(buf.position())
        out.writeShort(buf.position())
        buf.flip()
        buf.get(r)
        out.write(r)
    }

    @Throws(IOException::class)
    public override fun readObject(`in`: java.io.ObjectInputStream) {
        min = `in`.readDouble()
        max = `in`.readDouble()
        bitsOfPrecision = `in`.readByte().toInt()
        shift = `in`.readByte().toInt()
        offset = 0x3ff shl bitsOfPrecision

        val n = `in`.readShort().toInt()
        val buf = ByteBuffer.allocate(n)
        `in`.readFully(buf.array(), 0, n)
        val binCount = bucketIndex(max) + 1
        if (binCount > 10000) {
            throw IllegalArgumentException(
                String.format(
                    "Excessive number of bins %d during deserialization = %.2g, %.2g",
                    binCount, min, max
                )
            )

        }
        counts = LongArray(binCount)
        Simple64.decompress(buf.asLongBuffer(), counts)
    }

    @Throws(ObjectStreamException::class)
    private fun readObjectNoData() {
        throw InvalidObjectException("Stream data required")
    }

    override fun add(others: Iterable<Histogram>) {
        for (other in others) {
            if (this.javaClass != other.javaClass) {
                throw IllegalArgumentException(String.format("Cannot add %s to FloatHistogram", others.javaClass))
            }
            val actual = other as FloatHistogram
            if (actual.min != min || actual.max != max || actual.counts.size != counts.size) {
                throw IllegalArgumentException("Can only merge histograms with identical bounds and precision")
            }
            for (i in counts.indices) {
                counts[i] += other.counts[i]
            }
        }
    }

}
