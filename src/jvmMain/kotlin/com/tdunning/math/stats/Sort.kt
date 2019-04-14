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

/**
 * Static sorting methods
 */
object Sort {

    /**
     * Quick sort using an index array.  On return,
     * values[order[i]] is in order as i goes 0..n
     *
     * @param order  Indexes into values
     * @param values The values to sort.
     * @param n      The number of values to sort
     */
    fun sort(order: IntArray, values: DoubleArray, n: Int) {
        sort(order, values, 0, n)
    }

    /**
     * Quick sort using an index array.  On return,
     * values[order[i]] is in order as i goes start..n
     *
     * @param order  Indexes into values
     * @param values The values to sort.
     * @param start  The first element to sort
     * @param n      The number of values to sort
     */
    fun sort(order: IntArray, values: DoubleArray, start: Int = 0, n: Int = values.size) {
        for (i in 0 until n) {
            order[i] = i
        }
        quickSort(order, values, start, n, 8)
        insertionSort(order, values, start, n, 8)
    }

    /**
     * Standard quick sort except that sorting is done on an index array rather than the values themselves
     *
     * @param order  The pre-allocated index array
     * @param values The values to sort
     * @param start  The beginning of the values to sort
     * @param end    The value after the last value to sort
     * @param limit  The minimum size to recurse down to.
     */
    private fun quickSort(order: IntArray, values: DoubleArray, start: Int, end: Int, limit: Int) {
        var start = start
        var end = end
        // the while loop implements tail-recursion to avoid excessive stack calls on nasty cases
        while (end - start > limit) {

            // median of three values for the pivot
            val a = start
            val b = (start + end) / 2
            val c = end - 1

            val pivotIndex: Int
            val pivotValue: Double
            val va = values[order[a]]
            val vb = values[order[b]]
            val vc = values[order[c]]

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
            swap(order, start, pivotIndex)

            // we use a three way partition because many duplicate values is an important case

            var low = start + 1   // low points to first value not known to be equal to pivotValue
            var high = end        // high points to first value > pivotValue
            var i = low           // i scans the array
            while (i < high) {
                // invariant:  values[order[k]] == pivotValue for k in [0..low)
                // invariant:  values[order[k]] < pivotValue for k in [low..i)
                // invariant:  values[order[k]] > pivotValue for k in [high..end)
                // in-loop:  i < high
                // in-loop:  low < high
                // in-loop:  i >= low
                val vi = values[order[i]]
                if (vi == pivotValue) {
                    if (low != i) {
                        swap(order, low, i)
                    } else {
                        i++
                    }
                    low++
                } else if (vi > pivotValue) {
                    high--
                    swap(order, i, high)
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
                swap(order, from++, to--)
                i++
            }
            if (from == low) {
                // ran out of things to copy.  This means that the the last destination is the boundary
                low = to + 1
            } else {
                // ran out of places to copy to.  This means that there are uncopied pivots and the
                // boundary is at the beginning of those
                low = from
            }

            //            checkPartition(order, values, pivotValue, start, low, high, end);

            // now recurse, but arrange it so we handle the longer limit by tail recursion
            if (low - start < end - high) {
                quickSort(order, values, start, low, limit)

                // this is really a way to do
                //    quickSort(order, values, high, end, limit);
                start = high
            } else {
                quickSort(order, values, high, end, limit)
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
        insertionSort(key, values, start, n, 8)
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
        var start = start
        var end = end
        // the while loop implements tail-recursion to avoid excessive stack calls on nasty cases
        while (end - start > limit) {

            // median of three values for the pivot
            val a = start
            val b = (start + end) / 2
            val c = end - 1

            val pivotIndex: Int
            val pivotValue: Double
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

            var low = start + 1   // low points to first value not known to be equal to pivotValue
            var high = end        // high points to first value > pivotValue
            var i = low           // i scans the array
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
            if (from == low) {
                // ran out of things to copy.  This means that the the last destination is the boundary
                low = to + 1
            } else {
                // ran out of places to copy to.  This means that there are uncopied pivots and the
                // boundary is at the beginning of those
                low = from
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
        for (i in start + 1 until end) {
            val v = key[i]
            val m = Math.max(i - limit, start)
            for (j in i downTo m) {
                if (j == 0 || key[j - 1] <= v) {
                    if (j < i) {
                        System.arraycopy(key, j, key, j + 1, i - j)
                        for (value in values) {
                            System.arraycopy(value, j, value, j + 1, i - j)
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
        order: IntArray,
        values: DoubleArray,
        pivotValue: Double,
        start: Int,
        low: Int,
        high: Int,
        end: Int
    ) {
        if (order.size != values.size) {
            throw IllegalArgumentException("Arguments must be same size")
        }

        if (!(start >= 0 && low >= start && high >= low && end >= high)) {
            throw IllegalArgumentException(String.format("Invalid indices %d, %d, %d, %d", start, low, high, end))
        }

        for (i in 0 until low) {
            val v = values[order[i]]
            if (v >= pivotValue) {
                throw IllegalArgumentException(String.format("Value greater than pivot at %d", i))
            }
        }

        for (i in low until high) {
            if (values[order[i]] != pivotValue) {
                throw IllegalArgumentException(String.format("Non-pivot at %d", i))
            }
        }

        for (i in high until end) {
            val v = values[order[i]]
            if (v <= pivotValue) {
                throw IllegalArgumentException(String.format("Value less than pivot at %d", i))
            }
        }
    }

    /**
     * Limited range insertion sort.  We assume that no element has to move more than limit steps
     * because quick sort has done its thing.
     *
     * @param order  The permutation index
     * @param values The values we are sorting
     * @param start  Where to start the sort
     * @param n      How many elements to sort
     * @param limit  The largest amount of disorder
     */
    private fun insertionSort(order: IntArray, values: DoubleArray, start: Int, n: Int, limit: Int) {
        for (i in start + 1 until n) {
            val t = order[i]
            val v = values[order[i]]
            val m = Math.max(i - limit, start)
            for (j in i downTo m) {
                if (j == 0 || values[order[j - 1]] <= v) {
                    if (j < i) {
                        System.arraycopy(order, j, order, j + 1, i - j)
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
/**
 * Quick sort using an index array.  On return,
 * values[order[i]] is in order as i goes 0..values.length
 *
 * @param order  Indexes into values
 * @param values The values to sort.
 */
/**
 * Reverses an array in-place.
 *
 * @param order The array to reverse
 */
