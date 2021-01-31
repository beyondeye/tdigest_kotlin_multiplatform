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

import kotlin.jvm.JvmOverloads
import kotlin.random.Random

/**
 * Static sorting methods
 */
object Sort {
    private val prng = Random(0) // for choosing pivots during quicksort

    /**
     * Single-key stabilized quick sort on using an index array
     *
     * @param order   Indexes into values
     * @param values  The values to sort.
     * @param n       The number of values to sort
     */
    fun stableSort(order: IntArray, values: DoubleArray, n: Int) {
        for (i in 0 until n) {
            order[i] = i
        }
        stableQuickSort(order, values, 0, n, 64)
        stableInsertionSort(order, values, 0, n, 64)
    }

    /**
     * Two-key quick sort on (values, weights) using an index array
     *
     * @param order   Indexes into values
     * @param values  The values to sort.
     * @param weights The secondary sort key
     * @param n       The number of values to sort
     * @return true if the values were already sorted
     */
    fun sort(order: IntArray, values: DoubleArray, weights: DoubleArray?, n: Int): Boolean {
        var weights = weights
        if (weights == null) {
            weights = values.copyOf(values.size)
        }
        val r = sort(order, values, weights, 0, n)
        // now adjust all runs with equal value so that bigger weights are nearer
        // the median
        var medianWeight = 0.0
        for (i in 0 until n) {
            medianWeight += weights[i]
        }
        medianWeight = medianWeight / 2
        var i = 0
        var soFar = 0.0
        var nextGroup = 0.0
        while (i < n) {
            var j = i
            while (j < n && values[order[j]] == values[order[i]]) {
                val w = weights[order[j]]
                nextGroup += w
                j++
            }
            if (j > i + 1) {
                if (soFar >= medianWeight) {
                    // entire group is in last half, reverse the order
                    reverse(order, i, j - i)
                } else if (nextGroup > medianWeight) {
                    // group straddles the median, but not necessarily evenly
                    // most elements are probably unit weight if there are many
                    val scratch = DoubleArray(j - i)
                    var netAfter = nextGroup + soFar - 2 * medianWeight
                    // heuristically adjust weights to roughly balance around median
                    val max = weights[order[j - 1]]
                    for (k in j - i - 1 downTo 0) {
                        val weight = weights[order[i + k]]
                        if (netAfter < 0) {
                            // sort in normal order
                            scratch[k] = weight
                            netAfter += weight
                        } else {
                            // sort reversed, but after normal items
                            scratch[k] = 2 * max + 1 - weight
                            netAfter -= weight
                        }
                    }
                    // sort these balanced weights
                    val sub = IntArray(j - i)
                    sort(sub, scratch, scratch, 0, j - i)
                    val tmp: IntArray = order.copyOfRange( i, j)
                    for (k in 0 until j - i) {
                        order[i + k] = tmp[sub[k]]
                    }
                }
            }
            soFar = nextGroup
            i = j
        }
        return r
    }

    /**
     * Two-key quick sort on (values, weights) using an index array
     *
     * @param order   Indexes into values
     * @param values  The values to sort
     * @param weights The weights that define the secondary ordering
     * @param start   The first element to sort
     * @param n       The number of values to sort
     * @return True if the values were in order without sorting
     */
    private fun sort(order: IntArray, values: DoubleArray, weights: DoubleArray, start: Int, n: Int): Boolean {
        var inOrder = true
        for (i in start until start + n) {
            if (inOrder && i < start + n - 1) {
                inOrder = values[i] < values[i + 1] || values[i] == values[i + 1] && weights[i] <= weights[i + 1]
            }
            order[i] = i
        }
        if (inOrder) {
            return true
        }
        quickSort(order, values, weights, start, start + n, 64)
        insertionSort(order, values, weights, start, start + n, 64)
        return false
    }

    /**
     * Standard two-key quick sort on (values, weights) except that sorting is done on an index array
     * rather than the values themselves
     *
     * @param order   The pre-allocated index array
     * @param values  The values to sort
     * @param weights The weights (secondary key)
     * @param start   The beginning of the values to sort
     * @param end     The value after the last value to sort
     * @param limit   The minimum size to recurse down to.
     */
    private fun quickSort(
        order: IntArray,
        values: DoubleArray,
        weights: DoubleArray,
        start: Int,
        end: Int,
        limit: Int
    ) {
        // the while loop implements tail-recursion to avoid excessive stack calls on nasty cases
        var start = start
        var end = end
        while (end - start > limit) {

            // pivot by a random element
            val pivotIndex: Int = start + prng.nextInt(end - start)
            val pivotValue = values[order[pivotIndex]]
            val pivotWeight = weights[order[pivotIndex]]

            // move pivot to beginning of array
            swap(order, start, pivotIndex)

            // we use a three way partition because many duplicate values is an important case
            var low = start + 1 // low points to first value not known to be equal to pivotValue
            var high = end // high points to first value > pivotValue
            var i = low // i scans the array
            while (i < high) {
                // invariant:  (values,weights)[order[k]] == (pivotValue, pivotWeight) for k in [0..low)
                // invariant:  (values,weights)[order[k]] < (pivotValue, pivotWeight) for k in [low..i)
                // invariant:  (values,weights)[order[k]] > (pivotValue, pivotWeight) for k in [high..end)
                // in-loop:  i < high
                // in-loop:  low < high
                // in-loop:  i >= low
                val vi = values[order[i]]
                val wi = weights[order[i]]
                if (vi == pivotValue && wi == pivotWeight) {
                    if (low != i) {
                        swap(order, low, i)
                    } else {
                        i++
                    }
                    low++
                } else if (vi > pivotValue || vi == pivotValue && wi > pivotWeight) {
                    high--
                    swap(order, i, high)
                } else {
                    // vi < pivotValue || (vi == pivotValue && wi < pivotWeight)
                    i++
                }
            }
            // invariant:  (values,weights)[order[k]] == (pivotValue, pivotWeight) for k in [0..low)
            // invariant:  (values,weights)[order[k]] < (pivotValue, pivotWeight) for k in [low..i)
            // invariant:  (values,weights)[order[k]] > (pivotValue, pivotWeight) for k in [high..end)
            // assert i == high || low == high therefore, we are done with partition

            // at this point, i==high, from [start,low) are == pivot, [low,high) are < and [high,end) are >
            // we have to move the values equal to the pivot into the middle.  To do this, we swap pivot
            // values into the top end of the [low,high) range stopping when we run out of destinations
            // or when we run out of values to copy
            var from = start
            var to = high - 1
            i = 0
            while (from < low && to >= low) {
                swap(order, from++, to--)
                i++
            }
            low = if (from == low) {
                // ran out of things to copy.  This means that the the last destination is the boundary
                to + 1
            } else {
                // ran out of places to copy to.  This means that there are uncopied pivots and the
                // boundary is at the beginning of those
                from
            }

//            checkPartition(order, values, pivotValue, start, low, high, end);

            // now recurse, but arrange it so we handle the longer limit by tail recursion
            // we have to sort the pivot values because they may have different weights
            // we can't do that, however until we know how much weight is in the left and right
            if (low - start < end - high) {
                // left side is smaller
                quickSort(order, values, weights, start, low, limit)

                // this is really a way to do
                //    quickSort(order, values, high, end, limit);
                start = high
            } else {
                quickSort(order, values, weights, high, end, limit)
                // this is really a way to do
                //    quickSort(order, values, start, low, limit);
                end = low
            }
        }
    }

    /**
     * Stablized quick sort on an index array. This is a normal quick sort that uses the
     * original index as a secondary key. Since we are really just sorting an index array
     * we can do this nearly for free.
     *
     * @param order  The pre-allocated index array
     * @param values The values to sort
     * @param start  The beginning of the values to sort
     * @param end    The value after the last value to sort
     * @param limit  The minimum size to recurse down to.
     */
    private fun stableQuickSort(order: IntArray, values: DoubleArray, start: Int, end: Int, limit: Int) {
        // the while loop implements tail-recursion to avoid excessive stack calls on nasty cases
        var start = start
        var end = end
        while (end - start > limit) {

            // pivot by a random element
            val pivotIndex: Int = start + prng.nextInt(end - start)
            val pivotValue = values[order[pivotIndex]]
            val pv = order[pivotIndex]

            // move pivot to beginning of array
            swap(order, start, pivotIndex)

            // we use a three way partition because many duplicate values is an important case
            var low = start + 1 // low points to first value not known to be equal to pivotValue
            var high = end // high points to first value > pivotValue
            var i = low // i scans the array
            while (i < high) {
                // invariant:  (values[order[k]],order[k]) == (pivotValue, pv) for k in [0..low)
                // invariant:  (values[order[k]],order[k]) < (pivotValue, pv) for k in [low..i)
                // invariant:  (values[order[k]],order[k]) > (pivotValue, pv) for k in [high..end)
                // in-loop:  i < high
                // in-loop:  low < high
                // in-loop:  i >= low
                val vi = values[order[i]]
                val pi = order[i]
                if (vi == pivotValue && pi == pv) {
                    if (low != i) {
                        swap(order, low, i)
                    } else {
                        i++
                    }
                    low++
                } else if (vi > pivotValue || vi == pivotValue && pi > pv) {
                    high--
                    swap(order, i, high)
                } else {
                    // vi < pivotValue || (vi == pivotValue && pi < pv)
                    i++
                }
            }
            // invariant:  (values[order[k]],order[k]) == (pivotValue, pv) for k in [0..low)
            // invariant:  (values[order[k]],order[k]) < (pivotValue, pv) for k in [low..i)
            // invariant:  (values[order[k]],order[k]) > (pivotValue, pv) for k in [high..end)
            // assert i == high || low == high therefore, we are done with partition

            // at this point, i==high, from [start,low) are == pivot, [low,high) are < and [high,end) are >
            // we have to move the values equal to the pivot into the middle.  To do this, we swap pivot
            // values into the top end of the [low,high) range stopping when we run out of destinations
            // or when we run out of values to copy
            var from = start
            var to = high - 1
            i = 0
            while (from < low && to >= low) {
                swap(order, from++, to--)
                i++
            }
            low = if (from == low) {
                // ran out of things to copy.  This means that the the last destination is the boundary
                to + 1
            } else {
                // ran out of places to copy to.  This means that there are uncopied pivots and the
                // boundary is at the beginning of those
                from
            }

//            checkPartition(order, values, pivotValue, start, low, high, end);

            // now recurse, but arrange it so we handle the longer limit by tail recursion
            // we have to sort the pivot values because they may have different weights
            // we can't do that, however until we know how much weight is in the left and right
            if (low - start < end - high) {
                // left side is smaller
                stableQuickSort(order, values, start, low, limit)

                // this is really a way to do
                //    quickSort(order, values, high, end, limit);
                start = high
            } else {
                stableQuickSort(order, values, high, end, limit)
                // this is really a way to do
                //    quickSort(order, values, start, low, limit);
                end = low
            }
        }
    }

    /**
     * Quick sort in place of several paired arrays.  On return,
     * keys[...] is in order and the values[] arrays will be
     * reordered as well in the same way.
     *
     * @param key    Values to sort on
     * @param values The auxilliary values to sort.
     */
    fun sort(key: DoubleArray, vararg values: DoubleArray) {
        sort(key, 0, key.size, *values)
    }

    /**
     * Quick sort using an index array.  On return,
     * values[order[i]] is in order as i goes start..n
     * @param key    Values to sort on
     * @param start  The first element to sort
     * @param n      The number of values to sort
     * @param values The auxilliary values to sort.
     */
    fun sort(key: DoubleArray, start: Int, n: Int, vararg values: DoubleArray) {
        quickSort(key, values, start, start + n, 8)
        insertionSort(key, values, start, start + n, 8)
    }

    /**
     * Standard quick sort except that sorting rearranges parallel arrays
     *
     * @param key    Values to sort on
     * @param values The auxilliary values to sort.
     * @param start  The beginning of the values to sort
     * @param end    The value after the last value to sort
     * @param limit  The minimum size to recurse down to.
     */
    private fun quickSort(key: DoubleArray, values: Array<out DoubleArray>, start: Int, end: Int, limit: Int) {
        // the while loop implements tail-recursion to avoid excessive stack calls on nasty cases
        var start = start
        var end = end
        while (end - start > limit) {

            // median of three values for the pivot
            val a = start
            val b = (start + end) / 2
            val c = end - 1
            var pivotIndex: Int
            var pivotValue: Double
            val va = key[a]
            val vb = key[b]
            val vc = key[c]
            if (va > vb) {
                if (vc > va) {
                    // vc > va > vb
                    pivotIndex = a
                    pivotValue = va
                } else {
                    // va > vb, va >= vc
                    if (vc < vb) {
                        // va > vb > vc
                        pivotIndex = b
                        pivotValue = vb
                    } else {
                        // va >= vc >= vb
                        pivotIndex = c
                        pivotValue = vc
                    }
                }
            } else {
                // vb >= va
                if (vc > vb) {
                    // vc > vb >= va
                    pivotIndex = b
                    pivotValue = vb
                } else {
                    // vb >= va, vb >= vc
                    if (vc < va) {
                        // vb >= va > vc
                        pivotIndex = a
                        pivotValue = va
                    } else {
                        // vb >= vc >= va
                        pivotIndex = c
                        pivotValue = vc
                    }
                }
            }

            // move pivot to beginning of array
            swap(start, pivotIndex, key, *values)

            // we use a three way partition because many duplicate values is an important case
            var low = start + 1 // low points to first value not known to be equal to pivotValue
            var high = end // high points to first value > pivotValue
            var i = low // i scans the array
            while (i < high) {
                // invariant:  values[order[k]] == pivotValue for k in [0..low)
                // invariant:  values[order[k]] < pivotValue for k in [low..i)
                // invariant:  values[order[k]] > pivotValue for k in [high..end)
                // in-loop:  i < high
                // in-loop:  low < high
                // in-loop:  i >= low
                val vi = key[i]
                if (vi == pivotValue) {
                    if (low != i) {
                        swap(low, i, key, *values)
                    } else {
                        i++
                    }
                    low++
                } else if (vi > pivotValue) {
                    high--
                    swap(i, high, key, *values)
                } else {
                    // vi < pivotValue
                    i++
                }
            }
            // invariant:  values[order[k]] == pivotValue for k in [0..low)
            // invariant:  values[order[k]] < pivotValue for k in [low..i)
            // invariant:  values[order[k]] > pivotValue for k in [high..end)
            // assert i == high || low == high therefore, we are done with partition

            // at this point, i==high, from [start,low) are == pivot, [low,high) are < and [high,end) are >
            // we have to move the values equal to the pivot into the middle.  To do this, we swap pivot
            // values into the top end of the [low,high) range stopping when we run out of destinations
            // or when we run out of values to copy
            var from = start
            var to = high - 1
            i = 0
            while (from < low && to >= low) {
                swap(from++, to--, key, *values)
                i++
            }
            low = if (from == low) {
                // ran out of things to copy.  This means that the the last destination is the boundary
                to + 1
            } else {
                // ran out of places to copy to.  This means that there are uncopied pivots and the
                // boundary is at the beginning of those
                from
            }

//            checkPartition(order, values, pivotValue, start, low, high, end);

            // now recurse, but arrange it so we handle the longer limit by tail recursion
            if (low - start < end - high) {
                quickSort(key, values, start, low, limit)

                // this is really a way to do
                //    quickSort(order, values, high, end, limit);
                start = high
            } else {
                quickSort(key, values, high, end, limit)
                // this is really a way to do
                //    quickSort(order, values, start, low, limit);
                end = low
            }
        }
    }

    /**
     * Limited range insertion sort.  We assume that no element has to move more than limit steps
     * because quick sort has done its thing. This version works on parallel arrays of keys and values.
     *
     * @param key    The array of keys
     * @param values The values we are sorting
     * @param start  The starting point of the sort
     * @param end    The ending point of the sort
     * @param limit  The largest amount of disorder
     */
    private fun insertionSort(key: DoubleArray, values: Array<out DoubleArray>, start: Int, end: Int, limit: Int) {
        // loop invariant: all values start ... i-1 are ordered
        for (i in start + 1 until end) {
            val v = key[i]
            val m: Int = kotlin.math.max(i - limit, start)
            for (j in i downTo m) {
                if (j == m || key[j - 1] <= v) {
                    if (j < i) {
                        Utils.arraycopy(key, j, key, j + 1, i - j)
                        key[j] = v
                        for (value in values) {
                            val tmp = value[i]
                            Utils.arraycopy(value, j, value, j + 1, i - j)
                            value[j] = tmp
                        }
                    }
                    break
                }
            }
        }
    }

    private fun swap(order: IntArray, i: Int, j: Int) {
        val t = order[i]
        order[i] = order[j]
        order[j] = t
    }

    private fun swap(i: Int, j: Int, key: DoubleArray, vararg values: DoubleArray) {
        var t = key[i]
        key[i] = key[j]
        key[j] = t
        for (k in values.indices) {
            t = values[k][i]
            values[k][i] = values[k][j]
            values[k][j] = t
        }
    }

    /**
     * Check that a partition step was done correctly.  For debugging and testing.
     *
     * @param order      The array of indexes representing a permutation of the keys.
     * @param values     The keys to sort.
     * @param pivotValue The value that splits the data
     * @param start      The beginning of the data of interest.
     * @param low        Values from start (inclusive) to low (exclusive) are &lt; pivotValue.
     * @param high       Values from low to high are equal to the pivot.
     * @param end        Values from high to end are above the pivot.
     */
    fun checkPartition(
        order: IntArray, values: DoubleArray, pivotValue: Double, start: Int, low: Int,
        high: Int, end: Int
    ) {
        if (order.size != values.size) {
            throw IllegalArgumentException("Arguments must be same size")
        }
        if (!(start >= 0 && low >= start && high >= low && end >= high)) {
            throw IllegalArgumentException("Invalid indices $start, $low, $high, $end")
        }
        for (i in 0 until low) {
            val v = values[order[i]]
            if (v >= pivotValue) {
                throw IllegalArgumentException("Value greater than pivot at $i")
            }
        }
        for (i in low until high) {
            if (values[order[i]] != pivotValue) {
                throw IllegalArgumentException("Non-pivot at $i")
            }
        }
        for (i in high until end) {
            val v = values[order[i]]
            if (v <= pivotValue) {
                throw IllegalArgumentException("Value less than pivot at $i")
            }
        }
    }

    /**
     * Limited range insertion sort with primary and secondary key.  We assume that no
     * element has to move more than limit steps because quick sort has done its thing.
     *
     * If weights (the secondary key) is null, then only the primary key is used.
     *
     * This sort is inherently stable.
     *
     * @param order   The permutation index
     * @param values  The values we are sorting
     * @param weights The secondary key for sorting
     * @param start   Where to start the sort
     * @param n       How many elements to sort
     * @param limit   The largest amount of disorder
     */
    private fun insertionSort(
        order: IntArray,
        values: DoubleArray,
        weights: DoubleArray?,
        start: Int,
        n: Int,
        limit: Int
    ) {
        for (i in start + 1 until n) {
            val t = order[i]
            val v = values[order[i]]
            val w: Double = weights?.get(order[i]) ?: 0.0
            val m: Int = kotlin.math.max(i - limit, start)
            // values in [start, i) are ordered
            // scan backwards to find where to stick t
            for (j in i downTo m) {
                if (j == 0 || values[order[j - 1]] < v ||
                    values[order[j - 1]] == v && (weights == null || weights[order[j - 1]] <= w)
                ) {
                    if (j < i) {
                        Utils.arraycopy(order, j, order, j + 1, i - j)
                        order[j] = t
                    }
                    break
                }
            }
        }
    }

    /**
     * Limited range insertion sort with primary key stabilized by the use of the
     * original position to break ties.  We assume that no element has to move more
     * than limit steps because quick sort has done its thing.
     *
     * @param order   The permutation index
     * @param values  The values we are sorting
     * @param start   Where to start the sort
     * @param n       How many elements to sort
     * @param limit   The largest amount of disorder
     */
    private fun stableInsertionSort(order: IntArray, values: DoubleArray, start: Int, n: Int, limit: Int) {
        for (i in start + 1 until n) {
            val t = order[i]
            val v = values[order[i]]
            val vi = order[i]
            val m: Int = kotlin.math.max(i - limit, start)
            // values in [start, i) are ordered
            // scan backwards to find where to stick t
            for (j in i downTo m) {
                if (j == 0 || values[order[j - 1]] < v || values[order[j - 1]] == v && order[j - 1] <= vi) {
                    if (j < i) {
                        Utils.arraycopy(order, j, order, j + 1, i - j)
                        order[j] = t
                    }
                    break
                }
            }
        }
    }
    /**
     * Reverses part of an array. See [.reverse]
     *
     * @param order  The array containing the data to reverse.
     * @param offset Where to start reversing.
     * @param length How many elements to reverse
     */
    /**
     * Reverses an array in-place.
     *
     * @param order The array to reverse
     */
    @JvmOverloads
    fun reverse(order: IntArray, offset: Int = 0, length: Int = order.size) {
        for (i in 0 until length / 2) {
            val t = order[offset + i]
            order[offset + i] = order[offset + length - i - 1]
            order[offset + length - i - 1] = t
        }
    }

    /**
     * Reverses part of an array. See [.reverse]
     *
     * @param order  The array containing the data to reverse.
     * @param offset Where to start reversing.
     * @param length How many elements to reverse
     */
    fun reverse(order: DoubleArray, offset: Int, length: Int) {
        for (i in 0 until length / 2) {
            val t = order[offset + i]
            order[offset + i] = order[offset + length - i - 1]
            order[offset + length - i - 1] = t
        }
    }
}