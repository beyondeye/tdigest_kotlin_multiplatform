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

import org.apache.commons.lang3.SerializationUtils
import org.junit.Test

import java.nio.ByteBuffer
import java.util.Random

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull

/**
 * Verifies that the various TDigest implementations can be serialized.
 *
 * Serializability is important, for example, if we want to use t-digests with Spark.
 */
class TDigestSerializationTest {
    @Test
    fun testMergingDigest() {
        assertSerializesAndDeserializes(MergingDigest(100.0))
    }

    @Test
    fun testAVLTreeDigest() {
        assertSerializesAndDeserializes(AVLTreeDigest(100.0))
    }

    private fun <T : TDigest> assertSerializesAndDeserializes(tdigest: T) {
        assertNotNull(SerializationUtils.deserialize(SerializationUtils.serialize(tdigest)))

        val gen = Random()
        for (i in 0..99999) {
            tdigest.add(gen.nextDouble())
        }
        val roundTrip = SerializationUtils.deserialize<T>(SerializationUtils.serialize(tdigest))

        assertTDigestEquals(tdigest, roundTrip)
    }

    private fun assertTDigestEquals(t1: TDigest, t2: TDigest) {
        assertEquals(t1.min, t2.min, 0.0)
        assertEquals(t1.max, t2.max, 0.0)
        val cx = t2.centroids().iterator()
        for (c1 in t1.centroids()) {
            val c2 = cx.next()
            assertEquals(c1.count().toLong(), c2.count().toLong())
            assertEquals(c1.mean(), c2.mean(), 1e-10)
        }
        assertFalse(cx.hasNext())
        assertNotNull(t2)
    }
}
