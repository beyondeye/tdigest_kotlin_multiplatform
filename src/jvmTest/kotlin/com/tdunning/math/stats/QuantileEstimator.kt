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

/*
 * Copied verbatim from Datafu which originally had the implementation from
 * Google's Sawzall.  This version is only used for testing.
 */
package com.tdunning.math.stats

import java.util.ArrayList
import java.util.Collections

/**
 * Quantile estimation removed from DataFu library class StreamingQuantile for comparison.
 *
 * Computes approximate quantiles for a (not necessarily sorted) input bag, using the Munro-Paterson algorithm.
 *
 * The algorithm is described here: http://www.cs.ucsb.edu/~suri/cs290/MunroPat.pdf
 *
 * The implementation is based on the one in Sawzall, available here: szlquantile.cc
 */
class QuantileEstimator(private val numQuantiles: Int) {

    private val buffer = ArrayList<MutableList<Double>?>()
    private val maxElementsPerBuffer: Int
    private var totalElements: Int = 0
    private var min: Double = 0.toDouble()
    private var max: Double = 0.toDouble()

    val quantiles: List<Double>
        get() {
            val quantiles = ArrayList<Double>()
            quantiles.add(min)

            if (buffer[0] != null) {
                Collections.sort(buffer[0])
            }
            if (buffer[1] != null) {
                Collections.sort(buffer[1])
            }

            val index = IntArray(buffer.size)
            var S: Long = 0
            for (i in 1..numQuantiles - 2) {
                val targetS = Math.ceil(i * (totalElements / (numQuantiles - 1.0))).toLong()

                while (true) {
                    var smallest = max
                    var minBufferId = -1
                    for (j in buffer.indices) {
                        if (buffer[j] != null && index[j] < buffer[j]!!.size) {
                            if (smallest >= buffer[j]!![index[j]]) {
                                smallest = buffer[j]!![index[j]]
                                minBufferId = j
                            }
                        }
                    }

                    val incrementS = if (minBufferId <= 1) 1L else 0x1L shl minBufferId - 1
                    if (S + incrementS >= targetS) {
                        quantiles.add(smallest)
                        break
                    } else {
                        index[minBufferId]++
                        S += incrementS
                    }
                }
            }

            quantiles.add(max)
            return quantiles
        }

    init {
        this.maxElementsPerBuffer = computeMaxElementsPerBuffer()
    }

    private fun computeMaxElementsPerBuffer(): Int {
        val epsilon = 1.0 / (numQuantiles - 1.0)
        var b = 2
        while ((b - 2) * (0x1L shl b - 2) + 0.5 <= epsilon * MAX_TOT_ELEMS) {
            ++b
        }
        return (MAX_TOT_ELEMS / (0x1L shl b - 1)).toInt()
    }

    private fun ensureBuffer(level: Int) {
        while (buffer.size < level + 1) {
            buffer.add(null)
        }
        if (buffer[level] == null) {
            buffer[level] = ArrayList()
        }
    }

    private fun collapse(a: MutableList<Double>, b: MutableList<Double>, out: MutableList<Double>) {
        var indexA = 0
        var indexB = 0
        var count = 0
        var smaller: Double?
        while (indexA < maxElementsPerBuffer || indexB < maxElementsPerBuffer) {
            if (indexA >= maxElementsPerBuffer || indexB < maxElementsPerBuffer && a[indexA] >= b[indexB]) {
                smaller = b[indexB++]
            } else {
                smaller = a[indexA++]
            }

            if (count++ % 2 == 0) {
                out.add(smaller)
            }
        }
        a.clear()
        b.clear()
    }

    private fun recursiveCollapse(buf: MutableList<Double>, level: Int) {
        ensureBuffer(level + 1)

        val merged: MutableList<Double>
        if (buffer[level + 1]!!.isEmpty()) {
            merged = buffer[level + 1]!!
        } else {
            merged = ArrayList(maxElementsPerBuffer)
        }

        collapse(buffer[level]!!, buf, merged)
        if (buffer[level + 1] !== merged) {
            recursiveCollapse(merged, level + 1)
        }
    }

    fun add(elem: Double) {
        if (totalElements == 0 || elem < min) {
            min = elem
        }
        if (totalElements == 0 || max < elem) {
            max = elem
        }

        if (totalElements > 0 && totalElements % (2 * maxElementsPerBuffer) == 0) {
            Collections.sort(buffer[0])
            Collections.sort(buffer[1])
            recursiveCollapse(buffer[0]!!, 1)
        }

        ensureBuffer(0)
        ensureBuffer(1)
        val index = if (buffer[0]!!.size < maxElementsPerBuffer) 0 else 1
        buffer[index]!!.add(elem)
        totalElements++
    }

    fun clear() {
        buffer.clear()
        totalElements = 0
    }

    fun serializedSize(): Int {
        var r = 4 + 4 + 4 + 4 + 4
        for (b1 in buffer) {
            r += b1!!.size
        }
        return r
    }

    companion object {
        private val MAX_TOT_ELEMS = 1024L * 1024L * 1024L * 1024L
    }
}