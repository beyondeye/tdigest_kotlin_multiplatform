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

import com.basicio.BinaryInput
import com.carrotsearch.randomizedtesting.annotations.Seed
import com.google.common.collect.Lists
import com.tdunning.math.stats.Dist.quantile
import org.apache.mahout.common.RandomUtils
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.IOException
import java.util.*


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


    override fun fromBytes(bytes: BinaryInput): TDigest {
        return MergingDigest.fromBytes(bytes)
    }

    // This test came from PR#145 by github user pulver
    @Test
    fun testNanDueToBadInitialization() {
        val compression = 30
        val factor = 5
        val md = MergingDigest(compression.toDouble(), (factor + 1) * compression, compression)
        val M = 10
        val mds: MutableList<MergingDigest> = ArrayList()
        for (i in 0 until M) {
            mds.add(MergingDigest(compression.toDouble(), (factor + 1) * compression, compression))
        }

        // Fill all digests with values (0,10,20,...,80).
        val raw: MutableList<Double> = Lists.newArrayList()
        for (i in 0..8) {
            val x = (10 * i).toDouble()
            md.add(x)
            raw.add(x)
            for (j in 0 until M) {
                mds[j].add(x)
                raw.add(x)
            }
        }
        Collections.sort(raw)

        // Merge all mds one at a time into md.
        for (i in 0 until M) {
            val singleton: MutableList<MergingDigest> = ArrayList()
            singleton.add(mds[i])
            md.add(singleton)
        }
        //        md.add(mds);

//        Assert.assertFalse(Double.isNaN(md.quantile(0.01)));
        // Output
        System.out.printf("%4s\t%10s\t%10s\t%10s\t%10s\n", "q", "estimated", "actual", "error_cdf", "error_q")
        val dashes = "=========="
        System.out.printf("%4s\t%10s\t%10s\t%10s\t%10s\n", dashes.substring(0, 4), dashes, dashes, dashes, dashes)
        for (q in doubleArrayOf(0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 0.90, 0.95, 0.99)) {
            val est = md.quantile(q)
            val actual = quantile(q, raw)
            val qx = md.cdf(actual)
            Assert.assertEquals(q, qx, 0.08)
            Assert.assertEquals(est, actual, 3.5)
            System.out.printf(
                "%4.2f\t%10.2f\t%10.2f\t%10.2f\t%10.2f\n",
                q,
                est,
                actual,
                Math.abs(est - actual),
                Math.abs(qx - q)
            )
        }
    }


    /**
     * Verifies interpolation between a singleton and a larger centroid.
     */
    @Test
    fun singleMultiRange() {
        val digest = factory(50.0).create()
        digest.setScaleFunction(ScaleFunction.K_0)
        for (i in 0..99) {
            digest.add(1.0)
            digest.add(2.0)
            digest.add(3.0)
        }
        // this check is, of course true, but it also forces merging before we change scale
        assertTrue(digest.centroidCount() < 300)
        digest.add(0.0)
        // we now have a digest with a singleton first, then a heavier centroid next
        val ix = digest.centroids().iterator()
        val first = ix.next()
        val second = ix.next()
        assertEquals(1, first.count().toLong())
        assertEquals(0.0, first.mean(), 0.0)
        //        assertTrue(second.count() > 1);
        assertEquals(1.0, second.mean(), 0.0)
        assertEquals(0.5 / digest.size(), digest.cdf(0.0), 0.0)
        assertEquals(1.0 / digest.size(), digest.cdf(1e-10), 1e-10)
        assertEquals(1.0 / digest.size(), digest.cdf(0.25), 1e-10)
    }

    /**
     * Make sure that the first and last centroids have unit weight
     */
    @Test
    fun testSingletonsAtEnds() {
        val d: TDigest = MergingDigest(50.0)
        d.recordAllData()
        val gen = Random(1)
        val data = DoubleArray(100)
        for (i in data.indices) {
            data[i] = Math.floor(gen.nextGaussian() * 3)
        }
        for (i in 0..99) {
            for (x in data) {
                d.add(x)
            }
        }
        var last = 0
        for (centroid in d.centroids()) {
            if (last == 0) {
                assertEquals(1, centroid.count().toLong())
            }
            last = centroid.count()
        }
        assertEquals(1, last.toLong())
    }

    /**
     * Verify centroid sizes.
     */
    @Test
    fun testFill() {
        val x = MergingDigest(300.0)
        val gen = Random()
        val scale = x.scaleFunction
        val compression = x.compression()
        for (i in 0..999999) {
            x.add(gen.nextGaussian())
        }
        var q0 = 0.0
        var i = 0
        System.out.printf("i, q, mean, count, dk\n")
        for (centroid in x.centroids()) {
            val q = q0 + centroid.count() / 2.0 / x.size()
            val q1 = q0 + centroid.count().toDouble() / x.size()
            var dk = scale.k(q1, compression, x.size().toDouble()) - scale.k(q0, compression, x.size().toDouble())
            if (centroid.count() > 1) {
                assertTrue(String.format("K-size for centroid %d at %.3f is %.3f", i, centroid.mean(), dk), dk <= 1)
            } else {
                dk = 1.0
            }
            System.out.printf("%d,%.7f,%.7f,%d,%.7f\n", i, q, centroid.mean(), centroid.count(), dk)
            if (java.lang.Double.isNaN(dk)) {
                System.out.printf(">>>> %.8f, %.8f\n", q0, q1)
            }
            q0 = q1
            i++
        }
    }
    companion object {
        @BeforeClass
        @Throws(IOException::class)
        fun setup() {
            setup("merge")
        }
    }
}