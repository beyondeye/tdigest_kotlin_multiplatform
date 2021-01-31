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

import org.junit.Test

import java.io.FileNotFoundException
import java.io.PrintWriter
import java.util.HashMap

import java.lang.Math.abs
import java.lang.Math.max
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.io.PrintStream




/**
 * Validate internal consistency of scale functions.
 */
class ScaleFunctionTests {
    @Test
    fun asinApproximation() {
        var x = 0.0
        while (x < 1) {
            assertEquals(Math.asin(x), ScaleFunction.fastAsin(x), 1e-6)
            x += 1e-4
        }
        assertEquals(Math.asin(1.0), ScaleFunction.fastAsin(1.0), 0.0)
        assertTrue(java.lang.Double.isNaN(ScaleFunction.fastAsin(1.0001)))
    }

    /**
     * Test that the basic single pass greedy t-digest construction has expected behavior with all scale functions.
     *
     *
     * This also throws off a diagnostic file that can be visualized if desired under the name of
     * scale-function-sizes.csv
     */
    @Test
    @Throws(FileNotFoundException::class)
    fun testSize() {
        PrintWriter("scale-function-sizes.csv").use { out ->
            out.printf("alg,compression,n,centroids,singletons,normalizer\n")
            for ( compression in doubleArrayOf(20.0, 50.0, 100.0, 200.0, 500.0, 1000.0, 2000.0)) {
            for (n in doubleArrayOf(10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1e3, 2e3, 5e3, 10e3, 20e3, 100e3, 1e6)) {
                    val clusterCount = HashMap<String, Int>()
                    for (k in ScaleFunction.values()) {
                        if (k.toString() == "K_0") {
                            continue
                        }
                        var k0 = k.k(0.0, compression, n)
                        var m = 0
                        var singles = 0
                        var i = 0
                        while (i < n) {
                            var cnt = 1.0
                            while (i + cnt < n && k.k((i + cnt + 1) / (n - 1), compression, n) - k0 < 1) {
                                cnt++
                            }
                            if (cnt == 1.0) {
                                singles++
                            }
                            val size =
                                n * max(k.max(i / (n - 1), compression, n), k.max((i + cnt) / (n - 1), compression, n))

                            // check that we didn't cross the midline (which makes the size limit very conservative)
                            val left = i - (n - 1) / 2
                            val right = i + cnt - (n - 1) / 2
                            val sameSide = left * right > 0
                            if (!k.toString().endsWith("NO_NORM") && sameSide) {
                                assertTrue(
                                    String.format(
                                        "%s %.0f %.0f %.3f vs %.3f @ %.3f",
                                        k,
                                        compression,
                                        n,
                                        cnt,
                                        size,
                                        i / (n - 1)
                                    ),
                                    cnt == 1.0 || cnt <= max(1.1 * size, size + 1)
                                )
                            }
                            i += cnt.toInt()
                            k0 = k.k(i / (n - 1), compression, n)
                            m++
                        }
                        clusterCount[k.toString()] = m
                        out.printf("%s,%.0f,%.0f,%d,%d,%.4f\n", k, compression, n, m, singles, k.normalizer(compression, n))

                        if (!k.toString().endsWith("NO_NORM")) {
                            assertTrue(
                                String.format("%s %d, %.0f", k, m, compression),
                                n < 3 * compression || m >= compression / 3 && m <= compression
                            )
                        }
                    }
                    // make sure that the approximate version gets same results
                    assertEquals(clusterCount["K_1"], clusterCount["K_1_FAST"])
                }
            }
        }
    }

    /**
     * Validates the bounds on the shape of the different scale functions. The basic idea is
     * that diff difference between minimum and maximum values of k in the region where we
     * can have centroids with >1 sample should be small enough to meet the size limit of
     * the digest, but not small enough to degrade accuracy.
     */
    @Test
    @Throws(FileNotFoundException::class)
    fun testK() {
        val out = PrintStream("scale-function.csv")
        for (k in ScaleFunction.values()) {
            if (k.name.contains("NO_NORM")) {
                continue
            }
            if (k.name.contains("K_0")) {
                continue
            }
            for (compression in doubleArrayOf(50.0, 100.0, 200.0, 500.0, 1000.0)) {
                for (n in intArrayOf(10, 100, 1000, 10000, 100000, 1000000, 10000000)) {
                    // first confirm that the shortcut (with norm) and the full version agree
                    val norm = k.normalizer(compression, n.toDouble())
                    for (q in doubleArrayOf(0.0001, 0.001, 0.01, 0.1, 0.2, 0.5)) {
                        if (q * n > 1) {
                            assertEquals(
                                String.format("%s q: %.4f, compression: %.0f, n: %d", k, q, compression, n),
                                k.k(q, compression, n.toDouble()), k.k(q, norm), 1e-10
                            )
                            assertEquals(
                                String.format("%s q: %.4f, compression: %.0f, n: %d", k, q, compression, n),
                                k.k(1 - q, compression, n.toDouble()), k.k(1 - q, norm), 1e-10
                            )
                        }
                    }

                    // now estimate the number of centroids
                    var mink = Double.POSITIVE_INFINITY
                    var maxk = Double.NEGATIVE_INFINITY
                    var singletons = 0.0
                    while (singletons < n / 2.0) {
                        // could we group more than one sample?
                        val diff2 = k.k((singletons + 2.0) / n, norm) - k.k(singletons / n, norm)
                        if (diff2 < 1) {
                            // yes!
                            val q = singletons / n
                            mink = Math.min(mink, k.k(q, norm))
                            maxk = max(maxk, k.k(1 - q, norm))
                            break
                        }
                        singletons++
                    }
                    // did we consume all the data with singletons?
                    if (java.lang.Double.isInfinite(mink) || java.lang.Double.isInfinite(maxk)) {
                        // just make sure of this
                        assertEquals(n.toDouble(), 2 * singletons, 0.0)
                        mink = 0.0
                        maxk = 0.0
                    }
                    // estimate number of clusters. The real number would be a bit more than this
                    val diff = maxk - mink + 2 * singletons

                    // mustn't have too many
                    var label =
                        String.format("max diff: %.3f, scale: %s, compression: %.0f, n: %d", diff, k, compression, n)
                    assertTrue(label, diff <= Math.min(n.toDouble(), compression / 2 + 10))

                    // nor too few. This is where issue #151 shows up
                    label =
                        String.format("min diff: %.3f, scale: %s, compression: %.0f, n: %d", diff, k, compression, n)
                    assertTrue(label, diff >= Math.min(n.toDouble(), compression / 4))
                    out.printf(
                        "%s, %.0f, %d, %.0f, %.3f, %.3f\n",
                        k, compression, n, singletons, mink, maxk
                    )
                }
            }
        }
    }

    @Test
    fun testNonDecreasing() {
        for (scale in ScaleFunction.values()) {
            for (compression in doubleArrayOf(20.0, 50.0, 100.0, 200.0, 500.0, 1000.0)) {
                for (n in intArrayOf(10, 100, 1000, 10000, 100000, 1000000, 10000000)) {
                    val norm = scale.normalizer(compression, n.toDouble())
                    var last = Double.NEGATIVE_INFINITY
                    var q = -1.0
                    while (q < 2) {
                        val k1 = scale.k(q, norm)
                        val k2 = scale.k(q, compression, n.toDouble())
                        val remark = String.format(
                            "Different ways to compute scale function %s should agree, " +
                                    "compression=%.0f, n=%d, q=%.2f",
                            scale, compression, n, q
                        )
                        assertEquals(remark, k1, k2, 1e-10)
                        assertTrue(
                            String.format("Scale %s function should not decrease", scale),
                            k1 >= last
                        )
                        last = k1
                        q += 0.01
                    }
                    last = Double.NEGATIVE_INFINITY
                    var k = scale.q(0.0, norm) - 2
                    while (k < scale.q(1.0, norm) + 2) {
                        val q1 = scale.q(k, norm)
                        val q2 = scale.q(k, compression, n.toDouble())
                        val remark = String.format(
                            "Different ways to compute inverse scale function %s should agree, " +
                                    "compression=%.0f, n=%d, q=%.2f",
                            scale, compression, n, k
                        )
                        assertEquals(remark, q1, q2, 1e-10)
                        assertTrue(
                            String.format("Inverse scale %s function should not decrease", scale),
                            q1 >= last
                        )
                        last = q1
                        k += 0.01
                    }
                }
            }
        }
    }
    /**
     * Validates the fast asin approximation
     */
    @Test
    fun testApproximation() {
        var worst = 0.0
        var old = java.lang.Double.NEGATIVE_INFINITY
        var x = -1.0
        while (x < 1) {
            val ex = Math.asin(x)
            val actual = ScaleFunction.fastAsin(x)
            val error = ex - actual
            //            System.out.printf("%.8f, %.8f, %.8f, %.12f\n", x, ex, actual, error * 1e6);
            assertEquals("Bad approximation", 0.0, error, 1e-6)
            assertTrue("Not monotonic", actual >= old)
            worst = Math.max(worst, Math.abs(error))
            old = actual
            x += 0.00001
        }
        assertEquals(Math.asin(1.0), ScaleFunction.fastAsin(1.0), 0.0)
        System.out.printf("worst = %.5g\n", worst)
    }

    /**
     * Validates that the forward and reverse scale functions are as accurate as intended.
     */
    @Test
    fun testInverseScale() {
        for (f in ScaleFunction.values()) {
            val tolerance = if (f.toString().contains("FAST")) 2e-4 else 1e-10
            System.out.printf("F = %s\n", f)

            for (n in doubleArrayOf(1000.0, 3000.0, 10000.0, 100000.0)) {
                val epsilon = 1.0 / n
                for (compression in doubleArrayOf(20.0, 100.0, 1000.0)) {
                    var oldK = f.k(0.0, compression, n)
                    var i = 1
                    while (i < n) {
                        val q = i / n
                        val k = f.k(q, compression, n)
                        assertTrue(
                            String.format("monoticity %s(%.0f, %.0f) @ %.5f", f, compression, n, q),
                            k > oldK
                        )
                        oldK = k

                        val qx = f.q(k, compression, n)
                        val kx = f.k(qx, compression, n)
                        assertEquals(String.format("Q: %s(%.0f, %.0f) @ %.5f", f, compression, n, q), q, qx, 1e-6)
                        val absError = abs(k - kx)
                        val relError = absError / max(0.01, max(abs(k), abs(kx)))
                        val info = java.lang.String.format(
                            "K: %s(%.0f, %.0f) @ %.5f [%.5g, %.5g]",
                            f, compression, n, q, absError, relError
                        )
                        assertEquals(info, 0.0, absError, tolerance)
                        assertEquals(info, 0.0, relError, tolerance)
                        i++
                    }
                    assertTrue(f.k(0.0, compression, n) < f.k(epsilon, compression, n))
                    assertTrue(f.k(1.0, compression, n) > f.k(1 - epsilon, compression, n))
                }
            }
        }
    }
}
