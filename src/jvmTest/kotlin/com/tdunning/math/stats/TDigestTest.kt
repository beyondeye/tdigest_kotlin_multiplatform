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

import com.carrotsearch.randomizedtesting.RandomizedTest
import com.clearspring.analytics.stream.quantile.QDigest
import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import kotlinx.io.core.Input
import kotlinx.io.core.buildPacket
import org.apache.mahout.common.RandomUtils
import org.apache.mahout.math.jet.random.AbstractContinousDistribution
import org.apache.mahout.math.jet.random.Gamma
import org.apache.mahout.math.jet.random.Normal
import org.apache.mahout.math.jet.random.Uniform
import org.junit.*

import java.io.*
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.*


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

    protected abstract fun fromBytes(bytes: Input): TDigest

    @Throws(FileNotFoundException::class, InterruptedException::class, ExecutionException::class)
    private fun merge(factory: DigestFactory) {
        val gen0 = RandomizedTest.getRandom()
        val out = PrintWriter(File("merge.tsv"))
        out.printf("type\tparts\tq\te0\te1\te2\te2.rel\n")

        val tasks = Lists.newArrayList<Callable<String>>()
        for (k in 0 until repeats()) {
            val currentK = k
            tasks.add(object : Callable<String> {
                val gen = Random(gen0.nextLong())

                @Throws(Exception::class)
                override fun call(): String {
                    val s = StringWriter()
                    val out = PrintWriter(s)

                    for (parts in intArrayOf(2, 5, 10, 20, 50, 100)) {
                        val data = Lists.newArrayList<Double>()

                        val dist = factory.create()
                        dist.recordAllData()

                        // we accumulate the data into multiple sub-digests
                        val subs = Lists.newArrayList<TDigest>()
                        for (i in 0 until parts) {
                            subs.add(factory.create().recordAllData())
                        }

                        val cnt = IntArray(parts)
                        for (i in 0..99999) {
                            val x = gen.nextDouble()
                            data.add(x)
                            dist.add(x)
                            subs[i % parts].add(x)
                            cnt[i % parts]++
                        }
                        dist.compress()
                        Collections.sort(data)

                        // collect the raw data from the sub-digests
                        val data2 = Lists.newArrayList<Double>()
                        var i = 0
                        var k = 0
                        for (digest in subs) {
                            Assert.assertEquals("Sub-digest size check", cnt[i].toLong(), digest.size())
                            var k2 = 0
                            for (centroid in digest.centroids()) {
                                Iterables.addAll(data2, centroid.data()!!)
                                Assert.assertEquals(
                                    "Centroid consistency",
                                    centroid.count().toLong(),
                                    centroid.data()!!.size.toLong()
                                )
                                k2 += centroid.data()!!.size
                            }
                            k += k2
                            Assert.assertEquals("Sub-digest centroid sum check", cnt[i].toLong(), k2.toLong())
                            i++
                        }
                        Assert.assertEquals(
                            "Sub-digests don't add up to the right size",
                            data.size.toLong(),
                            k.toLong()
                        )

                        // verify that the raw data all got recorded
                        Collections.sort(data2)
                        Assert.assertEquals(data.size.toLong(), data2.size.toLong())
                        var ix = data.iterator()
                        for (x in data2) {
                            Assert.assertEquals(ix.next(), x,0.0)
                        }

                        // now merge the sub-digests
                        val dist2 = factory.create().recordAllData()
                        dist2.add(subs)

                        // verify the merged result has the right data
                        val data3 = Lists.newArrayList<Double>()
                        for (centroid in dist2.centroids()) {
                            Iterables.addAll(data3, centroid.data()!!)
                        }
                        Collections.sort(data3)
                        Assert.assertEquals(data.size.toLong(), data3.size.toLong())
                        ix = data.iterator()
                        for (x in data3) {
                            Assert.assertEquals(ix.next(), x,0.0)
                        }

                        if (dist is MergingDigest) {
                            dist.checkWeights()
                            (dist2 as MergingDigest).checkWeights()
                            for (sub in subs) {
                                (sub as MergingDigest).checkWeights()
                            }
                        }

                        for (q in doubleArrayOf(0.001, 0.01, 0.1, 0.2, 0.3, 0.5)) {
                            val z = quantile(q, data)
                            val e1 = dist.quantile(q) - z
                            val e2 = dist2.quantile(q) - z
                            out.printf(
                                "quantile\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t%.6f\n",
                                parts,
                                q,
                                z - q,
                                e1,
                                e2,
                                Math.abs(e2) / q
                            )
                            Assert.assertTrue(
                                String.format(
                                    "Relative error: parts=%d, q=%.4f, e1=%.5f, e2=%.5f, rel=%.4f",
                                    parts,
                                    q,
                                    e1,
                                    e2,
                                    Math.abs(e2) / q
                                ), Math.abs(e2) / q < 0.3
                            )
                            Assert.assertTrue(
                                String.format(
                                    "Absolute error: parts=%d, q=%.4f, e1=%.5f, e2=%.5f, rel=%.4f",
                                    parts,
                                    q,
                                    e1,
                                    e2,
                                    Math.abs(e2) / q
                                ), Math.abs(e2) < 0.015
                            )
                        }

                        for (x in doubleArrayOf(0.001, 0.01, 0.1, 0.2, 0.3, 0.5)) {
                            val z = cdf(x, data)
                            val e1 = dist.cdf(x) - z
                            val e2 = dist2.cdf(x) - z

                            out.printf(
                                "cdf\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t%.6f\n",
                                parts,
                                x,
                                z - x,
                                e1,
                                e2,
                                Math.abs(e2) / x
                            )
                            Assert.assertTrue(
                                String.format(
                                    "Absolute cdf: parts=%d, x=%.4f, e1=%.5f, e2=%.5f",
                                    parts,
                                    x,
                                    e1,
                                    e2
                                ), Math.abs(e2) < 0.015
                            )
                            Assert.assertTrue(
                                String.format(
                                    "Relative cdf: parts=%d, x=%.4f, e1=%.5f, e2=%.5f, rel=%.3f",
                                    parts,
                                    x,
                                    e1,
                                    e2,
                                    Math.abs(e2) / x
                                ), Math.abs(e2) / x < 0.3
                            )
                        }
                        out.flush()
                    }
                    System.out.printf("Iteration %d\n", currentK + 1)
                    out.close()
                    return s.toString()
                }
            })
        }

        val executor = Executors.newFixedThreadPool(20)
        try {
            for (result in executor.invokeAll(tasks)) {
                out.write(result.get())
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        } finally {
            executor.shutdownNow()
            executor.awaitTermination(5, TimeUnit.SECONDS)
            out.close()
        }

    }

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
            val q1 = quantile(q, values)
            val q2 = digest.quantile(q)
            Assert.assertEquals(String.format("At q=%g, expected %.2f vs %.2f", q, q1, q2), q1, q2, 0.03)
        }
    }

    private fun cdf(x: Double, data: List<Double>): Double {
        var n1 = 0
        var n2 = 0
        for (v in data) {
            n1 += if (v < x) 1 else 0
            n2 += if (v <= x) 1 else 0
        }
        return (n1 + n2).toDouble() / 2.0 / data.size.toDouble()
    }

    private fun quantile(q: Double, data: List<Double>): Double {
        if (data.size == 0) {
            return java.lang.Double.NaN
        }
        if (q == 1.0 || data.size == 1) {
            return data[data.size - 1]
        }
        var index = q * data.size
        if (index < 0.5) {
            return data[0]
        } else if (data.size - index < 0.5) {
            return data[data.size - 1]
        } else {
            index -= 0.5
            val intIndex = index.toInt()
            return data[intIndex + 1] * (index - intIndex) + data[intIndex] * (intIndex + 1 - index)
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
     * Builds estimates of the CDF of a bunch of data points and checks that the centroids are accurately
     * positioned.  Accuracy is assessed in terms of the estimated CDF which is much more stringent than
     * checking position of quantiles with a single value for desired accuracy.
     *
     * @param gen           Random number generator that generates desired values.
     * @param sizeGuide     Control for size of the histogram.
     * @param tag           Label for the output lines
     * @param recordAllData True if the internal histogrammer should be set up to record all data it sees for
     */
    private fun runTest(
        factory: DigestFactory,
        gen: AbstractContinousDistribution,
        sizeGuide: Double,
        qValues: DoubleArray,
        tag: String,
        recordAllData: Boolean
    ) {
        val dist = factory.create()
        if (recordAllData) {
            dist.recordAllData()
        }

        val data = Lists.newArrayList<Double>()
        for (i in 0..99999) {
            val x = gen.nextDouble()
            data.add(x)
        }
        val t0 = System.nanoTime()
        var sumW = 0
        for (x in data) {
            dist.add(x)
            sumW++
        }
        System.out.printf("# %fus per point\n", (System.nanoTime() - t0) * 1e-3 / 100000)
        System.out.printf("# %d centroids\n", dist.centroids().size)
        Collections.sort(data)

        val xValues = qValues.clone()
        for (i in qValues.indices) {
            val ix = data.size * qValues[i] - 0.5
            val index = Math.floor(ix).toInt()
            val p = ix - index
            xValues[i] = data[index] * (1 - p) + data[index + 1] * p
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
        Assert.assertEquals(qz, dist.size().toDouble(), 1e-10)
        Assert.assertEquals(iz.toLong(), dist.centroids().size.toLong())

        Assert.assertTrue(
            String.format(
                "Summary is too large (got %d, wanted < %.1f)",
                dist.centroids().size,
                20 * sizeGuide
            ), dist.centroids().size < 20 * sizeGuide
        )
        var softErrors = 0
        for (i in xValues.indices) {
            val x = xValues[i]
            val q = qValues[i]
            var estimate = dist.cdf(x)
            errorDump?.printf("%s\t%s\t%.8g\t%.8f\t%.8f\n", tag, "cdf", x, q, estimate - q)
            Assert.assertEquals(q, estimate, 0.005)

            estimate = cdf(dist.quantile(q), data)
            errorDump?.printf("%s\t%s\t%.8g\t%.8f\t%.8f\n", tag, "quantile", x, q, estimate - q)
            if (Math.abs(q - estimate) > 0.005) {
                softErrors++
            }
            Assert.assertEquals(q, estimate, 0.012)
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
        val digest = factory().create()
        val gen = RandomizedTest.getRandom()
        for (i in 0..999) {
            val next = gen.nextDouble()
            digest.add(next)
        }
        for (i in 0..9) {
            val next = gen.nextDouble()
            val count = 1 shl 28
            digest.add(next, count)
        }
        Assert.assertEquals(1000 + 10L * (1 shl 28), digest.size())
        Assert.assertTrue(digest.size() > Integer.MAX_VALUE)
        val quantiles = doubleArrayOf(0.0, 0.1, 0.5, 0.9, 1.0, gen.nextDouble())
        Arrays.sort(quantiles)
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
            digest.add(gen.nextDouble(), 1 + gen.nextInt(10))
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
                factory(), Uniform(0.0, 1.0, gen), 100.0,
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
                factory(), Gamma(0.1, 0.1, gen), 100.0,
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
                factory(),
                mix,
                100.0,
                doubleArrayOf(0.001, 0.01, 0.1, 0.3, 0.5, 0.7, 0.9, 0.99, 0.999),
                "mixture",
                false
            )
        }
    }

    @Test
    fun testRepeatedValues() {
        val gen = RandomizedTest.getRandom()

        // 5% of samples will be 0 or 1.0.  10% for each of the values 0.1 through 0.9
        val mix = object : AbstractContinousDistribution() {
            override fun nextDouble(): Double {
                return Math.rint(gen.nextDouble() * 10) / 10.0
            }
        }

        val dist = factory(1000.0).create()
        val data = Lists.newArrayList<Double>()
        for (i1 in 0..99999) {
            val x = mix.nextDouble()
            data.add(x)
        }

        val t0 = System.nanoTime()
        for (x in data) {
            dist.add(x)
        }

        System.out.printf("# %fus per point\n", (System.nanoTime() - t0) * 1e-3 / 100000)
        System.out.printf("# %d centroids\n", dist.centroids().size)

        // I would be happier with 5x compression, but repeated values make things kind of weird
        Assert.assertTrue(
            "Summary is too large: " + dist.centroids().size,
            dist.centroids().size < 10 * 1000.toDouble()
        )

        // all quantiles should round to nearest actual value
        for (i in 0..9) {
            val z = i / 10.0
            // we skip over troublesome points that are nearly halfway between
            for (delta in doubleArrayOf(0.01, 0.02, 0.03, 0.07, 0.08, 0.09)) {
                val q = z + delta
                val cdf = dist.cdf(q)
                // we also relax the tolerances for repeated values
                Assert.assertEquals(String.format("z=%.1f, q = %.3f, cdf = %.3f", z, q, cdf), z + 0.05, cdf, 0.01)

                val estimate = dist.quantile(q)
                Assert.assertEquals(
                    String.format("z=%.1f, q = %.3f, cdf = %.3f, estimate = %.3f", z, q, cdf, estimate),
                    Math.rint(q * 10) / 10.0,
                    estimate,
                    0.001
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
                }, 100.0, doubleArrayOf(0.001, 0.01, 0.1, 0.5, 0.9, 0.99, 0.999),
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
            dist.asBytes(this)
            writtenBytes=this.size
        }
//       writtenBytes=buf.remaining
        Assert.assertTrue(String.format("size is %d\n", writtenBytes), writtenBytes < 12000)
        Assert.assertEquals(dist.byteSize().toLong(), writtenBytes.toLong())

        System.out.printf("# big %d bytes\n", writtenBytes)

        var dist2 = fromBytes(buf)
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
            dist.asSmallBytes(this)
            writtenBytes=this.size
        }

        Assert.assertTrue(writtenBytes < 6000)
        System.out.printf("# small %d bytes\n", writtenBytes)

        dist2 = fromBytes(bufsmall)
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
     * Does basic sanity testing for a particular small example that used to fail.
     * See https://github.com/addthis/stream-lib/issues/138
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
                AbstractTDigest.encode(this, n)
            }
        }


        try {
            for (i in 0..2999) {
                val n = AbstractTDigest.decode(buf)
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
    @Throws(IOException::class, InterruptedException::class, ExecutionException::class)
    fun testSizeControl() {
        // very slow running data generator.  Don't want to run this normally.  To run slow tests use
        // mvn test -DrunSlowTests=true
        RandomizedTest.assumeTrue(java.lang.Boolean.parseBoolean(System.getProperty("runSlowTests")))

        val gen0 = RandomizedTest.getRandom()
        val out = PrintWriter(FileOutputStream("scaling.tsv"))
        out.printf("k\tsamples\tcompression\tsize1\tsize2\n")

        val tasks = Lists.newArrayList<Callable<String>>()
        for (k in 0..19) {
            for (size in intArrayOf(10, 100, 1000, 10000)) {
                tasks.add(object : Callable<String> {
                    val gen = Random(gen0.nextLong())

                    @Throws(Exception::class)
                    override fun call(): String {
                        System.out.printf("Starting %d,%d\n", k, size)
                        val s = StringWriter()
                        val out = PrintWriter(s)
                        for (compression in doubleArrayOf(2.0, 5.0, 10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1000.0)) {
                            val dist = factory(compression).create()
                            for (i in 0 until size * 1000) {
                                dist.add(gen.nextDouble())
                            }
                            out.printf(
                                "%d\t%d\t%.0f\t%d\t%d\n",
                                k,
                                size,
                                compression,
                                dist.smallByteSize(),
                                dist.byteSize()
                            )
                            out.flush()
                        }
                        out.close()
                        return s.toString()
                    }
                })
            }
        }

        val executor = Executors.newFixedThreadPool(20)
        for (result in executor.invokeAll(tasks)) {
            out.write(result.get())
        }
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)

        out.close()
    }

    @Test
    @Throws(FileNotFoundException::class, InterruptedException::class, ExecutionException::class)
    fun testScaling() {
        val gen0 = RandomizedTest.getRandom()

        PrintWriter(FileOutputStream("error-scaling.tsv")).use { out ->
            out.printf("pass\tcompression\tq\terror\tsize\n")

            val tasks = Lists.newArrayList<Callable<String>>()
            val n = Math.max(3, repeats() * repeats())
            for (k in 0 until n) {
                tasks.add(object : Callable<String> {
                    val gen = Random(gen0.nextLong())

                    @Throws(Exception::class)
                    override fun call(): String {
                        System.out.printf("Start %d\n", k)
                        val s = StringWriter()
                        val out = PrintWriter(s)

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
                                out.printf(
                                    "%d\t%.0f\t%.3f\t%.9f\t%d\n",
                                    k,
                                    compression,
                                    q,
                                    estimate - actual,
                                    dist.byteSize()
                                )
                                out.flush()
                            }
                        }
                        out.close()
                        System.out.printf("Finish %d\n", k)

                        return s.toString()
                    }
                })
            }

            val exec = Executors.newFixedThreadPool(16)
            try {
                for (result in exec.invokeAll(tasks)) {
                    out.write(result.get())
                }
                exec.shutdown()
                if (exec.awaitTermination(5, TimeUnit.SECONDS)) {
                    return
                }

            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            exec.shutdownNow()
            Assert.assertTrue("Dangling executor thread", exec.awaitTermination(5, TimeUnit.SECONDS))
        }
    }

    @Test
    @Throws(FileNotFoundException::class, InterruptedException::class, ExecutionException::class)
    fun testMerge() {
        merge(factory())
    }

    @Test
    fun testExtremeQuantiles() {
        // t-digest shouldn't merge extreme nodes, but let's still test how it would
        // answer to extreme quantiles in that case ('extreme' in the sense that the
        // quantile is either before the first node or after the last one)
        val digest = factory().create()
        digest.add(10.0, 3)
        digest.add(20.0, 1)
        digest.add(40.0, 5)
        // this group tree is roughly equivalent to the following sorted array:
        // [ ?, 10, ?, 20, ?, ?, 50, ?, ? ]
        // and we expect it to compute approximate missing values:
        // [ 5, 10, 15, 20, 30, 40, 50, 60, 70]
        val values = Arrays.asList(5.0, 10.0, 15.0, 20.0, 30.0, 35.0, 40.0, 45.0, 50.0)
        for (q in doubleArrayOf(1.5 / 9, 3.5 / 9, 6.5 / 9)) {
            Assert.assertEquals(String.format("q=%.2f ", q), quantile(q, values), digest.quantile(q), 0.01)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testMontonicity() {
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

    private fun ks(data: DoubleArray, length: Int, digest: TDigest): Double {
        var d1 = 0.0
        var d2 = 0.0
        Arrays.sort(data, 0, length)
        var i = 0
        for (centroid in digest.centroids()) {
            val x = centroid.mean()
            while (i < length && data[i] <= x) {
                i++
            }
            val q0a = i.toDouble() / (length - 1)
            val q0b = (i + 1).toDouble() / (length - 1)
            val q0: Double
            if (i > 0) {
                if (i < length) {
                    q0 = (q0a * (data[i] - x) + q0b * (x - data[i - 1])) / (data[i] - data[i - 1])
                } else {
                    q0 = 1.0
                }
            } else {
                q0 = 0.0
            }
            val q1 = digest.cdf(x)
            d1 = Math.max(q1 - q0, d1)
            d2 = Math.max(q0 - q1, d2)
        }
        return Math.max(d1, d2)
    }

    companion object {
        private val lock = 3
        private var sizeDump: PrintWriter? = null
        private var errorDump: PrintWriter? = null
        private var deviationDump: PrintWriter? = null

        @BeforeClass
        fun freezeSeed() {
            RandomUtils.useTestSeed()
        }

        @Throws(IOException::class)
        fun setup(digestName: String) {
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

        private fun merge(subData: Iterable<TDigest>, gen: Random, r: TDigest): TDigest {
            val centroids = ArrayList<Centroid>()
            var recordAll = false
            for (digest in subData) {
                for (centroid in digest.centroids()) {
                    centroids.add(centroid)
                }
                recordAll = recordAll or digest.isRecording
            }
            Collections.shuffle(centroids, gen)
            if (recordAll) {
                r.recordAllData()
            }

            for (c in centroids) {

                if (r.isRecording) {
                    // TODO should do something better here.
                }
                (r as AbstractTDigest).add(c.mean(), c.count(), c)
            }
            return r
        }
    }
}
