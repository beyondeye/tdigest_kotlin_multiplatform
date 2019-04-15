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
@JvmOverloads constructor(compression: Double, bufferSize: Int = -1, size: Int = -1) : AbstractTDigest() {
    private var mergeCount = 0

    private val publicCompression: Double
    private val compression: Double

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

    // if true, alternate upward and downward merge passes
    var useAlternatingSort = true
    // if true, use higher working value of compression during construction, then reduce on presentation
    var useTwoLevelCompression = true

    val scaleFunction: ScaleFunction
        get() = scale

    init {
        var compression = compression
        var bufferSize = bufferSize
        var size = size
        // ensure compression >= 10
        // default size = 2 * ceil(compression)
        // default bufferSize = 5 * size
        // scale = max(2, bufferSize / size - 1)
        // compression, publicCompression = sqrt(scale-1)*compression, compression
        // ensure size > 2 * compression + weightLimitFudge
        // ensure bufferSize > 2*size

        // force reasonable value. Anything less than 10 doesn't make much sense because
        // too few centroids are retained
        if (compression < 10) {
            compression = 10.0
        }

        // the weight limit is too conservative about sizes and can require a bit of extra room
        var sizeFudge = 0.0
        if (useWeightLimit) {
            sizeFudge = 10.0
            if (compression < 30) sizeFudge += 20.0
        }

        // default size
        size = kotlin.math.max(2 * compression + sizeFudge, size.toDouble()).toInt()

        // default buffer
        if (bufferSize == -1) {
            // TODO update with current numbers
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
            bufferSize = 5 * size
        }

        // ensure enough space in buffer
        if (bufferSize <= 2 * size) {
            bufferSize = 2 * size
        }

        // scale is the ratio of extra buffer to the final size
        // we have to account for the fact that we copy all live centroids into the incoming space
        var scale = kotlin.math.max(1, bufferSize / size - 1).toDouble()
        if (!useTwoLevelCompression) {
            scale = 1.0
        }

        // publicCompression is how many centroids the user asked for
        // compression is how many we actually keep
        this.publicCompression = compression
        this.compression = kotlin.math.sqrt(scale) * publicCompression

        // changing the compression could cause buffers to be too small, readjust if so
        if (size < this.compression + sizeFudge) {
            size = kotlin.math.ceil(this.compression + sizeFudge).toInt()
        }

        // ensure enough space in buffer (possibly again)
        if (bufferSize <= 2 * size) {
            bufferSize = 2 * size
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
        if (x < min) {
            min = x
        }
        if (x > max) {
            max = x
        }

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
        merge(m, w, count, data, null, total, false, compression)
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
    private fun mergeNewValues(force: Boolean = false, compression: Double = this.compression) {
        if (totalWeight == 0.0 && unmergedWeight == 0.0) {
            // seriously nothing to do
            return
        }
        if (force || unmergedWeight > 0) {
            // note that we run the merge in reverse every other merge to avoid left-to-right bias in merging
            merge(
                tempMean, tempWeight, tempUsed, tempData, order, unmergedWeight,
                useAlternatingSort and (mergeCount % 2 == 1), compression
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
        incomingMean: DoubleArray, incomingWeight: DoubleArray, incomingCount: Int,
        incomingData: MutableList<MutableList<Double>>?, incomingOrder: IntArray?,
        unmergedWeight: Double, runBackwards: Boolean, compression: Double
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

        assert(lastUsedCell + incomingCount > 0)
        lastUsedCell = 0
        mean[lastUsedCell] = incomingMean[incomingOrder[0]]
        weight[lastUsedCell] = incomingWeight[incomingOrder[0]]
        var wSoFar = 0.0
        if (data != null) {
            assert(incomingData != null)
            data!!.add(incomingData!![incomingOrder[0]])
        }


        // weight will contain all zeros after this loop

        val normalizer = scale.normalizer(compression, totalWeight)
        var k1 = scale.k(0.0, normalizer)
        var wLimit = totalWeight * scale.q(k1 + 1, normalizer)
        for (i in 1 until incomingCount) {
            val ix = incomingOrder[i]
            val proposedWeight = weight[lastUsedCell] + incomingWeight[ix]
            val projectedW = wSoFar + proposedWeight
            val addThis: Boolean
            if (useWeightLimit) {
                val q0 = wSoFar / totalWeight
                val q2 = (wSoFar + proposedWeight) / totalWeight
                addThis = proposedWeight <= totalWeight * kotlin.math.min(scale.max(q0, normalizer), scale.max(q2, normalizer))
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
                    k1 = scale.k(wSoFar / totalWeight, normalizer)
                    wLimit = totalWeight * scale.q(k1 + 1, normalizer)
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
            min = kotlin.math.min(min, mean[0])
            max = kotlin.math.max(max, mean[lastUsedCell - 1])
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

        val normalizer = scale.normalizer(publicCompression, totalWeight)
        var k1 = scale.k(0.0, normalizer)
        var q = 0.0
        var left = 0.0
        var header = "\n"
        for (i in 0 until n) {
            val dq = w[i] / total
            val k2 = scale.k(q + dq, normalizer)
            q += dq / 2
            if (k2 - k1 > 1 && w[i] != 1.0) {
                System.out.printf(
                    "%sOversize centroid at " + "%d, k0=%.2f, k1=%.2f, dk=%.2f, w=%.2f, q=%.4f, dq=%.4f, left=%.1f, current=%.2f maxw=%.2f\n",
                    header, i, k1, k2, k2 - k1, w[i], q, dq, left, w[i], totalWeight * scale.max(q, normalizer)
                )
                header = ""
                badCount++
            }
            if (k2 - k1 > 4 && w[i] != 1.0) {
                throw IllegalStateException(
                    String.format(
                        "Egregiously oversized centroid at " + "%d, k0=%.2f, k1=%.2f, dk=%.2f, w=%.2f, q=%.4f, dq=%.4f, left=%.1f, current=%.2f, maxw=%.2f\n",
                        i, k1, k2, k2 - k1, w[i], q, dq, left, w[i], totalWeight * scale.max(q, normalizer)
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
     * Merges any pending inputs and compresses the data down to the public setting.
     * Note that this typically loses a bit of precision and thus isn't a thing to
     * be doing all the time. It is best done only when we want to show results to
     * the outside world.
     */
    override fun compress() {
        mergeNewValues(true, publicCompression)
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
            if (x < min) {
                return 0.0
            }

            if (x > max) {
                return 1.0
            }

            // check for the left tail
            if (x < mean[0]) {
                // note that this is different than mean[0] > min
                // ... this guarantees we divide by non-zero number and interpolation works
                return if (mean[0] - min > 0) {
                    // must be a sample exactly at min
                    if (x == min) {
                        0.5 / totalWeight
                    } else {
                        (1 + (x - min) / (mean[0] - min) * (weight[0] / 2 - 1)) / totalWeight
                    }
                } else {
                    // this should be redundant with the check x < min
                    0.0
                }
            }
            assert(x >= mean[0])

            // and the right tail
            if (x > mean[n - 1]) {
                if (max - mean[n - 1] > 0) {
                    if (x == max) {
                        return 1 - 0.5 / totalWeight
                    } else {
                        // there has to be a single sample exactly at max
                        val dq = (1 + (max - x) / (max - mean[n - 1]) * (weight[n - 1] / 2 - 1)) / totalWeight
                        return 1 - dq
                    }
                } else {
                    return 1.0
                }
            }

            // we know that there are at least two centroids and mean[0] < x < mean[n-1]
            // that means that there are either one or more consecutive centroids all at exactly x
            // or there are consecutive centroids, c0 < x < c1
            var weightSoFar = 0.0
            var it = 0
            while (it < n - 1) {
                // weightSoFar does not include weight[it] yet
                if (mean[it] == x) {
                    // we have one or more centroids == x, treat them as one
                    // dw will accumulate the weight of all of the centroids at x
                    var dw = 0.0
                    while (it < n && mean[it] == x) {
                        dw += weight[it]
                        it++
                    }
                    return (weightSoFar + dw / 2) / totalWeight
                } else if (mean[it] <= x && x < mean[it + 1]) {
                    // landed between centroids ... check for floating point madness
                    if (mean[it + 1] - mean[it] > 0) {
                        // note how we handle singleton centroids here
                        // the point is that for singleton centroids, we know that their entire
                        // weight is exactly at the centroid and thus shouldn't be involved in
                        // interpolation
                        var leftExcludedW = 0.0
                        var rightExcludedW = 0.0
                        if (weight[it] == 1.0) {
                            if (weight[it + 1] == 1.0) {
                                // two singletons means no interpolation
                                // left singleton is in, right is out
                                return (weightSoFar + 1) / totalWeight
                            } else {
                                leftExcludedW = 0.5
                            }
                        } else if (weight[it + 1] == 1.0) {
                            rightExcludedW = 0.5
                        }
                        val dw = (weight[it] + weight[it + 1]) / 2

                        // can't have double singleton (handled that earlier)
                        assert(dw > 1)
                        assert(leftExcludedW + rightExcludedW <= 0.5)

                        // adjust endpoints for any singleton
                        val left = mean[it]
                        val right = mean[it + 1]

                        val dwNoSingleton = dw - leftExcludedW - rightExcludedW

                        // adjustments have only limited effect on endpoints
                        assert(dwNoSingleton > dw / 2)
                        assert(right - left > 0)
                        val base = weightSoFar + weight[it] / 2 + leftExcludedW
                        return (base + dwNoSingleton * (x - left) / (right - left)) / totalWeight
                    } else {
                        // this is simply caution against floating point madness
                        // it is conceivable that the centroids will be different
                        // but too near to allow safe interpolation
                        val dw = (weight[it] + weight[it + 1]) / 2
                        return (weightSoFar + dw) / totalWeight
                    }
                } else {
                    weightSoFar += weight[it]
                }
                it++
            }
            return if (x == mean[n - 1]) {
                1 - 0.5 / totalWeight
            } else {
                throw IllegalStateException("Can't happen ... loop fell through")
            }
        }
    }

    override fun quantile(q: Double): Double {
        if (q < 0 || q > 1) {
            throw IllegalArgumentException("q should be in [0,1], got $q")
        }
        mergeNewValues()

        if (lastUsedCell == 0) {
            // no centroids means no data, no way to get a quantile
            return java.lang.Double.NaN
        } else if (lastUsedCell == 1) {
            // with one data point, all quantiles lead to Rome
            return mean[0]
        }

        // we know that there are at least two centroids now
        val n = lastUsedCell

        // if values were stored in a sorted array, index would be the offset we are interested in
        val index = q * totalWeight

        // beyond the boundaries, we return min or max
        // usually, the first centroid will have unit weight so this will make it moot
        if (index < 1) {
            return min
        }

        // if the left centroid has more than one sample, we still know
        // that one sample occurred at min so we can do some interpolation
        if (weight[0] > 1 && index < weight[0] / 2) {
            // there is a single sample at min so we interpolate with less weight
            return min + (index - 1) / (weight[0] / 2 - 1) * (mean[0] - min)
        }

        // usually the last centroid will have unit weight so this test will make it moot
        if (index > totalWeight - 1) {
            return max
        }

        // if the right-most centroid has more than one sample, we still know
        // that one sample occurred at max so we can do some interpolation
        if (weight[n - 1] > 1 && totalWeight - index <= weight[n - 1] / 2) {
            return max - (totalWeight - index - 1.0) / (weight[n - 1] / 2 - 1) * (max - mean[n - 1])
        }

        // in between extremes we interpolate between centroids
        var weightSoFar = weight[0] / 2
        for (i in 0 until n - 1) {
            val dw = (weight[i] + weight[i + 1]) / 2
            if (weightSoFar + dw > index) {
                // centroids i and i+1 bracket our current point

                // check for unit weight
                var leftUnit = 0.0
                if (weight[i] == 1.0) {
                    if (index - weightSoFar < 0.5) {
                        // within the singleton's sphere
                        return mean[i]
                    } else {
                        leftUnit = 0.5
                    }
                }
                var rightUnit = 0.0
                if (weight[i + 1] == 1.0) {
                    if (weightSoFar + dw - index <= 0.5) {
                        // no interpolation needed near singleton
                        return mean[i + 1]
                    }
                    rightUnit = 0.5
                }
                val z1 = index - weightSoFar - leftUnit
                val z2 = weightSoFar + dw - index - rightUnit
                return weightedAverage(mean[i], z2, mean[i + 1], z1)
            }
            weightSoFar += dw
        }
        // we handled singleton at end up above
        assert(weight[n - 1] > 1)
        assert(index <= totalWeight)
        assert(index >= totalWeight - weight[n - 1] / 2)

        // weightSoFar = totalWeight - weight[n-1]/2 (very nearly)
        // so we interpolate out to max value ever seen
        val z1 = index - totalWeight - weight[n - 1] / 2.0
        val z2 = weight[n - 1] / 2 - z1
        return weightedAverage(mean[n - 1], z1, max, z2)
    }

    override fun centroidCount(): Int {
        mergeNewValues()
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
        return publicCompression
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
        buf.writeDouble(publicCompression)
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
        buf.writeFloat(publicCompression.toFloat())           // + 4
        buf.writeShort(mean.size.toShort())           // + 2
        buf.writeShort(tempMean.size.toShort())       // + 2
        buf.writeShort(lastUsedCell.toShort())          // + 2 = 30
        for (i in 0 until lastUsedCell) {
            buf.writeFloat(weight[i].toFloat())
            buf.writeFloat(mean[i].toFloat())
        }
    }

    override fun toString(): String {
        return ("MergingDigest"
                + "-" + scaleFunction
                + "-" + (if (useWeightLimit) "weight" else "kSize")
                + "-" + (if (useAlternatingSort) "alternating" else "stable")
                + "-" + if (useTwoLevelCompression) "twoLevel" else "oneLevel")
    }

    companion object {

        // this forces centroid merging based on size limit rather than
        // based on accumulated k-index. This can be much faster since we
        // scale functions are more expensive than the corresponding
        // weight limits.
        var useWeightLimit = true
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
