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

import org.junit.Assert.assertEquals
import org.junit.Test

import java.io.FileNotFoundException
import java.io.PrintStream
import java.io.PrintWriter
import java.io.UnsupportedEncodingException
import java.util.*


class AlternativeMergeTest {
    /**
     * Computes size using the alternative scaling limit for both an idealized merge and for
     * a MergingDigest.
     *
     * This test does some sanity checking, but the real purpose is to create data files
     * `sizes.csv` and `counts.csv`
     * @throws FileNotFoundException If output files can't be created.
     */
    @Test
    @Throws(FileNotFoundException::class)
    fun testMerges() {
        PrintWriter("sizes.csv").use { sizes ->
            PrintWriter("counts.csv").use { out ->
                sizes.printf("algo, counts, digest, compression, n\n")
                out.printf("algo, compression, n, q, count\n")
                for (n in intArrayOf(100, 1000, 10000, 100000)) {
                    for (compression in doubleArrayOf(50.0, 100.0, 200.0, 400.0)) {
                        val digest1 = MergingDigest(compression)
                        val digest2 = AVLTreeDigest(compression)
                        val data = ArrayList<Double>()
                        val gen = Random()
                        for (i in 0 until n) {
                            val x = gen.nextDouble()
                            data.add(x)
                            digest1.add(x)
                            digest2.add(x)
                        }
                        Collections.sort(data)
                        val counts = ArrayList<Double>()
                        var soFar = 0.0
                        var current = 0.0
                        for (x in data) {
                            val q = (soFar + (current + 1.0) / 2) / n
                            if (current == 0.0 || current + 1 < n * Math.PI / compression * Math.sqrt(q * (1 - q))) {
                                current += 1.0
                            } else {
                                counts.add(current)
                                soFar += current
                                current = 1.0
                            }
                        }
                        if (current > 0) {
                            counts.add(current)
                        }
                        sizes.printf(
                            "%s, %d, %d, %.0f, %d\n",
                            "merge",
                            counts.size,
                            digest1.centroids().size,
                            compression,
                            n
                        )
                        sizes.printf(
                            "%s, %d, %d, %.0f, %d\n",
                            "tree",
                            counts.size,
                            digest2.centroids().size,
                            compression,
                            n
                        )
                        sizes.printf("%s, %d, %d, %.0f, %d\n", "ideal", counts.size, counts.size, compression, n)
                        soFar = 0.0
                        for (count in counts) {
                            out.printf(
                                "%s, %.0f, %d, %.3f, %.0f\n",
                                "ideal",
                                compression,
                                n,
                                (soFar + count / 2) / n,
                                count
                            )
                            soFar += count
                        }
                        assertEquals(n.toDouble(), soFar, 0.0)
                        soFar = 0.0
                        for (c in digest1.centroids()) {
                            out.printf(
                                "%s, %.0f, %d, %.3f, %d\n",
                                "merge",
                                compression,
                                n,
                                (soFar + c.count() / 2) / n,
                                c.count()
                            )
                            soFar += c.count().toDouble()
                        }
                        assertEquals(n.toDouble(), soFar, 0.0)
                        soFar = 0.0
                        for (c in digest2.centroids()) {
                            out.printf(
                                "%s, %.0f, %d, %.3f, %d\n",
                                "tree",
                                compression,
                                n,
                                (soFar + c.count() / 2) / n,
                                c.count()
                            )
                            soFar += c.count().toDouble()
                        }
                        assertEquals(n.toDouble(), soFar, 0.0)
                    }
                }
            }
        }
    }
}
