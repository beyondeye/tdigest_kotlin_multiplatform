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
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import java.lang.Math.sqrt

/**
 * Non-linear histogram that uses floating point representation plus a quadratic correction to
 * bin width to achieve tighter fit to the ideal log2 sizing.
 */
class LogHistogram @JvmOverloads constructor(min: Double, max: Double, epsilonFactor: Double = 0.1) :
    Histogram(min, max) {

    init {
        logFactor = Math.log(2.0) / Math.log(1 + epsilonFactor)
        logOffset = approxLog2(min) * logFactor

        if (max <= 2 * min) {
            throw IllegalArgumentException(String.format("Illegal/nonsensical min, max (%.2f, %.2g)", min, max))
        }
        if (min <= 0 || max <= 0) {
            throw IllegalArgumentException("Min and max must be positive")
        }
        if (epsilonFactor < 1e-6 || epsilonFactor > 0.5) {
            throw IllegalArgumentException(
                String.format(
                    "Unreasonable number of bins per decade %.2g. Expected value in range [1e-6,0.5]",
                    epsilonFactor
                )
            )
        }

        setupBins(min, max)
    }

    override fun bucketIndex(x: Double): Int {
        return (approxLog2(x) * logFactor - logOffset).toInt()
    }

    internal override fun lowerBound(k: Int): Double {
        return pow2((k + logOffset) / logFactor)
    }

    internal override fun getCompressedCounts(): LongArray {
        return LongArray(0)
    }

    @Throws(IOException::class)
    internal override fun writeObject(out: ObjectOutputStream) {

    }

    @Throws(IOException::class)
    internal override fun readObject(`in`: ObjectInputStream) {

    }

    companion object {

        /**
         * Approximates log_2(value) by abusing floating point hardware. The floating point exponent
         * is used to get the integer part of the log. The mantissa is then adjusted with a second order
         * polynomial to get a better approximation. The error is bounded to be less than ±0.01 and is
         * zero at every power of two (which also implies the approximation is continuous).
         *
         * @param value The argument of the log
         * @return log_2(value) (within an error of about ± 0.01)
         */
        fun approxLog2(value: Double): Double {
            val valueBits = java.lang.Double.doubleToRawLongBits(value)
            val exponent = (valueBits and 0x7ff0000000000000L).ushr(52) - 1024
            val m = java.lang.Double.longBitsToDouble(valueBits and -0x7ff0000000000001L or 0x3ff0000000000000L)
            return m * (2 - 1.0 / 3 * m) + exponent - 2.0 / 3.0
        }

        /**
         * Computes an approximate value of 2^x. This is done as an exact inverse of #approxLog2 so
         * that bin boundaries can be computed exactly.
         *
         * @param x The power of 2 desired.
         * @return 2^x approximately.
         */
        fun pow2(x: Double): Double {
            var x = x
            val exponent = Math.floor(x) - 1
            x = x - exponent
            val m = 3 - sqrt(7 - 3 * x)
            return Math.pow(2.0, exponent + 1) * m
        }
    }
}
