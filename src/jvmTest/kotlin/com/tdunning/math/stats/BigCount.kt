package com.tdunning.math.stats

import org.junit.Assert
import org.junit.Test

abstract class BigCount : AbstractTest() {

    private fun getDigest(): TDigest
    {
            val digest = createDigest()
            addData(digest)
            return digest
    }

    @Test
    fun testBigMerge() {
        val digest = createDigest()
        for (i in 0..4) {
            digest.add(getDigest())
            val actual = digest.quantile(0.5)
            Assert.assertEquals(
                "Count = " + digest.size(), 3000.0,
                actual, 0.001
            )
        }
    }

    open fun createDigest(): TDigest {
        throw IllegalStateException("Should have over-ridden createDigest")
    }

    private fun addData(digest: TDigest) {
        val n = (300000000 * 5 + 200).toDouble()

        addFakeCentroids(digest, n, 300000000, 10)
        addFakeCentroids(digest, n, 300000000, 200)
        addFakeCentroids(digest, n, 300000000, 3000)
        addFakeCentroids(digest, n, 300000000, 4000)
        addFakeCentroids(digest, n, 300000000, 5000)
        addFakeCentroids(digest, n, 200, 47883554)

        assertEquals(n, digest.size().toDouble(),0.0)
    }

    companion object {
        private fun addFakeCentroids(digest: TDigest, n: Double, points: Int, x: Int) {
            val base = digest.size()
            var q0 = base / n
            var added: Long = 0
            while (added < points) {
                val k0 = digest.scale.k(q0, digest.compression(), n)
                var q1 = digest.scale.q(k0 + 1, digest.compression(), n)
                q1 = Math.min(q1, (base + points) / n)
                val m = Math.min((points - added).toDouble(), Math.max(1.0, Math.rint((q1 - q0) * n)))
                    .toInt()
                added += m.toLong()
                digest.add(x.toDouble(), m)
                q0 = q1
            }
        }
    }
}