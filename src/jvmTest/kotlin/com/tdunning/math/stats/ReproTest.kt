package com.tdunning.math.stats

import org.junit.Assert
import org.junit.Test
import kotlin.random.Random

class ReproTest {
    @Test
    fun testRepro() {
        val gen = Random(1)
        val data = DoubleArray(10000)
        for (i in data.indices) {
            // these samples are truncated and thus have lots of duplicates
            // this can wreak havoc with the t-digest invariants
            data[i] = Math.floor(gen.nextDouble() * 10)
        }
        for (sf in ScaleFunction.values()) {
            if (sf.toString().contains("NO_NORM")) {
                continue
            }
            val distLow: TDigest = MergingDigest(100.0)
            val distMedian: TDigest = MergingDigest(100.0)
            val distHigh: TDigest = MergingDigest(100.0)
            for (i in 0..499) {
                val d1 = MergingDigest(100.0)
                d1.setScaleFunction(ScaleFunction.K_2)
                for (x in data) {
                    d1.add(x)
                }
                d1.compress()
                distLow.add(d1.quantile(0.001))
                distMedian.add(d1.quantile(0.5))
                distHigh.add(d1.quantile(0.999))
            }
            Assert.assertEquals(0.0, distLow.quantile(0.0), 0.0)
            Assert.assertEquals(0.0, distLow.quantile(0.5), 0.0)
            Assert.assertEquals(0.0, distLow.quantile(1.0), 0.0)
            Assert.assertEquals(9.0, distHigh.quantile(0.0), 0.0)
            Assert.assertEquals(9.0, distHigh.quantile(0.5), 0.0)
            Assert.assertEquals(9.0, distHigh.quantile(1.0), 0.0)
            System.out.printf(
                "%s,%.3f,%.5f,%.5f,%.5f\n",
                sf, 0.5,
                distMedian.quantile(0.01), distMedian.quantile(0.5), distMedian.quantile(0.99)
            )
        }
    }
}