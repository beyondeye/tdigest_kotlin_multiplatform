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



import java.io.*
import java.nio.LongBuffer
import java.util.Arrays
import java.util.Random

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

abstract class HistogramTestCases {
    internal var useLinearBuckets: Boolean = false
    internal var factory: HistogramFactory? = null

    @Test
    fun testEmpty() {
        val x = factory!!.create(1.0, 100.0)
        val bins = x.bounds
        assertEquals(1.0, bins[0], 1e-5)
        assertTrue(x.lowerBound(bins.size) >= 100.0)
        assertTrue(bins.size >= 90)
    }

    @Throws(FileNotFoundException::class)
    internal fun doLinear(idealChi2: Double, chi2SD: Double, binCount: Int) {
        val n = 10000
        val trials = 1000
        // this should be about 160 by theory since that is where the cuts go above 1, but
        // there is some small residual issue that causes it to be a bit bigger.
        // these values are empirical against the current implementation. The fact that many counts are small may
        // be the reason for the slight deviation.

        val min = DoubleArray(binCount)
        val max = DoubleArray(binCount)

        // 165.4, 18, 212
        // 157, 18, 201


        Arrays.fill(min, 1.0)
        var n1above = 0
        var n2above = 0
        var n1below = 0
        var n2below = 0
        PrintWriter("data.csv").use { out ->
            out.print("j,cut,low,high,k,expected,err\n")
            var mean = 0.0
            var sd = 0.0
            for (j in 0 until trials) {
                val x = factory!!.create(1e-3, 10.0)
                val counts = x.counts
                val cuts = x.bounds
                assertTrue(min.size >= cuts.size)
                assertTrue(max.size >= cuts.size)

                val rand = Random()
                for (i in 0 until n) {
                    val u = rand.nextDouble()
                    x.add(u)
                    val k = x.bucket(u)
                    min[k] = Math.min(min[k], u)
                    max[k] = Math.max(max[k], u)
                }
                var sum = 0.0
                var maxErr = 0.0
                var i = 0
                while (i < cuts.size - 1 && cuts[i] < 1.1) {
                    var lowBound = Math.min(1.0, cuts[i])
                    if (i == 0) {
                        lowBound = 0.0
                    }
                    val highBound = Math.min(1.0, cuts[i + 1])
                    val expected = n * (highBound - lowBound)


                    var err = 0.0
                    if (counts[i] > 0) {
                        err = counts[i] * Math.log(counts[i] / expected)
                        sum += err

                        if (i > 0) {
                            assertTrue(String.format("%d: %.5f > %.5f", i, cuts[i], min[i]), cuts[i] <= min[i])
                        }
                        assertTrue(String.format("%d: %.5f > %.5f", i, max[i], cuts[i + 1]), cuts[i + 1] >= max[i])
                    }
                    out.printf(
                        "%d,%.4f,%.4f,%.4f,%d,%.4f,%.1f\n",
                        j,
                        cuts[i],
                        lowBound,
                        highBound,
                        counts[i],
                        expected,
                        err
                    )
                    maxErr = Math.max(maxErr, err)
                    i++
                }
                while (i < cuts.size) {
                    assertEquals(0, counts[i])
                    i++
                }
                sum = 2 * sum
                if (sum > idealChi2 + 3 * chi2SD) {
                    n2above++
                }
                if (sum > idealChi2 + 2 * chi2SD) {
                    n1above++
                }
                if (sum < idealChi2 - 3 * chi2SD) {
                    n2below++
                }
                if (sum < idealChi2 - 2 * chi2SD) {
                    n1below++
                }
                val old = mean
                mean += (sum - mean) / (j + 1)
                sd += (sum - mean) * (sum - old)
            }
            System.out.printf(
                "Checking χ^2 = %.4f ± %.1f against expected %.4f ± %.1f\n",
                mean, Math.sqrt(sd / trials), idealChi2, chi2SD
            )
            // verify that the chi^2 score for counts is as expected
            assertEquals("χ^2 > expect + 2*sd too often", 0.0, n1above.toDouble(), 0.05 * trials)
            // 3*sigma above for a chi^2 distribution happens more than you might think
            assertEquals("χ^2 > expect + 3*sd too often", 0.0, n2above.toDouble(), 0.01 * trials)
            // the bottom side of the chi^2 distribution is a bit tighter
            assertEquals("χ^2 < expect - 2*sd too often", 0.0, n1below.toDouble(), 0.03 * trials)
            assertEquals("χ^2 < expect - 3*sd too often", 0.0, n2below.toDouble(), 0.06 * trials)
        }
    }

    /**
     * The point of this test is to make sure that the floating point representation
     * can be used as a quick approximation of log_2
     *
     * @throws FileNotFoundException If we can't open an output file
     */
    @Test
    @Throws(FileNotFoundException::class)
    fun testFitToLog() {
        val scale = Math.pow(2.0, 52.0)
        var x = 0.001
        // 4 bits, worst case is mid octave
        val lowerBound = 1 / 16.0 * Math.sqrt(2.0)
        PrintWriter("log-fit.csv").use { out ->
            out.printf("x,y1,y2\n")
            while (x < 10) {
                val xz = java.lang.Double.doubleToLongBits(x)
                // the magic 0x3ff is the offset for the floating point exponent
                val v1 = xz / scale - 0x3ff
                val v2 = Math.log(x) / Math.log(2.0)
                out.printf("%.6f,%.6f,%.6f\n", x, v1, v2)
                assertTrue(v2 - v1 > 0)
                assertTrue(v2 - v1 < lowerBound)
                x *= 1.02
            }
        }
    }

    internal fun testBins(baseBinIndex: Int, bigBinIndex: Int, histogram: Histogram) {
        assertEquals(baseBinIndex.toLong(), histogram.bucket(10.01e-3).toLong())
        assertEquals(baseBinIndex.toLong(), histogram.bucket(10e-3).toLong())
        assertEquals(bigBinIndex.toLong(), histogram.bucket(2.235).toLong())
    }

    fun CompressionTestCore() {
        val n = 1000000
        val x = factory!!.create(1e-3, 10.0)

        val rand = Random()
        for (i in 0 until n) {
            x.add(rand.nextDouble())
        }
        val compressed = x.getCompressedCounts()
        assertTrue(compressed.size < 45)
        val uncompressed = LongArray(x.counts.size)
        val counts = x.counts

        val k = Simple64.decompress(LongBuffer.wrap(compressed), uncompressed)
        assertEquals(k.toLong(), counts.size.toLong())
        for (i in uncompressed.indices) {
            assertEquals(counts[i], uncompressed[i])
        }
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun SerializationTestCore() {
        val n = 1000000
        val x = factory!!.create(1e-3, 10.0)

        val rand = Random()
        for (i in 0 until n) {
            x.add(rand.nextDouble())
        }

        val out = ByteArrayOutputStream(1000)
        val xout = ObjectOutputStream(out)
        x.writeObject(xout)
        xout.close()

        val `in` = ByteArrayInputStream(out.toByteArray(), 0, out.size())
        val y = factory!!.create(0.1, 10.0)
        y.readObject(ObjectInputStream(`in`))

        assertArrayEquals(x.bounds, y.bounds, 1e-10)
        assertArrayEquals(x.counts, y.counts)
    }

    interface HistogramFactory {
        fun create(min: Double, max: Double): Histogram
    }
}