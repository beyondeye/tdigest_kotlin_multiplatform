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
        digest.add(10.0, 300000000)
        digest.add(200.0, 300000000)
        digest.add(3000.0, 300000000)
        digest.add(4000.0, 300000000)
        digest.add(5000.0, 300000000)
        digest.add(47883554.0, 200)
    }
}