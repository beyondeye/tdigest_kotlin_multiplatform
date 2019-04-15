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

import kotlinx.io.core.Input
import org.junit.BeforeClass

import java.io.IOException

class AVLTreeDigestTest : TDigestTest() {

    override fun factory(compression: Double): DigestFactory {
        return object : DigestFactory {
            override fun create(): TDigest {
                return AVLTreeDigest(compression)
            }
        }
    }

    override fun fromBytes(bytes: Input): TDigest {
        return AVLTreeDigest.fromBytes(bytes)
    }
    override fun testRepeatedValues() {
        // disabled for AVLTreeDigest for now
    }
    override fun testSingletonInACrowd() {
        // disabled for AVLTreeDigest for now
    }
    override fun singleSingleRange() {
        // disabled for AVLTreeDigest for now
    }
    companion object {
        @BeforeClass
        @Throws(IOException::class)
        fun setup() {
            setup("avl-tree")
        }
    }

    //    @Override
    //    public void testKSDrift() {
    //        System.out.printf("Skipping KS test for AVL digest\n");
    //    }
}
