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

import com.carrotsearch.randomizedtesting.annotations.Seed
import kotlinx.io.core.Input
import org.apache.mahout.common.RandomUtils
import org.junit.Before
import org.junit.BeforeClass

import java.io.IOException

//to freeze the tests with a particular seed, put the seed on the next line
//@Seed("84527677CF03B566:A6FF596BDDB2D59D")
@Seed("1CD6F48E8CA53BD1:379C5BDEB3A02ACB")
class MergingDigestTest : TDigestTest() {

    override fun factory(compression: Double): DigestFactory {
        return object : DigestFactory {
            override fun create(): TDigest {
                return MergingDigest(compression)
            }
        }
    }

    @Before
    fun testSetUp() {
        RandomUtils.useTestSeed()
    }


    override fun fromBytes(bytes: BinaryInput): TDigest {
        return MergingDigest.fromBytes(bytes)
    }

    companion object {
        @BeforeClass
        @Throws(IOException::class)
        fun setup() {
            setup("merge")
        }
    }
}