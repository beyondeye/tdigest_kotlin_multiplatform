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

import kotlinx.io.core.Input
import kotlinx.io.core.Output
import kotlin.random.Random

abstract class AbstractTDigest : TDigest() {
    //*PORT* multiplatform random is not serializable: define a wrapper class that is serializable at least on the jvm
//    internal val gen = Random.Default
    internal val gen = Random(0)
    override var isRecording = false
        internal set

    internal abstract fun add(x: Double, w: Int, base: Centroid)

    /**
     * Sets up so that all centroids will record all data assigned to them.  For testing only, really.
     */
    override fun recordAllData(): TDigest {
        isRecording = true
        return this
    }

    /**
     * Adds a sample to a histogram.
     *
     * @param x The value to add.
     */
    override fun add(x: Double) {
        add(x, 1)
    }

    override fun add(other: TDigest) {
        val tmp = mutableListOf<Centroid>()
        for (centroid in other.centroids()) {
            tmp.add(centroid)
        }

//        Collections.shuffle(tmp, gen)
        tmp.shuffle(gen)
        for (centroid in tmp) {
            add(centroid.mean(), centroid.count(), centroid)
        }
    }

    protected fun createCentroid(mean: Double, id: Int): Centroid {
        return Centroid(mean, id, isRecording)
    }

    companion object {

        /**
         * Same as [.weightedAverageSorted] but flips
         * the order of the variables if `x2` is greater than
         * `x1`.
         */
        internal fun weightedAverage(x1: Double, w1: Double, x2: Double, w2: Double): Double {
            return if (x1 <= x2) {
                weightedAverageSorted(x1, w1, x2, w2)
            } else {
                weightedAverageSorted(x2, w2, x1, w1)
            }
        }

        /**
         * Compute the weighted average between `x1` with a weight of
         * `w1` and `x2` with a weight of `w2`.
         * This expects `x1` to be less than or equal to `x2`
         * and is guaranteed to return a number between `x1` and
         * `x2`.
         */
        private fun weightedAverageSorted(x1: Double, w1: Double, x2: Double, w2: Double): Double {
            mpassert(x1 <= x2)
            val x = (x1 * w1 + x2 * w2) / (w1 + w2)
            return kotlin.math.max(x1, kotlin.math.min(x, x2))
        }

        internal fun interpolate(x: Double, x0: Double, x1: Double): Double {
            return (x - x0) / (x1 - x0)
        }
        internal fun encode(buf: Output, n: Int) {
            var n = n
            var k = 0
            while (n < 0 || n > 0x7f) {
                val b = (0x80 or (0x7f and n)).toByte()
                buf.writeByte(b)
                n = n.ushr(7)
                k++
                if (k >= 6) {
                    throw IllegalStateException("Size is implausibly large")
                }
            }
            buf.writeByte(n.toByte())
        }

        internal fun decode(buf: Input): Int {
            var v = buf.readByte().toInt()
            var z = 0x7f and v
            var shift = 7
            while (v and 0x80 != 0) {
                if (shift > 28) {
                    throw IllegalStateException("Shift too large in decode")
                }
                v = buf.readByte().toInt()
                z += v and 0x7f shl shift
                shift += 7
            }
            return z
        }

        /**
         * Computes an interpolated value of a quantile that is between two centroids.
         *
         * Index is the quantile desired multiplied by the total number of samples - 1.
         *
         * @param index              Denormalized quantile desired
         * @param previousIndex      The denormalized quantile corresponding to the center of the previous centroid.
         * @param nextIndex          The denormalized quantile corresponding to the center of the following centroid.
         * @param previousMean       The mean of the previous centroid.
         * @param nextMean           The mean of the following centroid.
         * @return  The interpolated mean.
         */
        internal fun quantile(
            index: Double,
            previousIndex: Double,
            nextIndex: Double,
            previousMean: Double,
            nextMean: Double
        ): Double {
            val delta = nextIndex - previousIndex
            val previousWeight = (nextIndex - index) / delta
            val nextWeight = (index - previousIndex) / delta
            return previousMean * previousWeight + nextMean * nextWeight
        }
    }
}
