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

import java.nio.ByteBuffer
import java.util.Collections

class AVLTreeDigest
/**
 * A histogram structure that will record a sketch of a distribution.
 *
 * @param compression How should accuracy be traded for size?  A value of N here will give quantile errors
 * almost always less than 3/N with considerably smaller errors expected for extreme
 * quantiles.  Conversely, you should expect to track about 5 N centroids for this
 * accuracy.
 */
    (private val compression: Double) : AbstractTDigest() {
    private var summary: AVLGroupTree?

    private var count: Long = 0 // package private for testing

    init {
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

    override internal fun add(x: Double, w: Int, base: Centroid) {
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
            setMinMax(Math.min(min, other.min), Math.max(max, other.max))
            for (centroid in other.centroids()) {
                add(centroid.mean(), centroid.count(), if (isRecording) centroid.data() else null)
            }
        }
    }

    fun add(x: Double, w: Int, data: MutableList<Double>?) {
        checkValue(x)
        if (x < min) {
            min=x
        }
        if (x > max) {
            max= x
        }
        var start = summary!!.floor(x)
        if (start == IntAVLTree.NIL) {
            start = summary!!.first()
        }

        if (start == IntAVLTree.NIL) { // empty summary
            assert(summary!!.size == 0)
            summary!!.add(x, w, data)
            count = w.toLong()
        } else {
            var minDistance = java.lang.Double.MAX_VALUE
            var lastNeighbor = IntAVLTree.NIL
            run {
                var neighbor = start
                while (neighbor != IntAVLTree.NIL) {
                    val z = Math.abs(summary!!.mean(neighbor) - x)
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
            var sum = summary!!.headSum(start)
            var n = 0.0
            var neighbor = start
            while (neighbor != lastNeighbor) {
                assert(minDistance == Math.abs(summary!!.mean(neighbor) - x))
                val q = if (count == 1L) 0.5 else (sum + (summary!!.count(neighbor) - 1) / 2.0) / (count - 1)
                val k = 4.0 * count.toDouble() * q * (1 - q) / compression

                // this slightly clever selection method improves accuracy with lots of repeated points
                if (summary!!.count(neighbor) + w <= k) {
                    n++
                    if (gen.nextDouble() < 1 / n) {
                        closest = neighbor
                    }
                }
                sum += summary!!.count(neighbor).toLong()
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
                centroid = weightedAverage(centroid, count.toDouble(), x, w.toDouble())
                count += w
                summary!!.update(closest, centroid, count, d)
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

        val centroids = summary
        this.summary = AVLGroupTree(isRecording)

        val nodes = IntArray(centroids!!.size)
        nodes[0] = centroids.first()
        for (i in 1 until nodes.size) {
            nodes[i] = centroids.next(nodes[i - 1])
            assert(nodes[i] != IntAVLTree.NIL)
        }
        assert(centroids.next(nodes[nodes.size - 1]) == IntAVLTree.NIL)

        for (i in centroids.size - 1 downTo 1) {
            val other = gen.nextInt(i + 1)
            val tmp = nodes[other]
            nodes[other] = nodes[i]
            nodes[i] = tmp
        }

        for (node in nodes) {
            add(centroids.mean(node), centroids.count(node), centroids.data(node))
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
            return java.lang.Double.NaN
        } else if (values.size == 1) {
            return (if (x < values.mean(values.first())) 0 else 1).toDouble()
        } else {
            var r = 0.0

            // we scan a across the centroids
            val it = values.iterator()
            var a = it.next()

            // b is the look-ahead to the next centroid
            var b = it.next()

            // initially, we set left width equal to right width
            var left = (b.mean() - a.mean()) / 2
            var right = left

            // scan to next to last element
            while (it.hasNext()) {
                if (x < a.mean() + right) {
                    val value = (r + a.count() * interpolate(
                        x,
                        a.mean() - left,
                        a.mean() + right
                    )) / count
                    return if (value > 0.0) value else 0.0
                }

                r += a.count().toDouble()

                a = b
                left = right

                b = it.next()
                right = (b.mean() - a.mean()) / 2
            }

            // for the last element, assume right width is same as left
            return if (x < a.mean() + right) {
                (r + a.count() * interpolate(x, a.mean() - left, a.mean() + right)) / count
            } else {
                1.0
            }
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
            return java.lang.Double.NaN
        } else if (values.size == 1) {
            // with one data point, all quantiles lead to Rome
            return values.iterator().next().mean()
        }

        // if values were stored in a sorted array, index would be the offset we are interested in
        val index = q * count
        var currentNode = values.first()
        var currentWeight = values.count(currentNode)

        // weightSoFar represents the total mass to the left of the center of the current node
        var weightSoFar = currentWeight / 2.0

        // at left boundary, we interpolate between min and first mean
        if (index < weightSoFar) {
            return (min * index + values.mean(currentNode) * (weightSoFar - index)) / weightSoFar
        }
        for (i in 0 until values.size - 1) {
            val nextNode = values.next(currentNode)
            val nextWeight = values.count(nextNode)
            // this is the mass between current center and next center
            val dw = (currentWeight + nextWeight) / 2.0
            if (weightSoFar + dw > index) {
                // centroids i and i+1 bracket our current point
                val z1 = index - weightSoFar
                val z2 = weightSoFar + dw - index
                return weightedAverage(
                    values.mean(currentNode),
                    z2,
                    values.mean(nextNode),
                    z1
                )
            }
            weightSoFar += dw
            currentNode = nextNode
            currentWeight = nextWeight
        }
        // index is in the right hand side of the last node, interpolate to max
        val z1 = index - weightSoFar
        val z2 = currentWeight / 2.0 - z1
        return weightedAverage(values.mean(currentNode), z2, max, z1)
    }

    override fun centroids(): Collection<Centroid> {
        //*PORT* define a platform dependent impl of  Collections.unmodifiableCollection
        return Collections.unmodifiableCollection(summary!!)
    }

    override fun compression(): Double {
        return compression
    }

    /**
     * Returns an upper bound on the number bytes that will be required to represent this histogram.
     */
    override fun byteSize(): Int {
        return 32 + summary!!.size * 12
    }

    /**
     * Returns an upper bound on the number of bytes that will be required to represent this histogram in
     * the tighter representation.
     */
    override fun smallByteSize(): Int {
        val bound = byteSize()
        val buf = ByteBuffer.allocate(bound)
        asSmallBytes(buf)
        return buf.position()
    }

    /**
     * Outputs a histogram as bytes using a particularly cheesy encoding.
     */
    override fun asBytes(buf: ByteBuffer) {
        buf.putInt(VERBOSE_ENCODING)
        buf.putDouble(min)
        buf.putDouble(max)
        buf.putDouble(compression().toFloat().toDouble())
        buf.putInt(summary!!.size)
        for (centroid in summary!!) {
            buf.putDouble(centroid.mean())
        }

        for (centroid in summary!!) {
            buf.putInt(centroid.count())
        }
    }

    override fun asSmallBytes(buf: ByteBuffer) {
        buf.putInt(SMALL_ENCODING)
        buf.putDouble(min)
        buf.putDouble(max)
        buf.putDouble(compression())
        buf.putInt(summary!!.size)

        var x = 0.0
        for (centroid in summary!!) {
            val delta = centroid.mean() - x
            x = centroid.mean()
            buf.putFloat(delta.toFloat())
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
        fun fromBytes(buf: ByteBuffer): AVLTreeDigest {
            val encoding = buf.int
            if (encoding == VERBOSE_ENCODING) {
                val min = buf.double
                val max = buf.double
                val compression = buf.double
                val r = AVLTreeDigest(compression)
                r.setMinMax(min, max)
                val n = buf.int
                val means = DoubleArray(n)
                for (i in 0 until n) {
                    means[i] = buf.double
                }
                for (i in 0 until n) {
                    r.add(means[i], buf.int)
                }
                return r
            } else if (encoding == SMALL_ENCODING) {
                val min = buf.double
                val max = buf.double
                val compression = buf.double
                val r = AVLTreeDigest(compression)
                r.setMinMax(min, max)
                val n = buf.int
                val means = DoubleArray(n)
                var x = 0.0
                for (i in 0 until n) {
                    val delta = buf.float.toDouble()
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
