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

import com.carrotsearch.randomizedtesting.annotations.Seed
import com.google.common.collect.Lists
import kotlinx.io.core.Input
import org.apache.mahout.common.RandomUtils
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import java.io.FileNotFoundException
import java.io.IOException
import java.io.PrintWriter
import java.nio.ByteBuffer
import java.util.Random

//to freeze the tests with a particular seed, put the seed on the next line
//@Seed("84527677CF03B566:A6FF596BDDB2D59D")
@Seed("1CD6F48E8CA53BD1:379C5BDEB3A02ACB")
class MergingDigestTest : TDigestTest() {

    override fun factory(compression: Double): DigestFactory {
        return object : DigestFactory {
            override fun create(): TDigest {
                return MergingDigest(compression)
            }
        }
    }

    @Before
    fun testSetUp() {
        RandomUtils.useTestSeed()
    }

    @Test
    fun testApproximation() {
        var worst = 0.0
        var old = java.lang.Double.NEGATIVE_INFINITY
        var x = -1.0
        while (x < 1) {
            val ex = Math.asin(x)
            val actual = MergingDigest.asinApproximation(x)
            val error = ex - actual
            //            System.out.printf("%.8f, %.8f, %.8f, %.12f\n", x, ex, actual, error * 1e6);
            worst = Math.max(worst, Math.abs(error))
            Assert.assertEquals("Bad approximation", 0.0, error, 1e-6)
            Assert.assertTrue("Not monotonic", actual >= old)
            old = actual
            x += 0.0001
        }
        System.out.printf("worst = %.5g\n", worst)

    }

    //    @Test
    fun testFill() {
        val delta = 300
        val x = MergingDigest(delta.toDouble())
        val gen = Random()
        for (i in 0..999999) {
            x.add(gen.nextGaussian())
        }
        var q0 = 0.0
        var i = 0
        System.out.printf("i, q, mean, count, dk\n")
        for (centroid in x.centroids()) {
            val q = q0 + centroid.count().toDouble() / 2.0 / x.size().toDouble()
            val q1 = q0 + centroid.count().toDouble() / x.size()
            val dk = delta * (qToK(q1) - qToK(q0))
            System.out.printf("%d,%.7f,%.7f,%d,%.7f\n", i, q, centroid.mean(), centroid.count(), dk)
            if (java.lang.Double.isNaN(dk)) {
                System.out.printf(">>>> %.8f, %.8f\n", q0, q1)
            }
            Assert.assertTrue(String.format("K-size for centroid %d at %.3f is %.3f", i, centroid.mean(), dk), dk <= 1)
            q0 = q1
            i++
        }
    }

    private fun qToK(q: Double): Double {
        return Math.asin(2 * Math.min(1.0, q) - 1) / Math.PI + 0.5
    }

    @Test
    fun testSmallCountQuantile() {
        val data = Lists.newArrayList(15.0, 20.0, 32.0, 60.0)
        val td = MergingDigest(200.0)
        for (datum in data) {
            td.add(datum!!)
        }
        Assert.assertEquals(21.2, td.quantile(0.4), 1e-10)
    }

    @Test
    @Throws(FileNotFoundException::class)
    fun printQuantiles() {
        // the output of this can be used to visually check the interpolation with R:
        //     x = c(1,2,5,5,6,9,10)
        //     zx = read.csv("interpolation.csv")
        //     plot.ecdf(x)
        //     lines(col='red', q ~ x, zx)
        val td = MergingDigest(200.0)
        td.setMinMax(0.0, 10.0)
        td.add(1.0)
        td.add(2.0)
        td.add(5.0, 2)
        td.add(6.0)
        td.add(9.0)
        td.add(10.0)

        PrintWriter("interpolation.csv").use { quantiles ->
            PrintWriter("reverse.csv").use { cdfs ->

                quantiles.printf("x,q\n")
                cdfs.printf("x,q\n")
                var q = 0.0
                while (q < 1) {
                    val x = td.quantile(q)
                    quantiles.printf("%.3f,%.3f\n", x, q)

                    val roundTrip = td.cdf(x)
                    cdfs.printf("%.3f,%.3f\n", x, q)

                    if (x < 10) {
                        Assert.assertEquals(q, roundTrip, 1e-6)
                    }
                    q += 1e-3
                }
            }
        }

        Assert.assertEquals(2.0 / 7, td.cdf(3.0), 1e-9)
    }

    override fun fromBytes(bytes: Input): TDigest {
        return MergingDigest.fromBytes(bytes)
    }

    companion object {
        @BeforeClass
        @Throws(IOException::class)
        fun setup() {
            setup("merge")
        }
    }
}