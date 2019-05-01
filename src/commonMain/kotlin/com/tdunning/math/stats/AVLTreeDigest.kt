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

import kotlin.js.JsName
import kotlin.math.abs

class AVLTreeDigest  : AbstractTDigest {
    private val compression: Double
    private var summary: AVLGroupTree?

    private var count: Long = 0 // package private for testing

    /**
     * A histogram structure that will record a sketch of a distribution.
     *
     * @param compression How should accuracy be traded for size?  A value of N here will give quantile errors
     * almost always less than 3/N with considerably smaller errors expected for extreme
     * quantiles.  Conversely, you should expect to track about 5 N centroids for this
     * accuracy.
     */
    constructor(compression: Double) {
        this.compression = compression
        summary = AVLGroupTree(false)
    }

    override fun recordAllData(): TDigest {
        if (summary!!.size != 0) {
            throw IllegalStateException("Can only ask to record added data on an empty summary")
        }
        summary = AVLGroupTree(true)
        return super.recordAllData()
    }

    override fun centroidCount(): Int {
        return summary!!.size
    }

    override  fun add(x: Double, w: Int, base: Centroid) {
        if (x != base.mean() || w != base.count()) {
            throw IllegalArgumentException()
        }
        add(x, w, base.data())
    }

    override fun add(x: Double, w: Int) {
        add(x, w, null as MutableList<Double>?)
    }

    override fun add(others: List<TDigest>) {
        for (other in others) {
            setMinMax(kotlin.math.min(min, other.min), kotlin.math.max(max, other.max))
            for (centroid in other.centroids()) {
                add(centroid.mean(), centroid.count(), if (isRecording) centroid.data() else null)
            }
        }
    }

    fun add(x: Double, w: Int, data: MutableList<Double>?) {
        checkValue(x)
        if (x < min) {
            min = x
        }
        if (x > max) {
            max = x
        }
        var start = summary!!.floor(x)
        if (start == IntAVLTree.NIL) {
            start = summary!!.first()
        }

        if (start == IntAVLTree.NIL) { // empty summary
            mpassert(summary!!.size == 0)
            summary!!.add(x, w, data)
            count = w.toLong()
        } else {
            var minDistance = Double.MAX_VALUE
            var lastNeighbor = IntAVLTree.NIL
            run {
                var neighbor = start
                while (neighbor != IntAVLTree.NIL) {
                    val z = abs(summary!!.mean(neighbor) - x)
                    if (z < minDistance) {
                        start = neighbor
                        minDistance = z
                    } else if (z > minDistance) {
                        // as soon as z increases, we have passed the nearest neighbor and can quit
                        lastNeighbor = neighbor
                        break
                    }
                    neighbor = summary!!.next(neighbor)
                }
            }

            var closest = IntAVLTree.NIL
            var n = 0.0
            var neighbor = start
            while (neighbor != lastNeighbor) {
                mpassert(minDistance == abs(summary!!.mean(neighbor) - x))
                val q0 = summary!!.headSum(neighbor).toDouble() / count
                val q1 = q0 + summary!!.count(neighbor).toDouble() / count
                val k = count * kotlin.math.min(scale.max(q0, compression, count.toDouble()), scale.max(q1, compression, count.toDouble()))

                // this slightly clever selection method improves accuracy with lots of repeated points
                // what it does is sample uniformly from all clusters that have room
                if (summary!!.count(neighbor) + w <= k) {
                    n++
                    if (gen.nextDouble() < 1 / n) {
                        closest = neighbor
                    }
                }
                neighbor = summary!!.next(neighbor)
            }

            if (closest == IntAVLTree.NIL) {
                summary!!.add(x, w, data)
            } else {
                // if the nearest point was not unique, then we may not be modifying the first copy
                // which means that ordering can change
                var centroid = summary!!.mean(closest)
                var count = summary!!.count(closest)
                val d = summary!!.data(closest)
                if (d != null) {
                    if (w == 1) {
                        d.add(x)
                    } else {
                        d.addAll(data!!)
                    }
                }
                centroid = weightedAverage(
                    centroid,
                    count.toDouble(),
                    x,
                    w.toDouble()
                )
                count += w
                summary!!.update(closest, centroid, count, d, false)
            }
            count += w.toLong()

            if (summary!!.size > 20 * compression) {
                // may happen in case of sequential points
                compress()
            }
        }
    }

    override fun compress() {
        if (summary!!.size <= 1) {
            return
        }

        var n0 = 0.0
        var k0 = count * scale.max(n0 / count, compression, count.toDouble())
        var node = summary!!.first()
        var w0 = summary!!.count(node)
        var n1 = n0 + summary!!.count(node)

        var w1 = 0
        var k1: Double
        while (node != IntAVLTree.NIL) {
            var after = summary!!.next(node)
            while (after != IntAVLTree.NIL) {
                w1 = summary!!.count(after)
                k1 = count * scale.max((n1 + w1) / count, compression, count.toDouble())
                if (w0 + w1 > kotlin.math.min(k0, k1)) {
                    break
                } else {
                    val mean = weightedAverage(
                        summary!!.mean(node),
                        w0.toDouble(),
                        summary!!.mean(after),
                        w1.toDouble()
                    )
                    val d1 = summary!!.data(node)
                    val d2 = summary!!.data(after)
                    if (d1 != null && d2 != null) {
                        d1.addAll(d2)
                    }
                    summary!!.update(node, mean, w0 + w1, d1, true)

                    val tmp = summary!!.next(after)
                    summary!!.remove(after)
                    after = tmp
                    n1 += w1.toDouble()
                    w0 += w1
                }
            }
            node = after
            if (node != IntAVLTree.NIL) {
                n0 = n1
                k0 = count * scale.max(n0 / count, compression, count.toDouble())
                w0 = w1
                n1 = n0 + w0
            }
        }
    }

    /**
     * Returns the number of samples represented in this histogram.  If you want to know how many
     * centroids are being used, try centroids().size().
     *
     * @return the number of samples that have been added.
     */
    override fun size(): Long {
        return count
    }

    /**
     * @param x the value at which the CDF should be evaluated
     * @return the approximate fraction of all samples that were less than or equal to x.
     */
    override fun cdf(x: Double): Double {
        val values = summary
        if (values!!.size == 0) {
            return Double.NaN
        } else if (values.size == 1) {
            return if (x < values.mean(values.first()))
                0.0
            else if (x > values.mean(values.first()))
                1.0
            else
                0.5
        } else {
            if (x < min) {
                return 0.0
            } else if (x == min) {
                return 0.5 / size()
            }
            mpassert(x > min)

            if (x > max) {
                return 1.0
            } else if (x == max) {
                val n = size()
                return (n - 0.5) / n
            }
            mpassert(x < max)

            val first = values.first()
            val firstMean = values.mean(first)
            if (x > min && x < firstMean) {
                return interpolateTail(values, x, first, firstMean, min)
            }

            val last = values.last()
            val lastMean = values.mean(last)
            if (x < max && x > lastMean) {
                return 1 - interpolateTail(values, x, last, lastMean, max)
            }
            mpassert(values.size >= 2)
            mpassert(x >= firstMean)
            mpassert(x <= lastMean)

            // we scan a across the centroids
            val it = values.iterator()
            val a = it.next()
            var aMean = a.mean()
            var aWeight = a.count().toDouble()

            if (x == aMean) {
                return aWeight / 2.0 / size().toDouble()
            }
            mpassert(x > aMean)

            // b is the look-ahead to the next centroid
            var b = it.next()
            var bMean = b.mean()
            var bWeight = b.count().toDouble()

            mpassert(bMean >= aMean)

            var weightSoFar = 0.0

            // scan to last element
            while (bWeight > 0) {
                mpassert(x > aMean)
                if (x == bMean) {
                    mpassert(bMean > aMean)
                    weightSoFar += aWeight
                    while (it.hasNext()) {
                        b = it.next()
                        if (x == b.mean()) {
                            bWeight += b.count().toDouble()
                        } else {
                            break
                        }
                    }
                    return (weightSoFar + aWeight + bWeight / 2.0) / size()
                }
                mpassert(x < bMean || x > bMean)

                if (x < bMean) {
                    // we are strictly between a and b
                    mpassert(aMean < bMean)
                    if (aWeight == 1.0) {
                        // but a might be a singleton
                        if (bWeight == 1.0) {
                            // we have passed all of a, but none of b, no interpolation
                            return (weightSoFar + 1.0) / size()
                        } else {
                            // only get to interpolate b's weight because a is a singleton and to our left
                            val partialWeight = (x - aMean) / (bMean - aMean) * bWeight / 2.0
                            return (weightSoFar + 1.0 + partialWeight) / size()
                        }
                    } else if (bWeight == 1.0) {
                        // only get to interpolate a's weight because b is a singleton
                        val partialWeight = (x - aMean) / (bMean - aMean) * aWeight / 2.0
                        // half of a is to left of aMean, and half is interpolated
                        return (weightSoFar + aWeight / 2.0 + partialWeight) / size()
                    } else {
                        // neither is singleton
                        val partialWeight = (x - aMean) / (bMean - aMean) * (aWeight + bWeight) / 2.0
                        return (weightSoFar + aWeight / 2.0 + partialWeight) / size()
                    }
                }
                weightSoFar += aWeight

                mpassert(x > bMean)

                if (it.hasNext()) {
                    aMean = bMean
                    aWeight = bWeight

                    b = it.next()
                    bMean = b.mean()
                    bWeight = b.count().toDouble()

                    mpassert(bMean >= aMean)
                } else {
                    bWeight = 0.0
                }
            }
            // shouldn't be possible because x <= lastMean
            throw IllegalStateException("Ran out of centroids")
        }
    }

    private fun interpolateTail(
        values: AVLGroupTree,
        x: Double,
        node: Int,
        mean: Double,
        extremeValue: Double
    ): Double {
        val count = values.count(node)
        mpassert(count > 1)
        if (count == 2) {
            // other sample must be on the other side of the mean
            return 1.0 / size()
        } else {
            // how much weight is available for interpolation?
            val weight = count / 2.0 - 1
            // how much is between min and here?
            val partialWeight = (extremeValue - x) / (extremeValue - mean) * weight
            // account for sample at min along with interpolated weight
            return (partialWeight + 1.0) / size()
        }
    }

    /**
     * @param q The quantile desired.  Can be in the range [0,1].
     * @return The minimum value x such that we think that the proportion of samples is  x is q.
     */
    override fun quantile(q: Double): Double {
        if (q < 0 || q > 1) {
            throw IllegalArgumentException("q should be in [0,1], got $q")
        }

        val values = summary
        if (values!!.size == 0) {
            // no centroids means no data, no way to get a quantile
            return Double.NaN
        } else if (values.size == 1) {
            // with one data point, all quantiles lead to Rome
            return values.iterator().next().mean()
        }

        // if values were stored in a sorted array, index would be the offset we are interested in
        val index = q * count

        // deal with min and max as a special case singletons
        if (index < 1) {
            return min
        }

        if (index >= count - 1) {
            return max
        }

        var currentNode = values.first()
        var currentWeight = values.count(currentNode)

        if (currentWeight == 2 && index <= 2) {
            // first node is a doublet with one sample at min
            // so we can infer location of other sample
            return 2 * values.mean(currentNode) - min
        }

        if (values.count(values.last()) == 2 && index > count - 2) {
            // likewise for last centroid
            return 2 * values.mean(values.last()) - max
        }

        // special edge cases are out of the way now ... continue with normal stuff

        // weightSoFar represents the total mass to the left of the center of the current node
        var weightSoFar = currentWeight / 2.0

        // at left boundary, we interpolate between min and first mean
        if (index < weightSoFar) {
            // we know that there was a sample exactly at min so we exclude that
            // from the interpolation
            return weightedAverage(
                min,
                weightSoFar - index,
                values.mean(currentNode),
                index - 1
            )
        }
        for (i in 0 until values.size - 1) {
            val nextNode = values.next(currentNode)
            val nextWeight = values.count(nextNode)
            // this is the mass between current center and next center
            val dw = (currentWeight + nextWeight) / 2.0
            if (index < weightSoFar + dw) {
                // index is bracketed between centroids

                // deal with singletons if present
                var leftExclusion = 0.0
                var rightExclusion = 0.0
                if (currentWeight == 1) {
                    if (index < weightSoFar + 0.5) {
                        return values.mean(currentNode)
                    } else {
                        leftExclusion = 0.5
                    }
                }
                if (nextWeight == 1) {
                    if (index >= weightSoFar + dw - 0.5) {
                        return values.mean(nextNode)
                    } else {
                        rightExclusion = 0.5
                    }
                }
                // if both are singletons, we will have returned a result already
                mpassert(leftExclusion + rightExclusion < 1)
                mpassert(dw > 1)
                // centroids i and i+1 bracket our current point
                // we interpolate, but the weights are diminished if singletons are present
                val w1 = index - weightSoFar - leftExclusion
                val w2 = weightSoFar + dw - index - rightExclusion
                return weightedAverage(
                    values.mean(currentNode),
                    w2,
                    values.mean(nextNode),
                    w1
                )
            }
            weightSoFar += dw
            currentNode = nextNode
            currentWeight = nextWeight
        }
        // index is in the right hand side of the last node, interpolate to max
        // we have already handled the case were last centroid is a singleton
        mpassert(currentWeight > 1)
        mpassert(index - weightSoFar < currentWeight / 2 - 1)
        mpassert(count - weightSoFar > 0.5)

        val w1 = index - weightSoFar
        val w2 = count.toDouble() - 1.0 - index
        return weightedAverage(values.mean(currentNode), w2, max, w1)
    }

    override fun centroids(): Collection<Centroid> {
        return summary!!
    }

    override fun compression(): Double {
        return compression
    }

    /**
     * Returns an upper bound on the number bytes that will be required to represent this histogram.
     */
    override fun byteSize(): Int {
        compress()
        return 32 + summary!!.size * 12
    }

    /**
     * Returns an upper bound on the number of bytes that will be required to represent this histogram in
     * the tighter representation.
     */
    override fun smallByteSize(): Int {
        val bound = byteSize()
        var res=0
        val buf= buildBinaryOutput(bound) {
            asSmallBytes(this)
            res=this.size
        }
        buf.release()
        return res
    }

    /**
     * Outputs a histogram as bytes using a particularly cheesy encoding.
     */
    override fun asBytes(buf: BinaryOutput) {
        buf.writeInt(VERBOSE_ENCODING)
        buf.writeDouble(min)
        buf.writeDouble(max)
        buf.writeDouble(compression().toFloat().toDouble())
        buf.writeInt(summary!!.size)
        for (centroid in summary!!) {
            buf.writeDouble(centroid.mean())
        }

        for (centroid in summary!!) {
            buf.writeInt(centroid.count())
        }
    }

    override fun asSmallBytes(buf: BinaryOutput) {
        buf.writeInt(SMALL_ENCODING)
        buf.writeDouble(min)
        buf.writeDouble(max)
        buf.writeDouble(compression())
        buf.writeInt(summary!!.size)

        var x = 0.0
        for (centroid in summary!!) {
            val delta = centroid.mean() - x
            x = centroid.mean()
            buf.writeFloat(delta.toFloat())
        }

        for (centroid in summary!!) {
            val n = centroid.count()
            encode(buf, n)
        }
    }

    companion object {

        private val VERBOSE_ENCODING = 1
        private val SMALL_ENCODING = 2

        /**
         * Reads a histogram from a byte buffer
         *
         * @param buf The buffer to read from.
         * @return The new histogram structure
         */
        @JsName("fromBytes")
        fun fromBytes(buf: BinaryInput): AVLTreeDigest {
            val encoding = buf.readInt()
            if (encoding == VERBOSE_ENCODING) {
                val min = buf.readDouble()
                val max = buf.readDouble()
                val compression = buf.readDouble()
                val r = AVLTreeDigest(compression)
                r.setMinMax(min, max)
                val n = buf.readInt()
                val means = DoubleArray(n)
                for (i in 0 until n) {
                    means[i] = buf.readDouble()
                }
                for (i in 0 until n) {
                    r.add(means[i], buf.readInt())
                }
                return r
            } else if (encoding == SMALL_ENCODING) {
                val min = buf.readDouble()
                val max = buf.readDouble()
                val compression = buf.readDouble()
                val r = AVLTreeDigest(compression)
                r.setMinMax(min, max)
                val n = buf.readInt()
                val means = DoubleArray(n)
                var x = 0.0
                for (i in 0 until n) {
                    val delta = buf.readFloat().toDouble()
                    x += delta
                    means[i] = x
                }

                for (i in 0 until n) {
                    val z = decode(buf)
                    r.add(means[i], z)
                }
                return r
            } else {
                throw IllegalStateException("Invalid format for serialized histogram")
            }
        }
    }

}
