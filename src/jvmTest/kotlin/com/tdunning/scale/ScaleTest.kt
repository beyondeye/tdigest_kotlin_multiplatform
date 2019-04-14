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

package com.tdunning.scale

import com.google.common.collect.Lists
import org.junit.Test

import org.junit.Assume.assumeTrue

/**
 * Tests scaling properties of t-digest variants
 */
class ScaleTest {
    @Test
    fun testGrowth() {
        assumeTrue(java.lang.Boolean.parseBoolean(System.getProperty("runSlowTests")))
        for (limit in arrayOf(
            RootLinearLimit(),
            RootLimit(),
            StandardLimit(),
            LinearLimit(),
            PiecewiseLinearLimit(0.05),
            PiecewiseLinearLimit(0.1),
            PiecewiseLinearLimit(0.2)
        )) {
            for (n in longArrayOf(1000, 10000, 100000, 1000000L, 10000000L, 100000000L, 1000000000L)) {
                val r = size(n, 200.0, limit)
                var nonTrivial = 0
                for (centroid in r) {
                    if (centroid.count > 1) {
                        nonTrivial++
                    }
                }
                System.out.printf("%s\t%d\t%d\t%d\n", limit.javaClass.simpleName, n, r.size, nonTrivial)
            }
        }
    }

    fun size(n: Long, compression: Double, limit: Limit?): List<Centroid> {
        var compression = compression
        var limit = limit
        if (compression <= 0) {
            compression = 50.0
        }

        if (limit == null) {
            limit = StandardLimit()
        }

        var total = 0.0
        var i: Long = 0
        val r = Lists.newArrayList<Centroid>()
        while (i < n) {
            var mean = i.toDouble()
            var count = 1
            i++
            var qx = total / n

            while (i < n && count + 1 <= Math.max(1.0, limit.limit(n, qx) / compression)) {
                count++
                mean += (i - mean) / count
                qx = (total + count / 2) / n
                i++
            }
            total += count.toDouble()
            r.add(Centroid(mean, count))
        }
        return r
    }

    class Centroid(internal val mean: Double, internal val count: Int)

    interface Limit {
        fun limit(n: Long, q: Double): Double
    }

    class StandardLimit : Limit {
        override fun limit(n: Long, q: Double): Double {
            return 4.0 * n.toDouble() * q * (1 - q)
        }
    }

    class RootLimit : Limit {
        override fun limit(n: Long, q: Double): Double {
            return 2.0 * n.toDouble() * Math.sqrt(q * (1 - q))
        }
    }

    class LinearLimit : Limit {
        override fun limit(n: Long, q: Double): Double {
            return 2.0 * n.toDouble() * Math.min(q, 1 - q)
        }
    }

    class RootLinearLimit : Limit {
        override fun limit(n: Long, q: Double): Double {
            return n * Math.sqrt(2 * Math.min(q, 1 - q))
        }
    }

    class PowerLinearLimit(private val exp: Double) : Limit {

        override fun limit(n: Long, q: Double): Double {
            return n * Math.pow(2 * Math.min(q, 1 - q), exp)
        }
    }

    private inner class PiecewiseLinearLimit internal constructor(private val cut: Double) : Limit {

        override fun limit(n: Long, q: Double): Double {
            return if (q < cut) {
                n * q / cut
            } else if (1 - q < cut) {
                limit(n, 1 - q)
            } else {
                n.toDouble()
            }

        }
    }
}
