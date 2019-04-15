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
import java.nio.ByteBuffer
import java.util.Random

import org.junit.Test

import com.google.common.collect.Lists
import kotlinx.io.core.buildPacket
import org.junit.Assert



class TDigestUtilTest : AbstractTest() {

    @Test
    fun testIntEncoding() {
        val gen = RandomizedTest.getRandom()
//        val buf = ByteBuffer.allocate(10000)
        val ref = Lists.newArrayList<Int>()
        val buf = buildPacket {
            for (i in 0..2999) {
                var n = gen.nextInt()
                n = n.ushr(i / 100)
                ref.add(n)
                AbstractTDigest.encode(this, n)
            }
        }


        for (i in 0..2999) {
            val n = AbstractTDigest.decode(buf)
            Assert.assertEquals(String.format("%d:", i), ref[i].toInt().toLong(), n.toLong())
        }
        buf.release()
    }
}