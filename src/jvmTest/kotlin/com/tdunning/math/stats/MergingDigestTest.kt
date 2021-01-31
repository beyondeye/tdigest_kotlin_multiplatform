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
import org.apache.mahout.common.RandomUtils
import org.junit.Before
import org.junit.BeforeClass

import java.io.IOException

import com.google.common.collect.Lists
import com.tdunning.math.stats.Dist.quantile
import org.junit.Assert
import org.junit.Test
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
    companion object {
        @BeforeClass
        @Throws(IOException::class)
        fun setup() {
            setup("merge")
        }
    }
}