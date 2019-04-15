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
            out.printf("alg,delta,n,m,singles\n")
            for (compression in doubleArrayOf(20.0, 50.0, 100.0, 200.0, 500.0)) {
                for (n in doubleArrayOf(100.0, 200.0, 500.0, 1e3, 5e3, 10e3, 100e3, 1e6)) {
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
                            while (i + cnt < n && k.k((i + cnt) / (n - 1), compression, n) - k0 < 1) {
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
                        out.printf("%s,%.0f,%.0f,%d,%d\n", k, compression, n, m, singles)

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
                        assertEquals(
                            String.format(
                                "K: %s(%.0f, %.0f) @ %.5f [%.5g, %.5g]",
                                f, compression, n, q, absError, relError
                            ),
                            0.0, absError, tolerance
                        )
                        assertEquals(
                            String.format(
                                "K: %s(%.0f, %.0f) @ %.5f [%.5g, %.5g]",
                                f, compression, n, q, absError, relError
                            ),
                            0.0, relError, tolerance
                        )
                        i++
                    }
                    assertTrue(f.k(0.0, compression, n) < f.k(epsilon, compression, n))
                    assertTrue(f.k(1.0, compression, n) > f.k(1 - epsilon, compression, n))
                }
            }
        }
    }
}
