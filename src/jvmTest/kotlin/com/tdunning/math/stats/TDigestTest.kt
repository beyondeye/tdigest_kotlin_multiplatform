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
import com.basicio.toBinaryInput
import com.basicio.toBinaryOutput
import com.carrotsearch.randomizedtesting.RandomizedTest
import com.clearspring.analytics.stream.quantile.QDigest
import com.google.common.collect.Lists
import com.tdunning.math.stats.Dist.cdf
import com.tdunning.math.stats.Dist.quantile
import kotlinx.io.core.buildPacket
import org.apache.mahout.common.RandomUtils
import org.apache.mahout.math.jet.random.AbstractContinousDistribution
import org.apache.mahout.math.jet.random.Gamma
import org.apache.mahout.math.jet.random.Normal
import org.apache.mahout.math.jet.random.Uniform
import org.junit.*
import java.io.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


/**
 * Base test case for TDigests, just extend this class and implement the abstract methods.
 */
@Ignore
abstract class TDigestTest : AbstractTest() {

    @After
    fun flush() {
        synchronized(lock) {
            if (sizeDump != null) {
                sizeDump!!.flush()
            }
            if (errorDump != null) {
                errorDump!!.flush()
            }
            if (deviationDump != null) {
                deviationDump!!.flush()
            }
        }
    }

    interface DigestFactory {
        fun create(): TDigest
    }

    protected abstract fun factory(compression: Double): DigestFactory

    private fun factory(): DigestFactory {
        return factory(100.0)
    }

    @Test
    fun offsetUniform() {
        System.out.printf("delta, q, x1, x2, q1, q2, error_x, error_q\n")
        for (compression in doubleArrayOf(20.0, 50.0, 100.0, 200.0)) {
            val digest = factory(compression).create()
            digest.setScaleFunction(ScaleFunction.K_0)
            val rand = Random()
            val gen = Uniform(50.0, 51.0, rand)
            val data = DoubleArray(1000000)
            for (i in 0..999999) {
                data[i] = gen.nextDouble()
                digest.add(data[i])
            }
            Arrays.sort(data)
            for (q in doubleArrayOf(0.5, 0.9, 0.99, 0.999, 0.9999, 0.99999, 0.999999)) {
                val x1 = Dist.quantile(q, data)
                val x2 = digest.quantile(q)
                val q1 = Dist.cdf(x1, data)
                val q2 = digest.cdf(x1)
                System.out.printf(
                    "%.0f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f\n",
                    compression, q, x1, x2, q1, q2, Math.abs(x1 - x2) / (1 - q), Math.abs(q1 - q2) / (1 - q)
                )
            }
        }
    }


    @Test
    open fun bigJump() {
        var digest = factory(100.0).create()
        for (i in 1..19) {
            digest.add(i.toDouble())
        }
        digest.add(1000000.0)
        assertEquals(18.0, digest.quantile(0.89999999), 0.0)
        assertEquals(19.0, digest.quantile(0.9), 0.0)
        assertEquals(19.0, digest.quantile(0.949999999), 0.0)
        assertEquals(1000000.0, digest.quantile(0.95), 0.0)
        assertEquals(0.925, digest.cdf(19.0), 1e-11)
        assertEquals(0.95, digest.cdf(19.0000001), 1e-11)
        assertEquals(0.9, digest.cdf(19 - 0.0000001), 1e-11)
        digest = factory(80.0).create()
        digest.setScaleFunction(ScaleFunction.K_0)
        for (j in 0..99) {
            for (i in 1..19) {
                digest.add(i.toDouble())
            }
            digest.add(1000000.0)
        }
        assertEquals(18.0, digest.quantile(0.885), 0.0)
        assertEquals(19.0, digest.quantile(0.915), 0.0)
        assertEquals(19.0, digest.quantile(0.935), 0.0)
        assertEquals(1000000.0, digest.quantile(0.965), 0.0)
    }


    @Test
    fun testSmallCountQuantile() {
        val data = Lists.newArrayList(15.0, 20.0, 32.0, 60.0)
        val td = factory(200.0).create()
        for (datum in data) {
            td.add(datum!!)
        }
        Assert.assertEquals(20.0, td.quantile(0.4), 1e-10)
        Assert.assertEquals(20.0, td.quantile(0.25), 1e-10)
        Assert.assertEquals(15.0, td.quantile(0.25 - 1e-10), 1e-10)
        Assert.assertEquals(20.0, td.quantile(0.5 - 1e-10), 1e-10)
        Assert.assertEquals(32.0, td.quantile(0.5), 1e-10)
    }

    /**
     * Check against the example given in
     * https://github.com/tdunning/t-digest/issues/143
     *
     * Don't think that there is a problem here, but keeping the test just in case.
     */
    @Test
    open fun testExplicitSkewedData() {
        val data = doubleArrayOf(
            245.0,
            246.0,
            247.249,
            240.0,
            243.0,
            248.0,
            250.0,
            241.0,
            244.0,
            245.0,
            245.0,
            247.0,
            243.0,
            242.0,
            241.0,
            50100.0,
            51246.0,
            52247.0,
            52249.0,
            51240.0,
            53243.0,
            59248.0,
            59250.0,
            57241.0,
            56244.0,
            55245.0,
            56245.0,
            575247.0,
            58243.0,
            51242.0,
            54241.0
        )
        val digest = factory(50.0).create()
        for (x in data) {
            digest.add(x)
        }
        assertEquals(quantile(0.5, data), digest.quantile(0.5), 0.0)
    }

    /**
     * See https://github.com/tdunning/t-digest/issues/114
     *
     *
     * The problem in that issue seems to have been due to adding samples with non-unit weight.
     * This resulted in a violation of the t-digest invariant.
     *
     *
     * The question in the issue about the origin of the shuffle still applies.
     */
    @Test
    open fun testQuantile() {
        val compression = 100.0
        val samples = doubleArrayOf(1.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 3.0, 3.0, 3.0, 3.0, 4.0, 5.0, 6.0, 7.0)
        for (i in 1..9999) {
            val hist1: TDigest = MergingDigest(compression)
            val data: MutableList<Double> = ArrayList()
            for (j in 0..99) {
                for (x in samples) {
                    data.add(x)
                    hist1.add(x)
                }
            }
            val hist2: TDigest = MergingDigest(compression)
            hist1.compress()
            hist2.add(hist1)
            Collections.sort(data)
            hist2.compress()
            val x1 = hist1.quantile(0.5)
            val x2 = hist2.quantile(0.5)
            assertEquals(Dist.quantile(0.5, data), x1, 0.2);
            assertEquals(x1, x2, 0.01);
        }
    }


    /**
     * Brute force test that cdf and quantile give reference behavior in digest made up of all singletons.
     */
    @Test
    fun singletonQuantiles() {
        val data = DoubleArray(20)
        val digest = factory(100.0).create()
        for (i in 0..19) {
            digest.add(i.toDouble())
            data[i] = i.toDouble()
        }

        var x = digest.min - 0.1
        while (x <= digest.max + 0.1) {
            Assert.assertEquals(Dist.cdf(x, data), digest.cdf(x), 0.0)
            x += 1e-3
        }

        var q = 0.0
        while (q <= 1) {
            Assert.assertEquals(Dist.quantile(q, data), digest.quantile(q), 0.0)
            q += 1e-3
        }
    }

    /**
     * Verifies behavior involving interpolation (or lack of same, really) between singleton centroids.
     */
    @Test
    open fun singleSingleRange() {
        val digest = factory(100.0).create()
        digest.add(1.0)
        digest.add(2.0)
        digest.add(3.0)

        // verify the cdf is a step between singletons
        Assert.assertEquals(0.5 / 3.0, digest.cdf(1.0), 0.0)
        Assert.assertEquals(1 / 3.0, digest.cdf(1 + 1e-10), 0.0)
        Assert.assertEquals(1 / 3.0, digest.cdf(2 - 1e-10), 0.0)
        Assert.assertEquals(1.5 / 3.0, digest.cdf(2.0), 0.0)
        Assert.assertEquals(2 / 3.0, digest.cdf(2 + 1e-10), 0.0)
        Assert.assertEquals(2 / 3.0, digest.cdf(3 - 1e-10), 0.0)
        Assert.assertEquals(2.5 / 3.0, digest.cdf(3.0), 0.0)
        Assert.assertEquals(1.0, digest.cdf(3 + 1e-10), 0.0)
    }

    /**
     * Test with adversarial inputs.
     */
    @Test
    @Throws(FileNotFoundException::class)
    open fun testAdversarial() {
        val kilo = 1000
        val gen = Random()
        val maxE = Math.log(10.0) * 308
        PrintWriter("adversarial.csv").use { out ->
            out.printf("k,n,E,q,x0,x1,q0,q1\n")
            for (N in intArrayOf(100 * kilo, 1000 * kilo)) {
                System.out.printf("%d\n", N)
                val data = DoubleArray(N)
                for (E in doubleArrayOf(10.0, 100.0, 300.0, 700.0, maxE)) {
                    val digest: TDigest = MergingDigest(500.0)
                    for (i in 0 until N) {
                        val u = gen.nextDouble()
                        data[i] = (if (gen.nextDouble() < 0.01) -1 else 1) * Math.exp((2 * u - 1) * E)
                        digest.add(data[i])
                    }
                    Arrays.sort(data)
                    for (k in 0..9) {
                        for (q in doubleArrayOf(0.0, 1e-6, 1e-5, 1e-4, 1e-3, 1e-2, 0.1, 0.5)) {
                            val x0 = quantile(q, data)
                            val x1 = digest.quantile(q)
                            val q0 = cdf(x0, data)
                            val q1 = cdf(x1, data)
                            out.printf("%d,%d,%.0f,%.6f,%.6g,%.6g,%.6f,%.6f\n", k, N, E, q, x0, x1, q0, q1)
                        }
                    }
                }
            }
        }
    }
    /**
     * Tests cases where min or max is not the same as the extreme centroid which has weight>1. In these cases min and
     * max give us a little information we wouldn't otherwise have.
     */
    @Test
    fun singletonAtEnd() {
        var digest = MergingDigest(100.0)
        digest.add(1.0)
        digest.add(2.0)
        digest.add(3.0)

        Assert.assertEquals(1.0, digest.min, 0.0)
        Assert.assertEquals(3.0, digest.max, 0.0)
        Assert.assertEquals(3, digest.centroidCount().toLong())
        Assert.assertEquals(0.0, digest.cdf(0.0), 0.0)
        Assert.assertEquals(0.0, digest.cdf(1 - 1e-9), 0.0)
        Assert.assertEquals(0.5 / 3, digest.cdf(1.0), 1e-10)
        Assert.assertEquals(1.0 / 3, digest.cdf(1 + 1e-10), 1e-10)
        Assert.assertEquals(2.0 / 3, digest.cdf(3 - 1e-9), 0.0)
        Assert.assertEquals(2.5 / 3, digest.cdf(3.0), 0.0)
        Assert.assertEquals(1.0, digest.cdf(3 + 1e-9), 0.0)

        digest.add(1.0)
        Assert.assertEquals(1.0 / 4, digest.cdf(1.0), 0.0)

        // normally min == mean[0] because weight[0] == 1
        // we can force this not to be true for testing
        digest = MergingDigest(1.0)
        digest.setScaleFunction(ScaleFunction.K_0)
        for (i in 0..99) {
            digest.add(1.0)
            digest.add(2.0)
            digest.add(3.0)
        }
        // This sample would normally be added to the first cluster that already exists
        // but there is special code in place to prevent that centroid from ever
        // having weight of more than one
        // As such, near q=0, cdf and quantiles
        // should reflect this single sample as a singleton
        digest.add(0.0)
        Assert.assertTrue(digest.centroidCount() > 0)
        val first = digest.centroids().iterator().next()
        assertEquals(1, first.count())
        assertEquals(first.mean(), digest.min, 0.0)
        Assert.assertEquals(0.0, digest.min, 0.0)
        Assert.assertEquals(0.0, digest.cdf(0 - 1e-9), 0.0)
        Assert.assertEquals(0.5 / digest.size(), digest.cdf(0.0), 1e-10)
        Assert.assertEquals(1.0 / digest.size(), digest.cdf(1e-9), 1e-10)

        Assert.assertEquals(0.0, digest.quantile(0.0), 0.0)
        Assert.assertEquals(0.0, digest.quantile(0.5 / digest.size()), 0.0)
        Assert.assertEquals(0.0, digest.quantile(1.0 / digest.size() - 1e-10), 0.0)
        Assert.assertEquals(0.0, digest.quantile(1.0 / digest.size()), 0.0)
        assertEquals(first.mean(), 0.0, 1e-5)

        digest.add(4.0)
        val last = Lists.reverse(Lists.newArrayList(digest.centroids())).iterator().next()
        assertEquals(1.0, last.count().toDouble(), 0.0)
        assertEquals(4.0, last.mean(), 0.0)

        Assert.assertEquals(1.0, digest.cdf(digest.max + 1e-9), 0.0)
        Assert.assertEquals(1 - 0.5 / digest.size(), digest.cdf(digest.max), 0.0)
        Assert.assertEquals(1 - 1.0 / digest.size(), digest.cdf(digest.max - 1e-9), 1e-10)

        Assert.assertEquals(4.0, digest.quantile(1.0), 0.0)
        Assert.assertEquals(4.0, digest.quantile(1 - 0.5 / digest.size()), 0.0)
        Assert.assertEquals(4.0, digest.quantile(1 - 1.0 / digest.size() + 1e-10), 0.0)
        Assert.assertEquals(4.0, digest.quantile(1 - 1.0 / digest.size()), 0.0)
        assertEquals(last.mean(), 4.0, 0.0)
    }

    protected abstract fun fromBytes(bytes: BinaryInput): TDigest

    @Test
    fun testSingleValue() {
        val digest = factory().create()
        val value = RandomizedTest.getRandom().nextDouble() * 1000
        digest.add(value)
        val q = RandomizedTest.getRandom().nextDouble()
        for (qValue in doubleArrayOf(0.0, q, 1.0)) {
            Assert.assertEquals(value, digest.quantile(qValue), 0.001)
        }
    }

    @Test
    fun testFewValues() {
        // When there are few values in the tree, quantiles should be exact
        val digest = factory().create()
        val r = RandomizedTest.getRandom()
        val length = r.nextInt(10)
        val values = ArrayList<Double>()
        for (i in 0 until length) {
            val value: Double
            if (i == 0 || r.nextBoolean()) {
                value = r.nextDouble() * 100
            } else {
                // introduce duplicates
                value = values[i - 1]
            }
            digest.add(value)
            values.add(value)
        }
        Collections.sort(values)

        // for this value of the compression, the tree shouldn't have merged any node
        Assert.assertEquals(digest.centroids().size.toLong(), values.size.toLong())
        for (q in doubleArrayOf(0.0, 1e-10, r.nextDouble(), 0.5, 1 - 1e-10, 1.0)) {
            val q1 = Dist.quantile(q, values)
            val q2 = digest.quantile(q)
            Assert.assertEquals(String.format("At q=%g, expected %.2f vs %.2f", q, q1, q2), q1, q2, 0.03)
        }
    }

    private fun repeats(): Int {
        return if (java.lang.Boolean.parseBoolean(System.getProperty("runSlowTests"))) 10 else 1
    }

    fun testEmptyDigest() {
        val digest = factory().create()
        Assert.assertEquals(0, digest.centroids().size.toLong())
        Assert.assertEquals(0, digest.centroids().size.toLong())
    }

    /**
     * Builds estimates of the CDF of a bunch of data points and checks that the centroids are accurately positioned.
     * Accuracy is assessed in terms of the estimated CDF which is much more stringent than checking position of
     * quantiles with a single value for desired accuracy.
     *
     * @param gen           Random number generator that generates desired values.
     * @param tag           Label for the output lines
     * @param recordAllData True if the internal histogrammer should be set up to record all data it sees for
     */
    private fun runTest(
        factory: DigestFactory,
        gen: AbstractContinousDistribution,
        qValues: DoubleArray,
        tag: String,
        recordAllData: Boolean
    ) {
        val dist = factory.create()
        if (recordAllData) {
            dist.recordAllData()
        }

        val data = DoubleArray(100000)
        for (i in 0..99999) {
            val x = gen.nextDouble()
            data[i] = x
        }
        val t0 = System.nanoTime()
        for (x in data) {
            dist.add(x)
        }
        System.out.printf("# %fus per point\n", (System.nanoTime() - t0) * 1e-3 / 100000)
        System.out.printf("# %d centroids\n", dist.centroids().size)
        Arrays.sort(data)

        val xValues = qValues.clone()
        for (i in qValues.indices) {
            xValues[i] = Dist.quantile(qValues[i], data)
        }

        var qz = 0.0
        var iz = 0
        for (centroid in dist.centroids()) {
            val q = (qz + centroid.count() / 2.0) / dist.size()
            sizeDump?.printf(
                "%s\t%d\t%.6f\t%.3f\t%d\n",
                tag,
                iz,
                q,
                4.0 * q * (1 - q) * dist.size().toDouble() / dist.compression(),
                centroid.count()
            )
            qz += centroid.count().toDouble()
            iz++
        }
        Assert.assertEquals(iz.toLong(), dist.centroids().size.toLong())
        dist.compress()
        Assert.assertEquals(qz, dist.size().toDouble(), 1e-10)

        Assert.assertTrue(
            String.format(
                "Summary is too large (got %d, wanted <= %.1f)",
                dist.centroids().size,
                dist.compression()
            ), dist.centroids().size <= dist.compression()
        )
        var softErrors = 0
        for (i in xValues.indices) {
            val x = xValues[i]
            val q = qValues[i]
            var estimate = dist.cdf(x)
            errorDump?.printf("%s\t%s\t%.8g\t%.8f\t%.8f\n", tag, "cdf", x, q, estimate - q)
            Assert.assertEquals(q, estimate, 0.08)

            estimate = Dist.cdf(dist.quantile(q), data)
            errorDump?.printf("%s\t%s\t%.8g\t%.8f\t%.8f\n", tag, "quantile", x, q, estimate - q)
            if (Math.abs(q - estimate) > 0.005) {
                softErrors++
            }
            Assert.assertEquals(String.format("discrepancy %.5f vs %.5f @ %.5f", q, estimate, x), q, estimate, 0.012)
        }
        Assert.assertTrue(softErrors < 3)

        if (recordAllData) {
            val ix = dist.centroids().iterator()
            var b: Centroid = ix.next()
            var c: Centroid = ix.next()
            qz = b.count().toDouble()
            while (ix.hasNext()) {
                val a = b
                b = c
                c = ix.next()
                val left = (b.mean() - a.mean()) / 2
                val right = (c.mean() - b.mean()) / 2

                val q = (qz + b.count() / 2.0) / dist.size()
                for (x in b.data()!!) {
                    deviationDump?.printf(
                        "%s\t%.5f\t%d\t%.5g\t%.5g\t%.5g\t%.5g\t%.5f\n",
                        tag,
                        q,
                        b.count(),
                        x,
                        b.mean(),
                        left,
                        right,
                        (x - b.mean()) / (right + left)
                    )
                }
                qz += a.count().toDouble()
            }
        }
    }

    @Test
    fun testEmpty() {
        val digest = factory().create()
        val q = RandomizedTest.getRandom().nextDouble()
        Assert.assertTrue(java.lang.Double.isNaN(digest.quantile(q)))
    }

    @Test
    fun testMoreThan2BValues() {
        val digest = factory(100.0).create()
        // carefully build a t-digest that is as if we added 3 uniform values from [0,1]
        val n = 3e9
        var q0 = 0.0
        var i = 0
        while (i < 200 && q0 < 1 - 1e-10) {
            val k0 = digest.scale.k(q0, digest.compression(), n)
            val q = digest.scale.q(k0 + 1, digest.compression(), n)
            val m = Math.max(1.0, n * (q - q0)).toInt()
            digest.add((q + q0) / 2, m)
            q0 = q0 + m / n
            ++i
        }
        digest.compress()
        assertEquals(3000000000L, digest.size())
        Assert.assertTrue(digest.size() > Integer.MAX_VALUE)

        val quantiles = doubleArrayOf(0.0, 0.1, 0.5, 0.9, 1.0)
        var prev = java.lang.Double.NEGATIVE_INFINITY
        for (q in quantiles) {
            val v = digest.quantile(q)
            Assert.assertTrue(String.format("q=%.1f, v=%.4f, pref=%.4f", q, v, prev), v >= prev)
            prev = v
        }
    }


    fun testSorted() {
        val digest = factory().create()
        val gen = RandomizedTest.getRandom()
        for (i in 0..9999) {
            val w = 1 + gen.nextInt(10)
            val x = gen.nextDouble()
            for (j in 0 until w) {
                digest.add(x)
            }
        }
        var previous: Centroid? = null
        for (centroid in digest.centroids()) {
            if (previous != null) {
                Assert.assertTrue(previous.mean() <= centroid.mean())
            }
            previous = centroid
        }
    }

    @Test
    fun testNaN() {
        val digest = factory().create()
        val gen = RandomizedTest.getRandom()
        val iters = gen.nextInt(100)
        for (i in 0 until iters) {
            digest.add(gen.nextDouble(), 1 + gen.nextInt(10))
        }
        try {
            // both versions should fail
            if (gen.nextBoolean()) {
                digest.add(java.lang.Double.NaN)
            } else {
                digest.add(java.lang.Double.NaN, 1)
            }
            Assert.fail("NaN should be an illegal argument")
        } catch (e: IllegalArgumentException) {
            // expected
        }

    }


    @Test
    fun testUniform() {
        val gen = RandomizedTest.getRandom()
        for (i in 0 until repeats()) {
            runTest(
                factory(), Uniform(0.0, 1.0, gen),
                doubleArrayOf(0.001, 0.01, 0.1, 0.5, 0.9, 0.99, 0.999),
                "uniform", true
            )
        }
    }

    @Test
    fun testGamma() {
        // this Gamma distribution is very heavily skewed.  The 0.1%-ile is 6.07e-30 while
        // the median is 0.006 and the 99.9th %-ile is 33.6 while the mean is 1.
        // this severe skew means that we have to have positional accuracy that
        // varies by over 11 orders of magnitude.
        val gen = RandomizedTest.getRandom()
        for (i in 0 until repeats()) {
            runTest(
                factory(200.0), Gamma(0.1, 0.1, gen),
                //                    new double[]{6.0730483624079e-30, 6.0730483624079e-20, 6.0730483627432e-10, 5.9339110446023e-03,
                //                            2.6615455373884e+00, 1.5884778179295e+01, 3.3636770117188e+01},
                doubleArrayOf(0.001, 0.01, 0.1, 0.5, 0.9, 0.99, 0.999),
                "gamma", true
            )
        }
    }

    @Test
    fun testNarrowNormal() {
        // this mixture of a uniform and normal distribution has a very narrow peak which is centered
        // near the median.  Our system should be scale invariant and work well regardless.
        val gen = RandomizedTest.getRandom()
        val mix = object : AbstractContinousDistribution() {
            val normal: AbstractContinousDistribution = Normal(0.0, 1e-5, gen)
            val uniform: AbstractContinousDistribution = Uniform(-1.0, 1.0, gen)

            override fun nextDouble(): Double {
                val x: Double
                if (gen.nextDouble() < 0.5) {
                    x = uniform.nextDouble()
                } else {
                    x = normal.nextDouble()
                }
                return x
            }
        }

        for (i in 0 until repeats()) {
            runTest(
                factory(400.0),
                mix,
                doubleArrayOf(0.001, 0.01, 0.1, 0.3, 0.5, 0.7, 0.9, 0.99, 0.999),
                "mixture",
                false
            )
        }
    }
    @Test
    open fun testMidPointRule() {
        val dist = factory(200.0).create()
        dist.add(1.0)
        dist.add(2.0)
        var scale = 0.0
        for (i in 0..999) {
            dist.add(1.0)
            dist.add(2.0)
            if (i % 8 == 0) {
                val message = String.format("i = %d", i)
                assertEquals(message, 0.0, dist.cdf(1 - 1e-9), 0.0)
                assertEquals(message, 0.25, dist.cdf(1.0), 0.01 * scale)
                assertEquals(message, 0.5, dist.cdf(1 + 1e-9), 0.03 * scale)
                assertEquals(message, 0.5, dist.cdf(2 - 1e-9), 0.03 * scale)
                assertEquals(message, 0.75, dist.cdf(2.0), 0.01 * scale)
                assertEquals(message, 1.0, dist.cdf(2 + 1e-9), 0.0)
                assertEquals(1.0, dist.quantile(0.0), 0.0)
                assertEquals(1.0, dist.quantile(0.1), 0.0)
                assertEquals(1.0, dist.quantile(0.2), 0.0)
                assertEquals(1.0, dist.quantile(0.4), 0.0)
                assertEquals(2.0, dist.quantile(0.6), 0.0)
                assertEquals(2.0, dist.quantile(0.7), 0.0)
                assertEquals(2.0, dist.quantile(0.8), 0.0)
                assertEquals(2.0, dist.quantile(0.9), 0.0)
                assertEquals(2.0, dist.quantile(1.0), 0.0)
            }
            // this limit should be 70 for merging digest variants
            // I decreased it to help out AVLTreeDigest.
            // TODO fix AVLTreeDigest behavior
            if (i >= 39) {
                // when centroids start doubling up, accuracy is no longer perfect
                scale = 1.0;
            }
        }
    }
    @Test
    open fun testRepeatedValues() {
        val gen = RandomizedTest.getRandom()

        // 5% of samples will be 0 or 1.0.  10% for each of the values 0.1 through 0.9
        val mix = object : AbstractContinousDistribution() {
            override fun nextDouble(): Double {
                return Math.rint(gen.nextDouble() * 10) / 10.0
            }
        }

        val dist = factory(400.0).create()
        val data = Lists.newArrayList<Double>()
        for (i1 in 0..999999) {
            val x = mix.nextDouble()
            data.add(x)
        }

        val t0 = System.nanoTime()
        for (x in data) {
            dist.add(x)
        }

        System.out.printf("# %fus per point\n", (System.nanoTime() - t0) * 1e-3 / 1000000)
        System.out.printf("# %d centroids\n", dist.centroids().size)

        Assert.assertTrue("Summary is too large: " + dist.centroids().size, dist.centroids().size < dist.compression())

        // all quantiles should round to nearest actual value
        for (i in 0..9) {
            val z = i / 10.0
            // we skip over troublesome points that are nearly halfway between
            for (delta in doubleArrayOf(0.01, 0.02, 0.03, 0.07, 0.08, 0.09)) {
                val q = z + delta
                val cdf = dist.cdf(q)
                // we also relax the tolerances for repeated values
                Assert.assertEquals(String.format("z=%.1f, q = %.3f, cdf = %.3f", z, q, cdf), z + 0.05, cdf, 0.03)

                val estimate = dist.quantile(q)
                Assert.assertEquals(
                    String.format("z=%.1f, q = %.3f, cdf = %.3f, estimate = %.3f", z, q, cdf, estimate),
                    Math.rint(q * 10) / 10.0, estimate, 0.02
                )
            }
        }
    }

    @Test
    fun testSequentialPoints() {
        for (i in 0 until repeats()) {
            runTest(
                factory(), object : AbstractContinousDistribution() {
                    var base = 0.0

                    override fun nextDouble(): Double {
                        base += Math.PI * 1e-5
                        return base
                    }
                }, doubleArrayOf(0.001, 0.01, 0.1, 0.5, 0.9, 0.99, 0.999),
                "sequential", true
            )
        }
    }

    @Test
    fun testSerialization() {
        val gen = RandomizedTest.getRandom()
        val compression = 20 + RandomizedTest.randomDouble() * 100
        val dist = factory(compression).create()
        for (i in 0..99999) {
            val x = gen.nextDouble()
            dist.add(x)
        }
        dist.compress()

//        val buf = ByteBuffer.allocate(20000)
        var writtenBytes=0
        val buf= buildPacket {
            dist.asBytes(this.toBinaryOutput())
            writtenBytes=this.size
        }
//        buf.readFully()
//       writtenBytes=buf.remaining
        Assert.assertTrue(String.format("size is %d\n", writtenBytes), writtenBytes < 12000)
        Assert.assertEquals(dist.byteSize().toLong(), writtenBytes.toLong())

        System.out.printf("# big %d bytes\n", writtenBytes)

        var dist2 = fromBytes(buf.toBinaryInput())
        buf.release()
        Assert.assertEquals(dist.centroids().size.toLong(), dist2.centroids().size.toLong())
        Assert.assertEquals(dist.compression(), dist2.compression(), 1e-4)
        Assert.assertEquals(dist.size(), dist2.size())

        run {
            var q = 0.0
            while (q < 1) {
                Assert.assertEquals(dist.quantile(q), dist2.quantile(q), 1e-5)
                q += 0.01
            }
        }

        var ix: Iterator<Centroid> = dist2.centroids().iterator()
        for (centroid in dist.centroids()) {
            Assert.assertTrue(ix.hasNext())
            Assert.assertEquals(centroid.count().toLong(), ix.next().count().toLong())
        }
        Assert.assertFalse(ix.hasNext())

        val bufsmall = buildPacket {
            dist.asSmallBytes(this.toBinaryOutput())
            writtenBytes=this.size
        }

        Assert.assertTrue(writtenBytes < 6000)
        System.out.printf("# small %d bytes\n", writtenBytes)

        dist2 = fromBytes(bufsmall.toBinaryInput())
        bufsmall.release()
        Assert.assertEquals(dist.centroids().size.toLong(), dist2.centroids().size.toLong())
        Assert.assertEquals(dist.compression(), dist2.compression(), 1e-4)
        Assert.assertEquals(dist.size(), dist2.size())

        var q = 0.0
        while (q < 1) {
            Assert.assertEquals(dist.quantile(q), dist2.quantile(q), 1e-6)
            q += 0.01
        }

        ix = dist2.centroids().iterator()
        for (centroid in dist.centroids()) {
            Assert.assertTrue(ix.hasNext())
            Assert.assertEquals(centroid.count().toLong(), ix.next().count().toLong())
        }
        Assert.assertFalse(ix.hasNext())
    }

    /**
     * Does basic sanity testing for a particular small example that used to fail. See
     * https://github.com/addthis/stream-lib/issues/138
     */
    @Test
    fun testThreePointExample() {
        val tdigest = factory(100.0).create()
        val x0 = 0.18615591526031494
        val x1 = 0.4241943657398224
        val x2 = 0.8813006281852722

        tdigest.add(x0)
        tdigest.add(x1)
        tdigest.add(x2)

        val p10 = tdigest.quantile(0.1)
        val p50 = tdigest.quantile(0.5)
        val p90 = tdigest.quantile(0.9)
        val p95 = tdigest.quantile(0.95)
        val p99 = tdigest.quantile(0.99)

        Assert.assertTrue("ordering of quantiles", p10 <= p50)
        Assert.assertTrue("ordering of quantiles", p50 <= p90)
        Assert.assertTrue("ordering of quantiles", p90 <= p95)
        Assert.assertTrue("ordering of quantiles", p95 <= p99)

        Assert.assertEquals("Extreme quantiles", x0, p10, 0.0)
        Assert.assertEquals("Extreme quantiles", x2, p99, 0.0)

        //        System.out.println("digest: " + tdigest.getClass());
        //        System.out.println("p10: " + tdigest.quantile(0.1));
        //        System.out.println("p50: " + tdigest.quantile(0.5));
        //        System.out.println("p90: " + tdigest.quantile(0.9));
        //        System.out.println("p95: " + tdigest.quantile(0.95));
        //        System.out.println("p99: " + tdigest.quantile(0.99));
        //        System.out.println();
    }

    @Test
    open fun testSingletonInACrowd() {
        val compression = 100.0
        val dist = factory(compression).create()
        for (i in 0..9999) {
            dist.add(10.0)
        }
        dist.add(20.0)
        dist.compress()
        Assert.assertEquals(10.0, dist.quantile(0.0), 0.0)
        Assert.assertEquals(10.0, dist.quantile(0.5), 0.0)
        Assert.assertEquals(10.0, dist.quantile(0.8), 0.0)
        Assert.assertEquals(10.0, dist.quantile(0.9), 0.0)
        Assert.assertEquals(10.0, dist.quantile(0.99), 0.0)
        Assert.assertEquals(10.0, dist.quantile(0.999), 0.0)
        Assert.assertEquals(20.0, dist.quantile(1.0), 0.0)
    }

    @Test
    fun testIntEncoding() {
        val gen = RandomizedTest.getRandom()
//        val buf = ByteBuffer.allocate(10000)
        val ref = Lists.newArrayList<Int>()
        val buf= buildPacket {
            for (i in 0..2999) {
                var n = gen.nextInt()
                n = n.ushr(i / 100)
                ref.add(n)
                AbstractTDigest.encode(this.toBinaryOutput(), n)
            }
        }


        try {
            for (i in 0..2999) {
                val n = AbstractTDigest.decode(buf.toBinaryInput())
                Assert.assertEquals(String.format("%d:", i), ref[i].toInt().toLong(), n.toLong())
            }
        } catch (e:Throwable) {
            buf.release()
            throw e
        }
    }

    @Test
    @Throws(FileNotFoundException::class)
    fun compareToQDigest() {
        val rand = RandomizedTest.getRandom()
        PrintWriter(FileOutputStream("qd-tree-comparison.csv")).use { out ->
            for (i in 0 until repeats()) {
                compareQD(out, Gamma(0.1, 0.1, rand), "gamma", 1L shl 48)
                compareQD(out, Uniform(0.0, 1.0, rand), "uniform", 1L shl 48)
            }
        }
    }

    private fun compareQD(out: PrintWriter, gen: AbstractContinousDistribution, tag: String, scale: Long) {
        for (compression in doubleArrayOf(10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1000.0, 2000.0)) {
            val qd = QDigest(compression)
            val dist = factory(compression).create()
            val data = Lists.newArrayList<Double>()
            for (i in 0..99999) {
                val x = gen.nextDouble()
                dist.add(x)
                qd.offer((x * scale).toLong())
                data.add(x)
            }
            dist.compress()
            Collections.sort(data)

            for (q in doubleArrayOf(0.001, 0.01, 0.1, 0.2, 0.3, 0.5, 0.7, 0.8, 0.9, 0.99, 0.999)) {
                val x1 = dist.quantile(q)
                val x2 = qd.getQuantile(q).toDouble() / scale
                val e1 = cdf(x1, data) - q
                out.printf(
                    "%s\t%.0f\t%.8f\t%.10g\t%.10g\t%d\t%d\n",
                    tag,
                    compression,
                    q,
                    e1,
                    cdf(x2, data) - q,
                    dist.smallByteSize(),
                    QDigest.serialize(qd).size
                )

            }
        }
    }

    @Test
    @Throws(FileNotFoundException::class)
    fun compareToStreamingQuantile() {
        val rand = RandomizedTest.getRandom()

        PrintWriter(FileOutputStream("sq-tree-comparison.csv")).use { out ->
            for (i in 0 until repeats()) {
                compareSQ(out, Gamma(0.1, 0.1, rand), "gamma", 1L shl 48)
                compareSQ(out, Uniform(0.0, 1.0, rand), "uniform", 1L shl 48)
            }
        }
    }

    private fun compareSQ(out: PrintWriter, gen: AbstractContinousDistribution, tag: String, scale: Long) {
        val quantiles = doubleArrayOf(0.001, 0.01, 0.1, 0.2, 0.3, 0.5, 0.7, 0.8, 0.9, 0.99, 0.999)
        for (compression in doubleArrayOf(10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1000.0, 2000.0)) {
            val sq = QuantileEstimator(1001)
            val dist = factory(compression).create()
            val data = Lists.newArrayList<Double>()
            for (i in 0..99999) {
                val x = gen.nextDouble()
                dist.add(x)
                sq.add(x)
                data.add(x)
            }
            dist.compress()
            Collections.sort(data)

            val qz = sq.quantiles
            for (q in quantiles) {
                val x1 = dist.quantile(q)
                val x2 = qz[(q * 1000 + 0.5).toInt()]
                val e1 = cdf(x1, data) - q
                val e2 = cdf(x2, data) - q
                out.printf(
                    "%s\t%.0f\t%.8f\t%.10g\t%.10g\t%d\t%d\n",
                    tag, compression, q, e1, e2, dist.smallByteSize(), sq.serializedSize()
                )

            }
        }
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testSizeControl() {
        // very slow running data generator.  Don't want to run this normally.  To run slow tests use
        // mvn test -DrunSlowTests=true
        //        assumeTrue(Boolean.parseBoolean(System.getProperty("runSlowTests")));
        val live = AtomicInteger(0)
        val pending = AtomicInteger(0)

        val gen0 = RandomizedTest.getRandom()
        PrintWriter(FileOutputStream(String.format("scaling-%s.tsv", digestName))).use { out ->
            out.printf("k\tsamples\tcompression\tcentroids\tsize1\tsize2\n")

            val tasks = Lists.newArrayList<Callable<String>>()
            for (k in 0..4) {
                for (size in intArrayOf(10, 100, 1000, 10000)) {
                    tasks.add(object : Callable<String> {
                        internal val gen = Random(gen0.nextLong())

                        override fun call(): String {
                            System.out.printf("Starting %d,%d\n", k, size)
                            live.incrementAndGet()
                            try {
                                val s = StringWriter()
                                val out = PrintWriter(s)
                                for (compression in doubleArrayOf(50.0, 100.0, 200.0, 500.0)) {
                                    try {
                                        val dist = factory(compression).create()
                                        for (i in 0 until size * 1000) {
                                            dist.add(gen.nextDouble())
                                        }
                                        dist.compress()
                                        out.printf(
                                            "%d\t%d\t%.0f\t%d\t%d\t%d\n",
                                            k,
                                            size,
                                            compression,
                                            dist.centroidCount(),
                                            dist.smallByteSize(),
                                            dist.byteSize()
                                        )
                                        out.flush()
                                    } catch (e: Throwable) {
                                        System.out.printf(
                                            "                         Exception %s, %d, %.0f, %d\n",
                                            e.toString(),
                                            k,
                                            compression,
                                            size
                                        )
                                        throw e
                                    }

                                }
                                out.close()
                                return s.toString()
                            } finally {
                                live.decrementAndGet()
                                pending.decrementAndGet()
                                System.out.printf(
                                    "                   %d,%d (%d live threads, %d pending tasks)\n",
                                    k,
                                    size,
                                    live.get(),
                                    pending.get()
                                )
                            }
                        }
                    })
                }
            }
            pending.set(tasks.size)

            val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2)
            for (result in executor.invokeAll(tasks)) {
                try {
                    out.write(result.get())
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

            }
            executor.shutdownNow()
            Assert.assertTrue("Dangling executor thread", executor.awaitTermination(5, TimeUnit.SECONDS))
        }
    }

    @Test
    @Throws(FileNotFoundException::class)
    fun testScaling() {
        val gen = RandomizedTest.getRandom()

        PrintWriter(FileOutputStream(String.format("error-scaling-%s.tsv", digestName))).use { out ->
            out.printf("pass\tcompression\tq\terror\tsize\n")

            val tasks = Lists.newArrayList<Callable<String>>()
            for (k in 0..9) {
                val data = Lists.newArrayList<Double>()
                for (i in 0..99999) {
                    data.add(gen.nextDouble())
                }
                Collections.sort(data)

                for (compression in doubleArrayOf(10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1000.0)) {
                    val dist = factory(compression).create()
                    for (x in data) {
                        dist.add(x!!)
                    }
                    dist.compress()

                    for (q in doubleArrayOf(0.001, 0.01, 0.1, 0.5)) {
                        val estimate = dist.quantile(q)
                        val actual = data[(q * data.size).toInt()]
                        out.printf("%d\t%.0f\t%.3f\t%.9f\t%d\n", k, compression, q, estimate - actual, dist.byteSize())
                        out.flush()
                    }
                }
            }
        }
    }


    @Test
    fun testMonotonicity() {
        val digest = factory().create()
        val gen = RandomizedTest.getRandom()
        for (i in 0..99999) {
            digest.add(gen.nextDouble())
        }

        var lastQuantile = -1.0
        var lastX = -1.0
        var z = 0.0
        while (z <= 1) {
            val x = digest.quantile(z)
            Assert.assertTrue(x >= lastX)
            lastX = x

            val q = digest.cdf(z)
            Assert.assertTrue(q >= lastQuantile)
            lastQuantile = q
            z += 1e-5
        }
    }

    companion object {
        private val lock = 3
        private var sizeDump: PrintWriter? = null
        private var errorDump: PrintWriter? = null
        private var deviationDump: PrintWriter? = null

        private var digestName: String? = null

        @BeforeClass
        fun freezeSeed() {
            RandomUtils.useTestSeed()
        }

        @Throws(IOException::class)
        fun setup(digestName: String) {
            TDigestTest.digestName = digestName
            synchronized(lock) {
                sizeDump = PrintWriter(FileWriter("sizes-$digestName.csv"))
                sizeDump!!.printf("tag\ti\tq\tk\tactual\n")

                errorDump = PrintWriter(FileWriter("errors-$digestName.csv"))
                errorDump!!.printf("dist\ttag\tx\tQ\terror\n")

                deviationDump = PrintWriter(FileWriter("deviation-$digestName.csv"))
                deviationDump!!.printf("tag\tQ\tk\tx\tmean\tleft\tright\tdeviation\n")
            }
        }

        @AfterClass
        fun teardown() {
            if (sizeDump != null) {
                sizeDump!!.close()
            }
            if (errorDump != null) {
                errorDump!!.close()
            }
            if (deviationDump != null) {
                deviationDump!!.close()
            }
        }
    }

    //    @Test
    //    public void testKSDrift() {
    //        final Random gen = getRandom();
    //        int N1 = 50;
    //        int N2 = 10000;
    //        double[] data = new double[N1 * N2];
    //        System.out.printf("rep,i,ks,class\n");
    //        for (int rep = 0; rep < 5; rep++) {
    //            TDigest digest = factory(200).create();
    //            for (int i = 0; i < N1; i++) {
    //                for (int j = 0; j < N2; j++) {
    //                    double x = gen.nextDouble();
    //                    data[i * N2 + j] = x;
    //                    digest.add(x);
    //                }
    //                System.out.printf("%d,%d,%.7f,%s,%d\n", rep, i, ks(data, (i + 1) * N2, digest), digest.getClass().getSimpleName(), digest.centroidCount());
    //            }
    //        }
    //    }

    //    private double ks(double[] data, int length, TDigest digest) {
    //        double d1 = 0;
    //        double d2 = 0;
    //        Arrays.sort(data, 0, length);
    //        int i = 0;
    //        for (Centroid centroid : digest.centroids()) {
    //            double x = centroid.mean();
    //            while (i < length && data[i] <= x) {
    //                i++;
    //            }
    //            double q0a = (double) i / (length - 1);
    //            double q0b = (double) (i + 1) / (length - 1);
    //            double q0;
    //            if (i > 0) {
    //                if (i < length) {
    //                    q0 = (q0a * (data[i] - x) + q0b * (x - data[i - 1])) / (data[i] - data[i - 1]);
    //                } else {
    //                    q0 = 1;
    //                }
    //            } else {
    //                q0 = 0;
    //            }
    //            double q1 = digest.cdf(x);
    //            d1 = Math.max(q1 - q0, d1);
    //            d2 = Math.max(q0 - q1, d2);
    //        }
    //        return Math.max(d1, d2);
    //    }
}
