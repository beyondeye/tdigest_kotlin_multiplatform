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

/**
 * Maintains a t-digest by collecting new points in a buffer that is then sorted occasionally and merged
 * into a sorted array that contains previously computed centroids.
 *
 *
 * This can be very fast because the cost of sorting and merging is amortized over several insertion. If
 * we keep N centroids total and have the input array is k long, then the amortized cost is something like
 *
 *
 * N/k + log k
 *
 *
 * These costs even out when N/k = log k.  Balancing costs is often a good place to start in optimizing an
 * algorithm.  For different values of compression factor, the following table shows estimated asymptotic
 * values of N and suggested values of k:
 * <table>
 * <thead>
 * <tr><td>Compression</td><td>N</td><td>k</td></tr>
</thead> *
 * <tbody>
 * <tr><td>50</td><td>78</td><td>25</td></tr>
 * <tr><td>100</td><td>157</td><td>42</td></tr>
 * <tr><td>200</td><td>314</td><td>73</td></tr>
</tbody> *
 * <caption>Sizing considerations for t-digest</caption>
</table> *
 *
 *
 * The virtues of this kind of t-digest implementation include:
 *
 *  * No allocation is required after initialization
 *  * The data structure automatically compresses existing centroids when possible
 *  * No Java object overhead is incurred for centroids since data is kept in primitive arrays
 *
 *
 *
 * The current implementation takes the liberty of using ping-pong buffers for implementing the merge resulting
 * in a substantial memory penalty, but the complexity of an in place merge was not considered as worthwhile
 * since even with the overhead, the memory cost is less than 40 bytes per centroid which is much less than half
 * what the AVLTreeDigest uses and no dynamic allocation is required at all.
 */
class MergingDigest
/**
 * Fully specified constructor.  Normally only used for deserializing a buffer t-digest.
 *
 * @param compression Compression factor
 * @param bufferSize  Number of temporary centroids
 * @param size        Size of main buffer
 *
 * Normally used constructor:
 * Allocates a buffer merging t-digest.  This is the normally used constructor that
 * allocates default sized internal arrays.  Other versions are available, but should
 * only be used for special cases.
 *
 * @param compression The compression factor
 *
 * constructor with buffer size:
 * If you know the size of the temporary buffer for incoming points, you can use this entry point.
 *
 * @param compression Compression factor for t-digest.  Same as 1/\delta in the paper.
 * @param bufferSize  How many samples to retain before merging.
 */// we can guarantee that we only need 2 * ceiling(compression).
@JvmOverloads constructor(private val compression: Double, bufferSize: Int = -1, size: Int = -1) : AbstractTDigest() {
    private var mergeCount = 0

    // points to the first unused centroid
    private var lastUsedCell: Int = 0

    // sum_i weight[i]  See also unmergedWeight
    private var totalWeight = 0.0

    // number of points that have been added to each merged centroid
    private val weight: DoubleArray
    // mean of points added to each merged centroid
    private val mean: DoubleArray

    // history of all data added to centroids (for testing purposes)
    private var data: MutableList<MutableList<Double>>? = null

    // sum_i tempWeight[i]
    private var unmergedWeight = 0.0

    // this is the index of the next temporary centroid
    // this is a more Java-like convention than lastUsedCell uses
    private var tempUsed = 0
    private val tempWeight: DoubleArray
    private val tempMean: DoubleArray
    private var tempData: MutableList<MutableList<Double>>? = null


    // array used for sorting the temp centroids.  This is a field
    // to avoid allocations during operation
    private val order: IntArray

    init {
        var bufferSize = bufferSize
        var size = size
        if (size == -1) {
            size = (2 * Math.ceil(compression)).toInt()
            if (useWeightLimit) {
                // the weight limit approach generates smaller centroids than necessary
                // that can result in using a bit more memory than expected (but is faster)
                size += 10
            }
        }
        if (bufferSize == -1) {
            // having a big buffer is good for speed
            // experiments show bufferSize = 1 gives half the performance of bufferSize=10
            // bufferSize = 2 gives 40% worse performance than 10
            // but bufferSize = 5 only costs about 5-10%
            //
            //   compression factor     time(us)
            //    50          1         0.275799
            //    50          2         0.151368
            //    50          5         0.108856
            //    50         10         0.102530
            //   100          1         0.215121
            //   100          2         0.142743
            //   100          5         0.112278
            //   100         10         0.107753
            //   200          1         0.210972
            //   200          2         0.148613
            //   200          5         0.118220
            //   200         10         0.112970
            //   500          1         0.219469
            //   500          2         0.158364
            //   500          5         0.127552
            //   500         10         0.121505
            bufferSize = (5 * Math.ceil(compression)).toInt()
        }

        weight = DoubleArray(size)
        mean = DoubleArray(size)

        tempWeight = DoubleArray(bufferSize)
        tempMean = DoubleArray(bufferSize)
        order = IntArray(bufferSize)

        lastUsedCell = 0
    }

    /**
     * Turns on internal data recording.
     */
    override fun recordAllData(): TDigest {
        super.recordAllData()
        data = mutableListOf()
        tempData = mutableListOf()
        return this
    }

    override fun add(x: Double, w: Int, base: Centroid) {
        add(x, w, base.data())
    }

    override fun add(x: Double, w: Int) {
        add(x, w, null as List<Double>?)
    }

    private fun add(x: Double, w: Int, history: List<Double>?) {
        var history = history
        if (java.lang.Double.isNaN(x)) {
            throw IllegalArgumentException("Cannot add NaN to t-digest")
        }
        if (tempUsed >= tempWeight.size - lastUsedCell - 1) {
            mergeNewValues()
        }
        val where = tempUsed++
        tempWeight[where] = w.toDouble()
        tempMean[where] = x
        unmergedWeight += w.toDouble()

        if (data != null) {
            if (tempData == null) {
                tempData = mutableListOf()
            }
            while (tempData!!.size <= where) {
                tempData!!.add(mutableListOf())
            }
            if (history == null) {
                history = listOf(x)
            }
            tempData!![where].addAll(history)
        }
    }

    private fun add(m: DoubleArray, w: DoubleArray, count: Int, data: MutableList<MutableList<Double>>?) {
        var m = m
        var w = w
        if (m.size != w.size) {
            throw IllegalArgumentException("Arrays not same length")
        }
        if (m.size < count + lastUsedCell) {
            // make room to add existing centroids
            val m1 = DoubleArray(count + lastUsedCell)
            System.arraycopy(m, 0, m1, 0, count)
            m = m1
            val w1 = DoubleArray(count + lastUsedCell)
            System.arraycopy(w, 0, w1, 0, count)
            w = w1
        }
        var total = 0.0
        for (i in 0 until count) {
            total += w[i]
        }
        merge(m, w, count, data, null, total, false)
    }

    override fun add(others: List<TDigest>) {
        if (others.size == 0) {
            return
        }
        var size = lastUsedCell
        for (other in others) {
            other.compress()
            size += other.centroidCount()
        }

        val m = DoubleArray(size)
        val w = DoubleArray(size)
        val data: MutableList<MutableList<Double>>?
        if (isRecording) {
            data = mutableListOf()
        } else {
            data = null
        }
        var offset = 0
        for (other in others) {
            if (other is MergingDigest) {
                System.arraycopy(other.mean, 0, m, offset, other.lastUsedCell)
                System.arraycopy(other.weight, 0, w, offset, other.lastUsedCell)
                if (data != null) {
                    for (centroid in other.centroids()) {
                        data.add(centroid.data()!!)
                    }
                }
                offset += other.lastUsedCell
            } else {
                for (centroid in other.centroids()) {
                    m[offset] = centroid.mean()
                    w[offset] = centroid.count().toDouble()
                    if (isRecording) {
                        assert(data != null)
                        data!!.add(centroid.data()!!)
                    }
                    offset++
                }
            }
        }
        add(m, w, size, data)
    }

    private fun mergeNewValues() {
        if (unmergedWeight > 0) {
            // note that we run the merge in reverse every other merge to avoid left-to-right bias in merging
            merge(
                tempMean,
                tempWeight,
                tempUsed,
                tempData,
                order,
                unmergedWeight,
                useAlternatingSort and (mergeCount % 2 == 1)
            )
            mergeCount++
            tempUsed = 0
            unmergedWeight = 0.0
            if (data != null) {
                tempData = mutableListOf()
            }

        }
    }

    private fun merge(
        incomingMean: DoubleArray,
        incomingWeight: DoubleArray,
        incomingCount: Int,
        incomingData: MutableList<MutableList<Double>>?,
        incomingOrder: IntArray?,
        unmergedWeight: Double,
        runBackwards: Boolean
    ) {
        var incomingCount = incomingCount
        var incomingOrder = incomingOrder
        System.arraycopy(mean, 0, incomingMean, incomingCount, lastUsedCell)
        System.arraycopy(weight, 0, incomingWeight, incomingCount, lastUsedCell)
        incomingCount += lastUsedCell

        if (incomingData != null) {
            for (i in 0 until lastUsedCell) {
                assert(data != null)
                incomingData.add(data!![i])
            }
            data = mutableListOf()
        }
        if (incomingOrder == null) {
            incomingOrder = IntArray(incomingCount)
        }
        Sort.sort(incomingOrder, incomingMean, incomingCount)
        // option to run backwards is to investigate bias in errors
        if (runBackwards) {
            Sort.reverse(incomingOrder, 0, incomingCount)
        }

        totalWeight += unmergedWeight
        var normalizer = compression / (2.0 * Math.PI * totalWeight)
        if (useConservativeLimit) {
            normalizer = normalizer / Math.log(totalWeight)
        }

        assert(incomingCount > 0)
        lastUsedCell = 0
        mean[lastUsedCell] = incomingMean[incomingOrder[0]]
        weight[lastUsedCell] = incomingWeight[incomingOrder[0]]
        var wSoFar = 0.0
        if (data != null) {
            assert(incomingData != null)
            data!!.add(incomingData!![incomingOrder[0]])
        }

        var k1 = 0.0

        // weight will contain all zeros
        var wLimit: Double
        wLimit = totalWeight * integratedQ(k1 + 1)
        for (i in 1 until incomingCount) {
            val ix = incomingOrder[i]
            val proposedWeight = weight[lastUsedCell] + incomingWeight[ix]
            val projectedW = wSoFar + proposedWeight
            val addThis: Boolean
            if (useWeightLimit) {
                val z = proposedWeight * normalizer
                val q0 = wSoFar / totalWeight
                val q2 = (wSoFar + proposedWeight) / totalWeight
                if (useConservativeLimit) {
                    addThis = z <= q0 * (1 - q0) && z <= q2 * (1 - q2)
                } else {
                    addThis = z * z <= q0 * (1 - q0) && z * z <= q2 * (1 - q2)
                }
            } else {
                addThis = projectedW <= wLimit
            }

            if (addThis) {
                // next point will fit
                // so merge into existing centroid
                weight[lastUsedCell] += incomingWeight[ix]
                mean[lastUsedCell] =
                    mean[lastUsedCell] + (incomingMean[ix] - mean[lastUsedCell]) * incomingWeight[ix] / weight[lastUsedCell]
                incomingWeight[ix] = 0.0

                if (data != null) {
                    while (data!!.size <= lastUsedCell) {
                        data!!.add(mutableListOf())
                    }
                    assert(incomingData != null)
                    assert(data!![lastUsedCell] !== incomingData!![ix])
                    data!![lastUsedCell].addAll(incomingData!![ix])
                }
            } else {
                // didn't fit ... move to next output, copy out first centroid
                wSoFar += weight[lastUsedCell]
                if (!useWeightLimit) {
                    k1 = integratedLocation(wSoFar / totalWeight, compression, totalWeight)
                    wLimit = totalWeight * integratedQ(k1 + 1)
                }

                lastUsedCell++
                mean[lastUsedCell] = incomingMean[ix]
                weight[lastUsedCell] = incomingWeight[ix]
                incomingWeight[ix] = 0.0

                if (data != null) {
                    assert(incomingData != null)
                    assert(data!!.size == lastUsedCell)
                    data!!.add(incomingData!![ix])
                }
            }
        }
        // points to next empty cell
        lastUsedCell++

        // sanity check
        var sum = 0.0
        for (i in 0 until lastUsedCell) {
            sum += weight[i]
        }
        assert(sum == totalWeight)
        if (runBackwards) {
            Sort.reverse(mean, 0, lastUsedCell)
            Sort.reverse(weight, 0, lastUsedCell)
            if (data != null) {
                data!!.reverse()
            }
        }

        if (totalWeight > 0) {
            min =Math.min(min, mean[0])
            max= Math.max(max, mean[lastUsedCell - 1])
        }
    }

    /**
     * Exposed for testing.
     */
    internal fun checkWeights(): Int {
        return checkWeights(weight, totalWeight, lastUsedCell)
    }

    private fun checkWeights(w: DoubleArray, total: Double, last: Int): Int {
        var badCount = 0

        var n = last
        if (w[n] > 0) {
            n++
        }

        var k1 = 0.0
        var q = 0.0
        var left = 0.0
        var header = "\n"
        for (i in 0 until n) {
            val dq = w[i] / total
            val k2 = integratedLocation(q + dq, compression, totalWeight)
            q += dq / 2
            if (k2 - k1 > 1 && w[i] != 1.0) {
                System.out.printf(
                    "%sOversize centroid at " + "%d, k0=%.2f, k1=%.2f, dk=%.2f, w=%.2f, q=%.4f, dq=%.4f, left=%.1f, current=%.2f maxw=%.2f\n",
                    header,
                    i,
                    k1,
                    k2,
                    k2 - k1,
                    w[i],
                    q,
                    dq,
                    left,
                    w[i],
                    Math.PI * total / compression * Math.sqrt(q * (1 - q))
                )
                header = ""
                badCount++
            }
            if (k2 - k1 > 4 && w[i] != 1.0) {
                throw IllegalStateException(
                    String.format(
                        "Egregiously oversized centroid at " + "%d, k0=%.2f, k1=%.2f, dk=%.2f, w=%.2f, q=%.4f, dq=%.4f, left=%.1f, current=%.2f, maxw=%.2f\n",
                        i,
                        k1,
                        k2,
                        k2 - k1,
                        w[i],
                        q,
                        dq,
                        left,
                        w[i],
                        Math.PI * total / compression * Math.sqrt(q * (1 - q))
                    )
                )
            }
            q += dq / 2
            left += w[i]
            k1 = k2
        }

        return badCount
    }

    /**
     * Converts a k-scale value into a quantile. This is the inverse of [.integratedLocation].
     * The virtue of using this is that sin is much cheaper than asin.
     *
     * @param k The k-index to be converted.
     * @return The value of q.
     */
    private fun integratedQ(k: Double): Double {
        return (Math.sin(Math.min(k, compression) * Math.PI / compression - Math.PI / 2) + 1) / 2
    }


    override fun compress() {
        mergeNewValues()
    }

    override fun size(): Long {
        return (totalWeight + unmergedWeight).toLong()
    }

    override fun cdf(x: Double): Double {
        mergeNewValues()

        if (lastUsedCell == 0) {
            // no data to examine
            return java.lang.Double.NaN
        } else if (lastUsedCell == 1) {
            // exactly one centroid, should have max==min
            val width = max - min
            return if (x < min) {
                0.0
            } else if (x > max) {
                1.0
            } else if (x - min <= width) {
                // min and max are too close together to do any viable interpolation
                0.5
            } else {
                // interpolate if somehow we have weight > 0 and max != min
                (x - min) / (max - min)
            }
        } else {
            val n = lastUsedCell
            if (x <= min) {
                return 0.0
            }

            if (x >= max) {
                return 1.0
            }

            // check for the left tail
            if (x <= mean[0]) {
                // note that this is different than mean[0] > min
                // ... this guarantees we divide by non-zero number and interpolation works
                return if (mean[0] - min > 0) {
                    (x - min) / (mean[0] - min) * weight[0] / totalWeight / 2.0
                } else {
                    0.0
                }
            }
            assert(x > mean[0])

            // and the right tail
            if (x >= mean[n - 1]) {
                return if (max - mean[n - 1] > 0) {
                    1 - (max - x) / (max - mean[n - 1]) * weight[n - 1] / totalWeight / 2.0
                } else {
                    1.0
                }
            }
            assert(x < mean[n - 1])

            // we know that there are at least two centroids and x > mean[0] && x < mean[n-1]
            // that means that there are either a bunch of consecutive centroids all equal at x
            // or there are consecutive centroids, c0 <= x and c1 > x
            var weightSoFar = weight[0] / 2
            var it = 0
            while (it < n) {
                if (mean[it] == x) {
                    val w0 = weightSoFar
                    while (it < n && mean[it + 1] == x) {
                        weightSoFar += weight[it] + weight[it + 1]
                        it++
                    }
                    return (w0 + weightSoFar) / 2.0 / totalWeight
                }
                if (mean[it] <= x && mean[it + 1] > x) {
                    if (mean[it + 1] - mean[it] > 0) {
                        val dw = (weight[it] + weight[it + 1]) / 2
                        return (weightSoFar + dw * (x - mean[it]) / (mean[it + 1] - mean[it])) / totalWeight
                    } else {
                        // this is simply caution against floating point madness
                        // it is conceivable that the centroids will be different
                        // but too near to allow safe interpolation
                        val dw = (weight[it] + weight[it + 1]) / 2
                        return weightSoFar + dw / totalWeight
                    }
                }
                weightSoFar += (weight[it] + weight[it + 1]) / 2
                it++
            }
            // it should not be possible for the loop fall through
            throw IllegalStateException("Can't happen ... loop fell through")
        }
    }

    override fun quantile(q: Double): Double {
        if (q < 0 || q > 1) {
            throw IllegalArgumentException("q should be in [0,1], got $q")
        }
        mergeNewValues()

        if (lastUsedCell == 0 && weight[lastUsedCell] == 0.0) {
            // no centroids means no data, no way to get a quantile
            return java.lang.Double.NaN
        } else if (lastUsedCell == 0) {
            // with one data point, all quantiles lead to Rome
            return mean[0]
        }

        // we know that there are at least two centroids now
        val n = lastUsedCell

        // if values were stored in a sorted array, index would be the offset we are interested in
        val index = q * totalWeight

        // at the boundaries, we return min or max
        if (index < weight[0] / 2) {
            assert(weight[0] > 0)
            return min + 2 * index / weight[0] * (mean[0] - min)
        }

        // in between we interpolate between centroids
        var weightSoFar = weight[0] / 2
        for (i in 0 until n - 1) {
            val dw = (weight[i] + weight[i + 1]) / 2
            if (weightSoFar + dw > index) {
                // centroids i and i+1 bracket our current point
                val z1 = index - weightSoFar
                val z2 = weightSoFar + dw - index
                return weightedAverage(mean[i], z2, mean[i + 1], z1)
            }
            weightSoFar += dw
        }
        assert(index <= totalWeight)
        assert(index >= totalWeight - weight[n - 1] / 2)

        // weightSoFar = totalWeight - weight[n-1]/2 (very nearly)
        // so we interpolate out to max value ever seen
        val z1 = index - totalWeight - weight[n - 1] / 2.0
        val z2 = weight[n - 1] / 2 - z1
        return weightedAverage(mean[n - 1], z1, max, z2)
    }

    override fun centroidCount(): Int {
        return lastUsedCell
    }

    override fun centroids(): Collection<Centroid> {
        // we don't actually keep centroid structures around so we have to fake it
        compress()
        return object : AbstractCollection<Centroid>() {
            override fun iterator(): MutableIterator<Centroid> {
                return object : MutableIterator<Centroid> {
                    var i = 0

                    override fun hasNext(): Boolean {
                        return i < lastUsedCell
                    }

                    override fun next(): Centroid {
                        val rc = Centroid(mean[i], weight[i].toInt(), if (data != null) data!![i] else null)
                        i++
                        return rc
                    }

                    override fun remove() {
                        throw UnsupportedOperationException("Default operation")
                    }
                }
            }

            override val size: Int get() {
                return lastUsedCell
            }
        }
    }

    override fun compression(): Double {
        return compression
    }

    override fun byteSize(): Int {
        compress()
        // format code, compression(float), buffer-size(int), temp-size(int), #centroids-1(int),
        // then two doubles per centroid
        return lastUsedCell * 16 + 32
    }

    override fun smallByteSize(): Int {
        compress()
        // format code(int), compression(float), buffer-size(short), temp-size(short), #centroids-1(short),
        // then two floats per centroid
        return lastUsedCell * 8 + 30
    }

    enum class Encoding constructor( val code: Int) {
        VERBOSE_ENCODING(1), SMALL_ENCODING(2)
    }

    override fun asBytes(buf: Output) {
        compress()
        buf.writeInt(Encoding.VERBOSE_ENCODING.code)
        buf.writeDouble(min)
        buf.writeDouble(max)
        buf.writeDouble(compression)
        buf.writeInt(lastUsedCell)
        for (i in 0 until lastUsedCell) {
            buf.writeDouble(weight[i])
            buf.writeDouble(mean[i])
        }
    }

    override fun asSmallBytes(buf: Output) {
        compress()
        buf.writeInt(Encoding.SMALL_ENCODING.code)    // 4
        buf.writeDouble(min)                          // + 8
        buf.writeDouble(max)                          // + 8
        buf.writeFloat(compression.toFloat())           // + 4
        buf.writeShort(mean.size.toShort())           // + 2
        buf.writeShort(tempMean.size.toShort())       // + 2
        buf.writeShort(lastUsedCell.toShort())          // + 2 = 30
        for (i in 0 until lastUsedCell) {
            buf.writeFloat(weight[i].toFloat())
            buf.writeFloat(mean[i].toFloat())
        }
    }

    companion object {

        // if true, alternate upward and downward merge passes
        var useAlternatingSort = false

        // if useWeightLimit is false, this makes asin faster
        internal var usePieceWiseApproximation = true

        // this forces centroid merging based on size limit rather than
        // based on accumulated k-index. This is much faster since we
        // never have to compute any sines or square roots
        var useWeightLimit = true

        // the conservative limit limits centroids to
        // \delta \log(N) (q/(1-q))
        // the non-conservative limit is
        // \delta \sqrt {q/(1-q)}
        // the conservative limit makes the extreme centroids smaller to
        // get more accuracy at the edges. Both options result in the
        // same number of centroids ... it is just that the mass is
        // distributed differently
        var useConservativeLimit = true

        /**
         * Converts a quantile into a centroid scale value.  The centroid scale is nominally
         * the number k of the centroid that a quantile point q should belong to.  Due to
         * round-offs, however, we can't align things perfectly without splitting points
         * and centroids.  We don't want to do that, so we have to allow for offsets.
         * In the end, the criterion is that any quantile range that spans a centroid
         * scale range more than one should be split across more than one centroid if
         * possible.  This won't be possible if the quantile range refers to a single point
         * or an already existing centroid.
         *
         *
         * This mapping is steep near q=0 or q=1 so each centroid there will correspond to
         * less q range.  Near q=0.5, the mapping is flatter so that centroids there will
         * represent a larger chunk of quantiles.
         *
         * @param q           The quantile scale value to be mapped.
         * @param compression
         * @param totalWeight
         * @return The centroid scale value corresponding to q.
         */
        fun integratedLocation(q: Double, compression: Double, totalWeight: Double): Double {
            return if (!useConservativeLimit) {
                compression * (asinApproximation(2 * q - 1) + Math.PI / 2) / Math.PI
            } else {
                // the k-scale for the conservative limit has infinite tails that we have to trim off
                if (q < 1 / totalWeight) {
                    0.0
                } else if (1 - q < 1 / totalWeight) {
                    1.0
                } else {
                    compression / 2 * (1 + Math.log(q / (1 - q)) / Math.log(totalWeight - 1))
                }
            }
        }

        internal fun asinApproximation(x: Double): Double {
            if (usePieceWiseApproximation) {
                if (x < 0) {
                    return -asinApproximation(-x)
                } else {
                    // this approximation works by breaking that range from 0 to 1 into 5 regions
                    // for all but the region nearest 1, rational polynomial models get us a very
                    // good approximation of asin and by interpolating as we move from region to
                    // region, we can guarantee continuity and we happen to get monotonicity as well.
                    // for the values near 1, we just use Math.asin as our region "approximation".

                    // cutoffs for models. Note that the ranges overlap. In the overlap we do
                    // linear interpolation to guarantee the overall result is "nice"
                    val c0High = 0.1
                    val c1High = 0.55
                    val c2Low = 0.5
                    val c2High = 0.8
                    val c3Low = 0.75
                    val c3High = 0.9
                    val c4Low = 0.87
                    if (x > c3High) {
                        return Math.asin(x)
                    } else {
                        // the models
                        val m0 = doubleArrayOf(
                            0.2955302411,
                            1.2221903614,
                            0.1488583743,
                            0.2422015816,
                            -0.3688700895,
                            0.0733398445
                        )
                        val m1 = doubleArrayOf(
                            -0.0430991920,
                            0.9594035750,
                            -0.0362312299,
                            0.1204623351,
                            0.0457029620,
                            -0.0026025285
                        )
                        val m2 = doubleArrayOf(
                            -0.034873933724,
                            1.054796752703,
                            -0.194127063385,
                            0.283963735636,
                            0.023800124916,
                            -0.000872727381
                        )
                        val m3 = doubleArrayOf(
                            -0.37588391875,
                            2.61991859025,
                            -2.48835406886,
                            1.48605387425,
                            0.00857627492,
                            -0.00015802871
                        )

                        // the parameters for all of the models
                        val vars = doubleArrayOf(1.0, x, x * x, x * x * x, 1 / (1 - x), 1.0 / (1 - x) / (1 - x))

                        // raw grist for interpolation coefficients
                        val x0 = bound((c0High - x) / c0High)
                        val x1 = bound((c1High - x) / (c1High - c2Low))
                        val x2 = bound((c2High - x) / (c2High - c3Low))
                        val x3 = bound((c3High - x) / (c3High - c4Low))

                        // interpolation coefficients

                        val mix1 = (1 - x0) * x1
                        val mix2 = (1 - x1) * x2
                        val mix3 = (1 - x2) * x3
                        val mix4 = 1 - x3

                        // now mix all the results together, avoiding extra evaluations
                        var r = 0.0
                        if (x0 > 0) {
                            r += x0 * eval(m0, vars)
                        }
                        if (mix1 > 0) {
                            r += mix1 * eval(m1, vars)
                        }
                        if (mix2 > 0) {
                            r += mix2 * eval(m2, vars)
                        }
                        if (mix3 > 0) {
                            r += mix3 * eval(m3, vars)
                        }
                        if (mix4 > 0) {
                            // model 4 is just the real deal
                            r += mix4 * Math.asin(x)
                        }
                        return r
                    }
                }
            } else {
                return Math.asin(x)
            }
        }

        private fun eval(model: DoubleArray, vars: DoubleArray): Double {
            var r = 0.0
            for (i in model.indices) {
                r += model[i] * vars[i]
            }
            return r
        }

        private fun bound(v: Double): Double {
            return if (v <= 0) {
                0.0
            } else if (v >= 1) {
                1.0
            } else {
                v
            }
        }

        fun fromBytes(buf: Input): MergingDigest {
            val encoding = buf.readInt()
            if (encoding == Encoding.VERBOSE_ENCODING.code) {
                val min = buf.readDouble()
                val max = buf.readDouble()
                val compression = buf.readDouble()
                val n = buf.readInt()
                val r = MergingDigest(compression)
                r.setMinMax(min, max)
                r.lastUsedCell = n
                for (i in 0 until n) {
                    r.weight[i] = buf.readDouble()
                    r.mean[i] = buf.readDouble()

                    r.totalWeight += r.weight[i]
                }
                return r
            } else if (encoding == Encoding.SMALL_ENCODING.code) {
                val min = buf.readDouble()
                val max = buf.readDouble()
                val compression = buf.readFloat().toDouble()
                val n = buf.readShort().toInt()
                val bufferSize = buf.readShort().toInt()
                val r = MergingDigest(compression, bufferSize, n)
                r.setMinMax(min, max)
                r.lastUsedCell = buf.readShort().toInt()
                for (i in 0 until r.lastUsedCell) {
                    r.weight[i] = buf.readFloat().toDouble()
                    r.mean[i] = buf.readFloat().toDouble()

                    r.totalWeight += r.weight[i]
                }
                return r
            } else {
                throw IllegalStateException("Invalid format for serialized histogram")
            }

        }
    }
}
