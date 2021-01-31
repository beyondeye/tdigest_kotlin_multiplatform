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

import com.google.common.collect.HashMultiset
import com.tdunning.math.stats.Sort.sort
import com.tdunning.math.stats.Sort.stableSort
import org.junit.Test

import java.util.Random

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class SortTest {
    @Test
    fun testReverse() {
        var x = IntArray(0)

        // don't crash with no input
        Sort.reverse(x)

        // reverse stuff!
        x = intArrayOf(1, 2, 3, 4, 5)
        Sort.reverse(x)
        for (i in 0..4) {
            assertEquals((5 - i).toLong(), x[i].toLong())
        }

        // reverse some stuff back
        Sort.reverse(x, 1, 3)
        assertEquals(5, x[0].toLong())
        assertEquals(2, x[1].toLong())
        assertEquals(3, x[2].toLong())
        assertEquals(4, x[3].toLong())
        assertEquals(1, x[4].toLong())

        // another no-op
        Sort.reverse(x, 3, 0)
        assertEquals(5, x[0].toLong())
        assertEquals(2, x[1].toLong())
        assertEquals(3, x[2].toLong())
        assertEquals(4, x[3].toLong())
        assertEquals(1, x[4].toLong())

        x = intArrayOf(1, 2, 3, 4, 5, 6)
        Sort.reverse(x)
        for (i in 0..5) {
            assertEquals((6 - i).toLong(), x[i].toLong())
        }
    }

    @Test
    fun testEmpty() {
        Sort.sort(intArrayOf(), doubleArrayOf(),null,0)
    }

    @Test
    fun testOne() {
        val order = IntArray(1)
        Sort.sort(order, doubleArrayOf(1.0), doubleArrayOf(1.0),1)
        assertEquals(0, order[0].toLong())
    }

    @Test
    fun testIdentical() {
        val order = IntArray(6)
        val values = DoubleArray(6)

        Sort.sort(order, values, null, values.size)
        checkOrder(order, values)
    }

    @Test
    fun testRepeated() {
        val n = 50
        val order = IntArray(n)
        val values = DoubleArray(n)
        for (i in 0 until n) {
            values[i] = Math.rint(10 * (i.toDouble() / n)) / 10.0
        }

        Sort.sort(order, values,null,values.size)
        checkOrder(order, values)
    }

    @Test
    fun testRepeatedSortByWeight() {
        // this needs to be long enough to force coverage of both quicksort and insertion sort
        // (i.e. >64)
        val n = 125
        val order = IntArray(n)
        val values = DoubleArray(n)
        val weights = DoubleArray(n)
        var totalWeight = 0.0

        // generate evenly distributed values and weights
        for (i in 0 until n) {
            val k = (i + 5) * 37 % n
            values[i] = Math.floor(k / 25.0)
            weights[i] = (k % 25 + 1).toDouble()
            totalWeight += weights[i]
        }

        // verify: test weights should be evenly distributed
        val tmp = DoubleArray(5)
        for (i in 0 until n) {
            tmp[values[i].toInt()] += weights[i]
        }
        for (v in tmp) {
            assertEquals(totalWeight / tmp.size, v, 0.0)
        }

        // now sort ...
        sort(order, values, weights, n)

        // and verify our somewhat unusual ordering of the result
        // within the first two quintiles, value is constant, weights increase within each quintile
        val delta = order.size / 5
        var sum = checkSubOrder(0.0, order, values, weights, 0, delta, 1)
        assertEquals(totalWeight * 0.2, sum, 0.0)
        sum = checkSubOrder(sum, order, values, weights, delta, 2 * delta, 1)
        assertEquals(totalWeight * 0.4, sum, 0.0)

        // in the middle quintile, weights go up and then down after the median
        sum = checkMidOrder(totalWeight / 2, sum, order, values, weights, 2 * delta, 3 * delta)
        assertEquals(totalWeight * 0.6, sum, 0.0)

        // in the last two quintiles, weights decrease
        sum = checkSubOrder(sum, order, values, weights, 3 * delta, 4 * delta, -1)
        assertEquals(totalWeight * 0.8, sum, 0.0)
        sum = checkSubOrder(sum, order, values, weights, 4 * delta, 5 * delta, -1)
        assertEquals(totalWeight, sum, 0.0)
    }

    @Test
    fun testStableSort() {
        // this needs to be long enough to force coverage of both quicksort and insertion sort
        // (i.e. >64)
        val n = 70
        val z = 10
        val order = IntArray(n)
        val values = DoubleArray(n)
        val weights = DoubleArray(n)
        var totalWeight = 0.0

        // generate evenly distributed values and weights
        for (i in 0 until n) {
            val k = (i + 5) * 37 % n
            values[i] = Math.floor(k / z.toDouble())
            weights[i] = (k % z + 1).toDouble()
            totalWeight += weights[i]
        }

        // verify: test weights should be evenly distributed
        val tmp = DoubleArray(n / z)
        for (i in 0 until n) {
            tmp[values[i].toInt()] += weights[i]
        }
        for (v in tmp) {
            assertEquals(totalWeight / tmp.size, v, 0.0)
        }

        // now sort ...
        stableSort(order, values, n)

        // and verify stability of the ordering
        // values must be in order and they must appear in their original ordering
        var last = -1.0
        for (j in order) {
            val m = values[j] * n + j
            assertTrue(m > last)
            last = m
        }
    }

    private fun checkMidOrder(
        medianWeight: Double,
        sofar: Double,
        order: IntArray,
        values: DoubleArray,
        weights: DoubleArray,
        start: Int,
        end: Int
    ): Double {
        var sofar = sofar
        val value = values[order[start]]
        val last = 0.0
        assertTrue(sofar < medianWeight)
        for (i in start until end) {
            assertEquals(value, values[order[i]], 0.0)
            var w = weights[order[i]]
            assertTrue(w > 0)
            if (sofar > medianWeight) {
                w = 2 * medianWeight - w
            }
            assertTrue(w >= last)
            sofar += weights[order[i]]
        }
        assertTrue(sofar > medianWeight)
        return sofar
    }

    private fun checkSubOrder(
        sofar: Double,
        order: IntArray,
        values: DoubleArray,
        weights: DoubleArray,
        start: Int,
        end: Int,
        ordering: Int
    ): Double {
        var sofar = sofar
        var lastWeight = weights[order[start]] * ordering
        val value = values[order[start]]
        for (i in start until end) {
            assertEquals(value, values[order[i]], 0.0)
            val newOrderedWeight = weights[order[i]] * ordering
            assertTrue(newOrderedWeight >= lastWeight)
            lastWeight = newOrderedWeight
            sofar += weights[order[i]]
        }
        return sofar
    }


    @Test
    fun testShort() {
        val order = IntArray(6)
        val values = DoubleArray(6)

        // all duplicates
        for (i in 0..5) {
            values[i] = 1.0
        }

        Sort.sort(order, values,null,values.size)
        checkOrder(order, values)

        values[0] = 0.8
        values[1] = 0.3

        Sort.sort(order, values,null,values.size)
        checkOrder(order, values)

        values[5] = 1.5
        values[4] = 1.2

        Sort.sort(order, values,null,values.size)
        checkOrder(order, values)
    }

    @Test
    fun testLonger() {
        val order = IntArray(20)
        val values = DoubleArray(20)
        for (i in 0..19) {
            values[i] = (i * 13 % 20).toDouble()
        }
        Sort.sort(order, values,null,values.size)
        checkOrder(order, values)
    }

    @Test
    fun testMultiPivots() {
        // more pivots than low split on first pass
        // multiple pivots, but more low data on second part of recursion
        val order = IntArray(30)
        val values = DoubleArray(30)
        for (i in 0..8) {
            values[i] = (i + 20 * (i % 2)).toDouble()
        }

        for (i in 9..19) {
            values[i] = 10.0
        }

        for (i in 20..29) {
            values[i] = (i - 20 * (i % 2)).toDouble()
        }
        values[29] = 29.0
        values[24] = 25.0
        values[26] = 25.0

        Sort.sort(order, values,null,values.size)
        checkOrder(order, values)
    }

    @Test
    fun testMultiPivotsInPlace() {
        // more pivots than low split on first pass
        // multiple pivots, but more low data on second part of recursion
        val keys = DoubleArray(30)
        for (i in 0..8) {
            keys[i] = (i + 20 * (i % 2)).toDouble()
        }

        for (i in 9..19) {
            keys[i] = 10.0
        }

        for (i in 20..29) {
            keys[i] = (i - 20 * (i % 2)).toDouble()
        }
        keys[29] = 29.0
        keys[24] = 25.0
        keys[26] = 25.0

        val v = valuesFromKeys(keys, 0)

        //*DARIO* original code
        //Sort.sort(keys, *v)
        //checkOrder(keys, 0, keys.size, *v)
        Sort.sort(keys, v)
        checkOrder(keys, 0, keys.size, v)
    }

    @Test
    fun testRandomized() {
        val rand = Random()

        for (k in 0..99) {
            val order = IntArray(30)
            val values = DoubleArray(30)
            for (i in 0..29) {
                values[i] = rand.nextDouble()
            }

            Sort.sort(order, values,null,values.size)
            checkOrder(order, values)
        }
    }

    @Test
    fun testRandomizedShortSort() {
        val rand = Random()

        for (k in 0..99) {
            val keys = DoubleArray(30)
            for (i in 0..9) {
                keys[i] = i.toDouble()
            }
            for (i in 10..19) {
                keys[i] = rand.nextDouble()
            }
            for (i in 20..29) {
                keys[i] = i.toDouble()
            }
            val v0 = valuesFromKeys(keys, 0)
            val v1 = valuesFromKeys(keys, 1)

            Sort.sort(keys, 10, 10, v0, v1)
            checkOrder(keys, 10, 10, v0, v1)
            checkValues(keys, 0, keys.size, v0, v1)
            for (i in 0..9) {
                assertEquals(i.toDouble(), keys[i], 0.0)
            }
            for (i in 20..29) {
                assertEquals(i.toDouble(), keys[i], 0.0)
            }
        }

    }

    /**
     * Generates a vector of values corresponding to a vector of keys.
     *
     * @param keys A vector of keys
     * @param k    Which value vector to generate
     * @return The new vector containing frac(key_i * 3 * 5^k)
     */
    private fun valuesFromKeys(keys: DoubleArray, k: Int): DoubleArray {
        val r = DoubleArray(keys.size)
        var scale = 3.0
        for (i in 0 until k) {
            scale = scale * 5
        }
        for (i in keys.indices) {
            r[i] = fractionalPart(keys[i] * scale)
        }
        return r
    }

    /**
     * Verifies that keys are in order and that each value corresponds to the keys
     *
     * @param key    Array of keys
     * @param start  The starting offset of keys and values to check
     * @param length The number of keys and values to check
     * @param values Arrays of associated values. Value_{ki} = frac(key_i * 3 * 5^k)
     */
    private fun checkOrder(key: DoubleArray, start: Int, length: Int, vararg values: DoubleArray) {
        assert(start + length <= key.size)

        for (i in start until start + length - 1) {
            assertTrue(String.format("bad ordering at %d, %f > %f", i, key[i], key[i + 1]), key[i] <= key[i + 1])
        }

        checkValues(key, start, length, *values)
    }

    private fun checkValues(key: DoubleArray, start: Int, length: Int, vararg values: DoubleArray) {
        var scale = 3.0
        for (k in values.indices) {
            val v = values[k]
            assertEquals(key.size.toLong(), v.size.toLong())
            for (i in start until length) {
                assertEquals(
                    String.format("value %d not correlated, key=%.5f, k=%d, v=%.5f", i, key[i], k, values[k][i]),
                    fractionalPart(key[i] * scale), values[k][i], 0.0
                )
            }
            scale = scale * 5
        }
    }


    private fun fractionalPart(v: Double): Double {
        return v - Math.floor(v)
    }

    private fun checkOrder(order: IntArray, values: DoubleArray) {
        var previous = -java.lang.Double.MAX_VALUE
        val counts = HashMultiset.create<Int>()
        for (i in values.indices) {
            counts.add(i)
            val v = values[order[i]]
            if (v < previous) {
                throw IllegalArgumentException("Values out of order at %d")
            }
            previous = v
        }

        assertEquals(order.size.toLong(), counts.size.toLong())

        for (count in counts) {
            assertEquals(1, counts.count(count).toLong())
        }
    }
}