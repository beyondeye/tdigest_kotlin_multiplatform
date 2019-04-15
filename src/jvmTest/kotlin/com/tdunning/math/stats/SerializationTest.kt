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

import kotlinx.io.core.buildPacket
import org.junit.Test

import java.nio.BufferUnderflowException
import java.nio.ByteBuffer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class SerializationTest {
    @Test
    @Throws(Exception::class)
    fun mergingDigestSerDes() {
        val out = TDigest.createDigest(100.0)
        out.add(42.5)
        out.add(1.0)
        out.add(24.0)

//        val output = ByteBuffer.allocate(out.smallByteSize())
        val buf = buildPacket {
            out.asSmallBytes(this)
        }

//        var input = ByteBuffer.wrap(output.array())
        var input = buf.copy()
        try {
            val m = MergingDigest.fromBytes(input)
            var q = 0.0
            while (q <= 1) {
                assertEquals(m.quantile(q), out.quantile(q), 0.0)
                q += 0.001
            }
            val ix = m.centroids().iterator()
            for (centroid in out.centroids()) {
                assertTrue(ix.hasNext())
                val c = ix.next()
                assertEquals(centroid.mean(), c.mean(), 0.0)
                assertEquals(centroid.count().toFloat(), c.count().toFloat(), 0f)
            }
            assertFalse(ix.hasNext())
        } catch (e: BufferUnderflowException) {
            println("WTF?")
        }
        input.release()

        input = buf
        val `in` = MergingDigest.fromBytes(input)
        input.release()
        assertEquals(out.quantile(0.95), `in`.quantile(0.95), 0.001)
    }
}
