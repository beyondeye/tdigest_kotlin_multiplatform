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
 * Static class with methods for comparing distributions.
 */
object Comparison {

    /**
     * Use a log-likelihood ratio test to compare two distributions.
     * This is done by estimating counts in quantile ranges from each
     * distribution and then comparing those counts using a multinomial
     * test. The result should be asymptotically chi^2 distributed if
     * the data comes from the same distribution, but this isn't so
     * much useful as a traditional test of a null hypothesis as it is
     * just a reasonably well-behaved score that is bigger when the
     * distributions are more different, subject to having enough data
     * to tell.
     *
     * @param dist1 First distribution (usually the reference)
     * @param dist2 Second distribution to compare (usually the test case)
     * @param qCuts The quantiles that define the bin boundaries. Values 0 or 1
     * may result in zero counts. Note that the actual cuts are
     * defined loosely as <pre>dist1.quantile(qCuts[i])</pre>.
     * @return A score that is big when dist1 and dist2 are discernibly different.
     * A small score does not mean similarity. Instead, it could just mean insufficient
     * data.
     */
    fun compareChi2(dist1: TDigest, dist2: TDigest, qCuts: DoubleArray): Double {
        val count = arrayOfNulls<DoubleArray>(2)
        count[0] = DoubleArray(qCuts.size + 1)
        count[1] = DoubleArray(qCuts.size + 1)

        var oldQ = 0.0
        var oldQ2 = 0.0
        for (i in 0..qCuts.size) {
            val newQ: Double
            val x: Double
            if (i == qCuts.size) {
                newQ = 1.0
                x = kotlin.math.max(dist1.max, dist2.max) + 1
            } else {
                newQ = qCuts[i]
                x = dist1.quantile(newQ)
            }
            count[0]!![i] = dist1.size() * (newQ - oldQ)

            val q2 = dist2.cdf(x)
            count[1]!![i] = dist2.size() * (q2 - oldQ2)
            oldQ = newQ
            oldQ2 = q2
        }

        //*DARIO* cast to remove nullability
        return llr(count as Array<DoubleArray>)
    }

    /**
     * Use a log-likelihood ratio test to compare two distributions.
     * With non-linear histograms that have compatible bin boundaries,
     * all that we have to do is compare two count vectors using a
     * chi^2 test (actually a log-likelihood ratio version called a G-test).
     *
     * @param dist1 First distribution (usually the reference)
     * @param dist2 Second distribution to compare (usually the test case)
     * @return A score that is big when dist1 and dist2 are discernibly different.
     * A small score does not mean similarity. Instead, it could just mean insufficient
     * data.
     */
    fun compareChi2(dist1: Histogram, dist2: Histogram): Double {
        if (dist1.javaClass != dist2.javaClass) {
            throw IllegalArgumentException(
                String.format(
                    "Must have same class arguments, got %s and %s",
                    dist1.javaClass, dist2.javaClass
                )
            )
        }

        val k1 = dist1.counts
        val k2 = dist2.counts

        val n1 = k1.size
        if (n1 != k2.size ||
            dist1.lowerBound(0) != dist2.lowerBound(0) ||
            dist1.lowerBound(n1 - 1) != dist2.lowerBound(n1 - 1)
        ) {
            throw IllegalArgumentException("Incompatible histograms in terms of size or bounds")
        }

        val count = Array(2) { DoubleArray(n1) }
        for (i in 0 until n1) {
            count[0][i] = k1[i].toDouble()
            count[1][i] = k2[i].toDouble()
        }
        return llr(count)
    }

    fun llr(count: Array<DoubleArray>): Double {
        if (count.size == 0) {
            throw IllegalArgumentException("Must have some data in llr")
        }
        val columns = count[0].size
        val rows = count.size
        val rowSums = DoubleArray(rows)
        val colSums = DoubleArray(columns)

        var totalCount = 0.0
        var h = 0.0 // accumulator for entropy

        for (i in 0 until rows) {
            for (j in 0 until columns) {
                val k = count[i][j]
                rowSums[i] += k
                colSums[j] += k
                if (k < 0) {
                    throw IllegalArgumentException(String.format("Illegal negative count (%.5f) at %d,%d", k, i, j))
                }
                if (k > 0) {
                    h += k * kotlin.math.ln(k)
                    totalCount += k
                }
            }
        }

        val normalizer = totalCount * kotlin.math.ln(totalCount)
        h -= normalizer  // same as dividing every count by total inside the log

        var hr = 0.0 // accumulator for row-wise entropy
        for (i in 0 until rows) {
            if (rowSums[i] > 0) {
                hr += rowSums[i] * kotlin.math.ln(rowSums[i])
            }
        }
        hr -= normalizer

        var hc = 0.0 // accumulator for column-wise entropy
        for (j in 0 until columns) {
            if (colSums[j] > 0) {
                hc += colSums[j] * kotlin.math.ln(colSums[j])
            }
        }
        hc -= normalizer
        // return value is 2N * mutualInformation(count)
        return 2 * (h - hr - hc)
    }

    /**
     * Returns the observed value of the Kolmogorov-Smirnov statistic normalized by sample counts so
     * that the score should be roughly distributed as sqrt(-log(u)/2). This is equal to the normal
     * KS statistic multiplied by sqrt(m*n/(m+n)) where m and n are the number of samples in d1 and
     * d2 respectively.
     * @param d1  A digest of the first set of samples.
     * @param d2  A digest of the second set of samples.
     * @return A statistic which is bigger when d1 and d2 seem to represent different distributions.
     */
    fun ks(d1: TDigest, d2: TDigest): Double {
        val ix1 = d1.centroids().iterator()
        val ix2 = d2.centroids().iterator()

        var diff = 0.0

        var x1 = d1.min
        var x2 = d2.min

        while (x1 <= d1.max && x2 <= d2.max) {
            if (x1 < x2) {
                diff = maxDiff(d1, d2, diff, x1)
                x1 = nextValue(d1, ix1, x1)
            } else if (x1 > x2) {
                diff = maxDiff(d1, d2, diff, x2)
                x2 = nextValue(d2, ix2, x2)
            } else if (x1 == x2) {
                diff = maxDiff(d1, d2, diff, x1)

                val q1 = d1.cdf(x1)
                val q2 = d2.cdf(x2)
                if (q1 < q2) {
                    x1 = nextValue(d1, ix1, x1)
                } else if (q1 > q2) {
                    x2 = nextValue(d2, ix2, x2)
                } else {
                    x1 = nextValue(d1, ix1, x1)
                    x2 = nextValue(d2, ix2, x2)
                }
            }
        }
        while (x1 <= d1.max) {
            diff = maxDiff(d1, d2, diff, x1)
            x1 = nextValue(d1, ix1, x1)
        }

        while (x2 <= d2.max) {
            diff = maxDiff(d2, d2, diff, x2)
            x2 = nextValue(d2, ix2, x2)
        }

        val n1 = d1.size()
        val n2 = d2.size()
        return diff * kotlin.math.sqrt(n1.toDouble() * n2 / (n1 + n2))
    }

    private fun maxDiff(d1: TDigest, d2: TDigest, diff: Double, x1: Double): Double {
        var diff = diff
        diff = kotlin.math.max(diff, kotlin.math.abs(d1.cdf(x1) - d2.cdf(x1)))
        return diff
    }

    private fun nextValue(d: TDigest, ix: Iterator<Centroid>, x: Double): Double {
        return if (ix.hasNext()) {
            ix.next().mean()
        } else if (x < d.max) {
            d.max
        } else {
            d.max + 1
        }
    }
}
