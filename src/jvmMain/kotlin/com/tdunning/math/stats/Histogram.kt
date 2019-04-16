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
import java.io.Serializable

/**
 * A Histogram is a histogram with cleverly chosen, but fixed, bin widths.
 *
 * Different implementations may provide better or worse speed or space complexity,
 * but each is attuned to a particular distribution or error metric.
 */
abstract class Histogram(protected var min: Double, protected var max: Double) : Serializable {
    var counts: LongArray= LongArray(0)
        protected set
    protected var logFactor: Double = 0.toDouble()
    protected var logOffset: Double = 0.toDouble()

    fun getBounds(): DoubleArray {
            val r = DoubleArray(counts.size)
            for (i in r.indices) {
                r[i] = lowerBound(i)
            }
            return r
        }

    internal abstract fun getCompressedCounts(): LongArray

    protected fun setupBins(min: Double, max: Double) {
        val binCount = bucketIndex(max) + 1
        if (binCount > 10000) {
            throw IllegalArgumentException(
                String.format(
                    "Excessive number of bins %d resulting from min,max = %.2g, %.2g",
                    binCount, min, max
                )
            )

        }
        counts = LongArray(binCount)
    }

    fun add(v: Double) {
        counts[bucket(v)]++
    }

    // exposed for testing
    internal fun bucket(x: Double): Int {
        return if (x <= min) {
            0
        } else if (x >= max) {
            counts.size - 1
        } else {
            bucketIndex(x)
        }
    }

    protected abstract fun bucketIndex(x: Double): Int

    // exposed for testing
    internal abstract fun lowerBound(k: Int): Double

    @Throws(IOException::class)
    internal abstract fun writeObject(out: java.io.ObjectOutputStream)

    @Throws(IOException::class)
    internal abstract fun readObject(`in`: java.io.ObjectInputStream)

    internal abstract fun add(others: Iterable<Histogram>)
}
