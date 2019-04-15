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

class FloatHistogramTest : HistogramTestCases() {
    @Before
    fun setup() {
        useLinearBuckets = true
        factory = object : HistogramTestCases.HistogramFactory {
            override fun create(min: Double, max: Double): Histogram {
                return FloatHistogram(min, max)
            }
        }
    }

    @Test
    fun testBins() {
        super.testBinSizes(79, 141, FloatHistogram(10e-6, 5.0, 20.0))
    }

    @Test
    @Throws(FileNotFoundException::class)
    fun testLinear() {
        super.doLinear(165.4, 18.0, 212)
    }
    @Test
    fun testCompression() {
        CompressionTestCore()
    }
    @Test
    @Throws(IOException::class, ClassNotFoundException::class)
    fun testSerialization() {
        SerializationTestCore()
    }
}
