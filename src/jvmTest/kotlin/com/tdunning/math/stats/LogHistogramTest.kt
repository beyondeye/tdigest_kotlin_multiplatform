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

import org.junit.Before
import org.junit.Test

import java.io.FileNotFoundException
import java.io.IOException

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class LogHistogramTest : HistogramTestCases() {
    @Before
    fun setup() {
        useLinearBuckets = false
        factory = object : HistogramTestCases.HistogramFactory {
            override fun create(min: Double, max: Double): Histogram {
                return LogHistogram(min, max, 0.05)
            }
        }
    }


    @Test
    fun testApproxLog() {
        var x = 1e-6
        for (i in 0..999) {
            assertEquals(Math.log(x) / Math.log(2.0), LogHistogram.approxLog2(x), 0.01)
            x *= 1.0 + Math.PI / 100.0
        }
        assertTrue("Insufficient range", x > 1e6)
    }

    @Test
    @Throws(Exception::class)
    fun testInverse() {
        var x = 0.001
        while (x <= 100) {
            val log = LogHistogram.approxLog2(x)
            val roundTrip = LogHistogram.pow2(log)
            assertEquals(x, roundTrip, 1e-13)
            x += 1e-3
        }

    }

    @Test
    fun testBins() {
        super.testBinSizes(72, 129, LogHistogram(10e-6, 5.0, 0.1))
    }

    @Test
    @Throws(FileNotFoundException::class)
    fun testLinear() {
        super.doLinear(146.0, 17.0, 189)
    }

    @Test
    fun testCompression() {
        //ignore
       // testCompressionCore()
    }
    @Test
    @Throws(IOException::class, ClassNotFoundException::class)
    fun testSerialization() {
        //ignore
       // testSerializationCore()
    }

}
