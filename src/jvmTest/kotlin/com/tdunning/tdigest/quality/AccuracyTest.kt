package com.tdunning.tdigest.quality

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import com.tdunning.math.stats.Dist
import com.tdunning.math.stats.MergingDigest
import com.tdunning.math.stats.ScaleFunction
import com.tdunning.math.stats.TDigest
import com.tdunning.tdigest.quality.Util.Distribution
import org.junit.Assert
import org.junit.Test
import java.io.*
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Produce measurements of accuracy versus compression factor for fixed data size
 */
class AccuracyTest {
    private val gen = Random()

    /**
     * Generates information that demonstrates that t-digests can be merged without major loss of
     * accuracy.
     */
    @Test
    fun merge() {
        val seedGenerator = Random()
        try {
            PrintWriter(File("merge.csv")).use { out ->
                out.printf("type,parts,q,e0,e1,e2,e2.rel,e3\n")
                val tasks: MutableList<Callable<String>> = Lists.newArrayList()
                for (k in 0..19) {
                    val currentK = k
                    tasks.add(object : Callable<String> {
                        val gen = Random(seedGenerator.nextLong())
                        override fun call(): String {
                            val s = StringWriter()
                            val out = PrintWriter(s)
                            System.out.printf("Starting %d\n", currentK)
                            for (parts in intArrayOf(2, 5, 10, 20, 50, 100)) {
                                val data = Lists.newArrayList<Double>()
                                val dist: TDigest = MergingDigest(100.0)
                                dist.recordAllData()

                                // we accumulate the data into multiple sub-digests
                                val subs: MutableList<TDigest> =
                                    Lists.newArrayList()
                                for (i in 0 until parts) {
                                    subs.add(MergingDigest(100.0).recordAllData())
                                }
                                val highRes: MutableList<TDigest> =
                                    Lists.newArrayList()
                                for (i in 0 until parts) {
                                    highRes.add(MergingDigest(200.0))
                                }
                                val cnt = IntArray(parts)
                                for (i in 0..99999) {
                                    val x = gen.nextDouble()
                                    data.add(x)
                                    dist.add(x)
                                    subs[i % parts].add(x)
                                    highRes[i % parts].add(x)
                                    cnt[i % parts]++
                                }
                                dist.compress()
                                Collections.sort(data)

                                // collect the raw data from the sub-digests
                                val data2: List<Double> = Lists.newArrayList()
                                var i = 0
                                var k = 0
                                var totalByCount = 0
                                for (digest in subs) {
                                    Assert.assertEquals("Sub-digest size check", cnt[i], digest.size().toInt())
                                    var k2 = 0
                                    for (centroid in digest.centroids()) {
                                        Iterables.addAll(data2, centroid.data())
                                        Assert.assertEquals(
                                            "Centroid consistency",
                                            centroid.count().toLong(),
                                            centroid.data()!!.size.toLong()
                                        )
                                        k2 += centroid.data()!!.size
                                    }
                                    totalByCount += cnt[i]
                                    k += k2
                                    Assert.assertEquals(
                                        "Sub-digest centroid sum check",
                                        cnt[i],
                                        k2
                                    )
                                    Assert.assertEquals(
                                        "Sub-digest centroid sum check",
                                        cnt[i],
                                        subs[i].size().toInt()
                                    )
                                    i++
                                }
                                Assert.assertEquals(
                                    "Sub-digests don't add up to the right size",
                                    data.size.toLong(),
                                    k.toLong()
                                )
                                Assert.assertEquals(
                                    "Counts don't match up",
                                    data.size.toLong(),
                                    totalByCount.toLong()
                                )

                                // verify that the raw data all got recorded
                                Collections.sort(data2)
                                Assert.assertEquals(data.size.toLong(), data2.size.toLong())
                                var ix: Iterator<Double?> = data.iterator()
                                for (x in data2) {
                                    Assert.assertEquals(ix.next(), x)
                                }

                                // now merge the sub-digests
                                val dist2 = MergingDigest(100.0).recordAllData()
                                dist2.add(subs)
                                Assert.assertEquals(
                                    String.format(
                                        "Digest count is wrong %d vs %d",
                                        totalByCount,
                                        dist2.size()
                                    ), totalByCount.toLong(), dist2.size()
                                )

                                // verify the merged result has the right data
                                val data3: List<Double> = Lists.newArrayList()
                                for (centroid in dist2.centroids()) {
                                    Iterables.addAll(data3, centroid.data())
                                }
                                Collections.sort(data3)
                                Assert.assertEquals(
                                    String.format(
                                        "Total data size %d vs %d",
                                        data.size,
                                        data3.size
                                    ), data.size.toLong(), data3.size.toLong()
                                )
                                ix = data.iterator()
                                for (x in data3) {
                                    Assert.assertEquals(ix.next(), x)
                                }
                                val dist3: TDigest = MergingDigest(100.0)
                                dist3.add(highRes)
                                val allData = DoubleArray(data.size)
                                var iz = 0
                                for (x in data) {
                                    allData[iz++] = x
                                }
                                for (q in doubleArrayOf(0.001, 0.01, 0.1, 0.2, 0.3, 0.5)) {
                                    val z = Dist.quantile(q, allData)
                                    val e1 = dist.quantile(q) - z
                                    val e2 = dist2.quantile(q) - z
                                    val e2Relative = Math.abs(e2) / q
                                    val e3 = dist3.quantile(q) - z
                                    out.printf(
                                        "quantile,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f\n",
                                        parts,
                                        q,
                                        z - q,
                                        e1,
                                        e2,
                                        e2Relative,
                                        e3
                                    )
                                    Assert.assertTrue(
                                        String.format(
                                            "Relative error: parts=%d, q=%.4f, e1=%.5f, e2=%.5f, rel=%.4f, e3=%.4f",
                                            parts,
                                            q,
                                            e1,
                                            e2,
                                            e2Relative,
                                            e3
                                        ), e2Relative < 0.4
                                    )
                                    Assert.assertTrue(
                                        String.format(
                                            "Absolute error: parts=%d, q=%.4f, e1=%.5f, e2=%.5f, rel=%.4f, e3=%.4f",
                                            parts,
                                            q,
                                            e1,
                                            e2,
                                            e2Relative,
                                            e3
                                        ), Math.abs(e2) < 0.015
                                    )
                                }
                                for (x in doubleArrayOf(0.001, 0.01, 0.1, 0.2, 0.3, 0.5)) {
                                    val z = Dist.cdf(x, allData)
                                    val e1 = dist.cdf(x) - z
                                    val e2 = dist2.cdf(x) - z
                                    val e3 = dist3.cdf(z) - z
                                    out.printf(
                                        "cdf,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f\n",
                                        parts,
                                        x,
                                        z - x,
                                        e1,
                                        e2,
                                        Math.abs(e2) / x,
                                        e3
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
                                        ), Math.abs(e2) / x < 0.4
                                    )
                                }
                                out.flush()
                            }
                            System.out.printf("    Finishing %d\n", currentK + 1)
                            out.close()
                            return s.toString()
                        }
                    })
                }
                val executor =
                    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2)
                try {
                    for (result in executor.invokeAll(tasks)) {
                        out.write(result.get())
                    }
                } catch (e: Throwable) {
                    Assert.fail(e.message)
                } finally {
                    executor.shutdownNow()
                    executor.awaitTermination(10, TimeUnit.SECONDS)
                }
            }
        } catch (e: InterruptedException) {
            Assert.fail("Tasks interrupted")
        } catch (e: FileNotFoundException) {
            Assert.fail("Couldn't write to data output file merge.csv")
        }
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testTreeAccuracy() {
        // TODO there is a fair bit of duplicated code here
        val head = Git.getHash(true).substring(0, 10)
        val experiment = "tree-digest"
        File("tests").mkdirs()
        val quantiles = PrintWriter(String.format("tests/accuracy-%s-%s.csv", experiment, head))
        val sizes = PrintWriter(String.format("tests/accuracy-sizes-%s-%s.csv", experiment, head))
        val cdf = PrintWriter(String.format("tests/accuracy-cdf-%s-%s.csv", experiment, head))
        quantiles.printf("digest, dist, sort, q.digest, q.raw, error, compression, x, k, clusters\n")
        cdf.printf("digest, dist, sort, x.digest, x.raw, error, compression, q, k, clusters\n")
        sizes.printf("digest, dist, sort, q.0, q.1, dk, mean, compression, count, k, clusters\n")
        val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 4)
        val tasks: MutableCollection<Callable<Int>> = ArrayList()
        val lines = AtomicInteger()
        val t0 = System.nanoTime()
        for (k in 0..19) {
            tasks.add(Callable {
                for (dist in setOf(Distribution.UNIFORM)) {
//                        for (Util.Distribution dist : Util.Distribution.values()) {
                    val dx = dist.create(gen)
                    val raw = DoubleArray(N)
                    for (i in 0 until N) {
                        raw[i] = dx!!.nextDouble()
                    }
                    val sorted = Arrays.copyOf(raw, raw.size)
                    Arrays.sort(sorted)
                    for (compression in doubleArrayOf(20.0, 50.0, 100.0, 200.0, 500.0)) {
                        for (factory in setOf(Util.Factory.TREE)) {
//                                    for (Util.Factory factory : Util.Factory.values()) {
                            var digest = factory.create(compression)
                            for (datum in raw) {
                                digest.add(datum)
                            }
                            evaluate(
                                k,
                                quantiles,
                                sizes,
                                cdf,
                                dist,
                                "unsorted",
                                sorted,
                                compression,
                                factory.create(compression)
                            )
                            digest = factory.create(compression)
                            for (datum in sorted) {
                                digest.add(datum)
                            }
                            evaluate(
                                k,
                                quantiles,
                                sizes,
                                cdf,
                                dist,
                                "sorted",
                                sorted,
                                compression,
                                factory.create(compression)
                            )
                        }
                    }
                }
                val count = lines.incrementAndGet()
                val t = System.nanoTime()
                val duration = (t - t0) * 1e-9
                System.out.printf("%d, %d, %.2f, %.3f\n", k, count, duration, count / duration)
                k
            })
        }
        pool.invokeAll(tasks)
        sizes.close()
        quantiles.close()
        cdf.close()
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testAccuracyVersusCompression() {
        val head = Git.getHash(true).substring(0, 10)
        val experiment = "digest"
        File("tests").mkdirs()
        PrintWriter(String.format("tests/accuracy-%s-%s.csv", experiment, head)).use { out ->
            PrintWriter(String.format("tests/accuracy-cdf-%s-%s.csv", experiment, head)).use { cdf ->
                PrintWriter(String.format("tests/accuracy-sizes-%s-%s.csv", experiment, head)).use { sizes ->
                    out.printf("digest, dist, sort, q.digest, q.raw, error, compression, q, x, k, clusters\n")
                    cdf.printf("digest, dist, sort, x.digest, x.raw, error, compression, q, k, clusters\n")
                    sizes.printf("digest, dist, sort, q.0, q.1, dk, mean, compression, count, k, clusters\n")
                    val abort =
                        AtomicBoolean(false)
                    val pool =
                        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 4)
                    val tasks: MutableCollection<Callable<Int>> = ArrayList()
                    val lines = AtomicInteger()
                    val t0 = System.nanoTime()
                    for (k in 0..49) {
                        tasks.add(Callable {

//                            for (Util.Distribution dist : Collections.singleton(Util.Distribution.UNIFORM)) {
                            for (dist in Distribution.values()) {
                                val dx = dist.create(gen)
                                val size =
                                    (N + Random().nextGaussian() * 1000).toInt()
                                val raw = DoubleArray(size)
                                for (i in 0 until size) {
                                    raw[i] = dx!!.nextDouble()
                                }
                                val sorted = Arrays.copyOf(raw, raw.size)
                                Arrays.sort(sorted)
                                for (useWeightLimit in booleanArrayOf(true, false)) {
                                    for (scale in ScaleFunction.values()) {
                                        if (abort.get()) {
                                            // some alternative failed don't even try to continue
                                            return@Callable 0
                                        }
                                        if (scale.toString()
                                                .contains("_NO_NORM") || scale.toString() == "K_0" || scale.toString()
                                                .contains("FAST") || scale.toString().contains("kSize")
                                        ) {
                                            continue
                                        }
                                        for (compression in doubleArrayOf(
                                            50.0,
                                            100.0,
                                            200.0,
                                            500.0,
                                            1000.0
                                        )) {
                                            //                            for (double compression : new double[]{100, 200, 500}) {
                                            for (factory in setOf(Util.Factory.MERGE)) {
                                                //                                    for (Util.Factory factory : Util.Factory.values()) {
                                                val digest =
                                                    factory.create(compression)
                                                MergingDigest.useWeightLimit = useWeightLimit
                                                try {
                                                    digest.setScaleFunction(scale)
                                                } catch (e: IllegalArgumentException) {
                                                    // not all scale functions work with different weight limit strategies
                                                    continue
                                                }
                                                if (digest.toString().contains("K_3")) {
                                                    continue
                                                }
                                                try {
                                                    for (datum in raw) {
                                                        digest.add(datum)
                                                    }
                                                    digest.compress()
                                                    evaluate(
                                                        k,
                                                        out,
                                                        sizes,
                                                        cdf,
                                                        dist,
                                                        "unsorted",
                                                        sorted,
                                                        compression,
                                                        digest
                                                    )
                                                } catch (e: Throwable) {
                                                    System.err.printf(
                                                        "Aborting test with %s, %b, %.0f\n",
                                                        digest,
                                                        useWeightLimit,
                                                        compression
                                                    )
                                                    e.printStackTrace()
                                                    abort.set(true)
                                                    throw e
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            val count = lines.incrementAndGet()
                            val t = System.nanoTime()
                            val duration = (t - t0) * 1e-9
                            System.out.printf(
                                "%d, %d, %.2f, %.3f\n",
                                k,
                                count,
                                duration,
                                count / duration
                            )
                            k
                        })
                    }
                    pool.invokeAll(tasks)
                    Assert.assertFalse("Tasks aborted in test", abort.get())
                }
            }
        }
    }

    private fun evaluate(
        k: Int, quantiles: PrintWriter, sizes: PrintWriter, cdf: PrintWriter,
        dist: Distribution, sort: String,
        sorted: DoubleArray, compression: Double, digest: TDigest
    ) {
        val clusters = digest.centroidCount()
        var qx = 0.0
        for (centroid in digest.centroids()) {
            val dq = centroid.count().toDouble() / sorted.size
            val k0 = (digest as MergingDigest).scaleFunction.k(qx, compression, digest.size().toDouble())
            val k1 = digest.scaleFunction.k(qx + dq, compression, digest.size().toDouble())
            synchronized(sizes) {
                sizes.printf(
                    "%s,%s,%s,%.8f,%.8f,%.8f,%.8g,%.0f,%d,%d,%d\n",
                    digest,
                    dist,
                    sort,
                    qx,
                    qx + dq,
                    k1 - k0,
                    centroid.mean(),
                    compression,
                    centroid.count(),
                    k,
                    clusters
                )
            }
            qx += dq
        }
        for (q in doubleArrayOf(
            1e-6,
            1e-5,
            0.0001,
            0.001,
            0.01,
            0.1,
            0.5,
            0.9,
            0.99,
            0.999,
            0.9999,
            1 - 1e-5,
            1 - 1e-6
        )) {
            val x = Dist.quantile(q, sorted)
            val q1 = digest.cdf(x)
            val q0 = Dist.cdf(x, sorted)
            val error = (q1 - q0) / Math.min(q1, 1 - q1)
            synchronized(quantiles) {
                quantiles.printf(
                    "%s,%s,%s,%.8f,%.8f,%.8g,%.0f,%.8g,%.8g,%d,%d\n",
                    digest,
                    dist,
                    sort,
                    q1,
                    q0,
                    error,
                    compression,
                    q,
                    x,
                    k,
                    clusters
                )
            }
        }
        for (q in doubleArrayOf(
            1e-6,
            1e-5,
            0.0001,
            0.001,
            0.01,
            0.1,
            0.5,
            0.9,
            0.99,
            0.999,
            0.9999,
            1 - 1e-5,
            1 - 1e-6
        )) {
            val x1 = digest.quantile(q)
            val x0 = Dist.quantile(q, sorted)
            val error = (x1 - x0) / Math.min(x1, 1 - x1)
            synchronized(cdf) {
                cdf.printf(
                    "%s,%s,%s,%.8f,%.8f,%.8g,%.0f,%.8g,%d,%d\n",
                    digest,
                    dist,
                    sort,
                    x1,
                    x0,
                    error,
                    compression,
                    q,
                    k,
                    clusters
                )
            }
        }
    }

    /**
     * Prints the actual samples that went into a few clusters near the tails and near the median.
     *
     *
     * This is important for testing how close to ideal a real-world t-digest might be. In particular,
     * it lets us visualize how clusters are shaped in sample space to look for smear or skew.
     *
     *
     * The accuracy.r script produces a visualization of the data produced by this test.
     *
     * @throws FileNotFoundException If output file can't be opened.
     * @throws InterruptedException  If threads are interrupted (we don't ever expect that to happen).
     */
    @Test
    @Throws(FileNotFoundException::class, InterruptedException::class)
    fun testBucketFill() {
        val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2)
        val tasks: MutableCollection<Callable<Int>> = ArrayList()
        val lines = AtomicInteger()
        val t0 = System.nanoTime()
        val samples = PrintWriter("accuracy-samples.csv")
        samples.printf("digest, dist, sort, compression, k, centroid, centroid.down, i, x, mean, q0, q1\n")
        for (k in 0..19) {
            tasks.add(Callable {
                for (compression in doubleArrayOf(100.0)) {
                    for (dist in Distribution.values()) {
                        val dx = dist.create(gen)
                        val raw = DoubleArray(N)
                        for (i in 0 until N) {
                            raw[i] = dx!!.nextDouble()
                        }
                        //                        double[] sorted = Arrays.copyOf(raw, raw.length);
//                        Arrays.sort(sorted);
                        for (scale in arrayOf(ScaleFunction.K_2, ScaleFunction.K_3)) {
                            val digest = MergingDigest(compression)
                            digest.recordAllData()
                            digest.setScaleFunction(scale)
                            evaluate2(k, dist, samples, raw, compression, digest)
                            //                            evaluate2(finalK, dist, samples, "sorted", factory, sorted, compression);
                        }
                    }
                    //                  }
                }
                val count = lines.incrementAndGet()
                val t = System.nanoTime()
                val duration = (t - t0) * 1e-9
                System.out.printf("%d, %d, %.2f, %.3f\n", k, count, duration, count / duration)
                k
            })
        }
        pool.invokeAll(tasks)
        samples.close()
    }

    private fun evaluate2(
        k: Int, dist: Distribution, samples: PrintWriter,
        data: DoubleArray, compression: Double, digest: TDigest
    ) {
        for (datum in data) {
            digest.add(datum)
        }
        var qx = 0.0
        var cx = 0
        val centroids = digest.centroids()
        for (centroid in centroids) {
            val dq = centroid.count().toDouble() / N
            if (qx < 0.05 || Math.abs(qx - 0.5) < 0.025 || qx > 0.95) {
                var sx = 0
                synchronized(samples) {
                    for (x in centroid.data()!!) {
                        samples.printf(
                            "%s,%s,%s,%.0f,%d,%d,%d,%d,%.8f,%.8f,%.8f,%.8f\n",
                            digest, dist, "unsorted", compression,
                            k, cx, centroids.size - cx - 1, sx, x, centroid.mean(), qx, qx + dq
                        )
                        sx++
                    }
                }
            }
            qx += dq
            cx++
        }
    }

    companion object {
        private const val N = 1000000
    }
}