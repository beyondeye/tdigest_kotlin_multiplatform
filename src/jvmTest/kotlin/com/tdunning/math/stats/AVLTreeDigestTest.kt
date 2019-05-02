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

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import java.io.IOException

class AVLTreeDigestTest : TDigestTest() {

    override fun factory(compression: Double): DigestFactory {
        return object : DigestFactory {
            override fun create(): TDigest {
                return AVLTreeDigest(compression)
            }
        }
    }

    override fun fromBytes(bytes: BinaryInput): TDigest {
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

    @Test
    fun testUpdateSample() {
        val oldValue=5.0
        val newValue=40.0
        val srcdata = listOf(5.0, 10.0, 15.0, 20.0, 32.0, 60.0)
        //5 removed, 40 added
        val targetdata = listOf(10.0, 15.0, 20.0, 32.0, 40.0, 60.0)

        val td_src = AVLTreeDigest(200.0)
        for (datum in srcdata) {
            td_src.add(datum)
        }
        val td_target=AVLTreeDigest(200.0)
        for (datum in targetdata) {
            td_target.add(datum)
        }
        td_src.updateSample(oldValue,newValue)
//        Assert.assertEquals(td_src.toString(),td_target.toString())


//        Assert.assertEquals(td_src.quantile(0.1), td_target.quantile(0.1), 1e-10)
        Assert.assertEquals(td_src.quantile(0.2), td_target.quantile(0.2), 1e-10)
        Assert.assertEquals(td_src.quantile(0.3), td_target.quantile(0.3), 1e-10)
        Assert.assertEquals(td_src.quantile(0.4), td_target.quantile(0.4), 1e-10)
        Assert.assertEquals(td_src.quantile(0.5), td_target.quantile(0.5), 1e-10)
        Assert.assertEquals(td_src.quantile(0.6), td_target.quantile(0.6), 1e-10)
        Assert.assertEquals(td_src.quantile(0.7), td_target.quantile(0.7), 1e-10)
        Assert.assertEquals(td_src.quantile(0.8), td_target.quantile(0.8), 1e-10)
    }
    @Test
    fun testUpdateSample2() {
        val oldValue=5.0
        val newValue=40.5
        val src = Array(1000) { it.toDouble() }
        val srcdata =src.toList()
        val targetdata= src.toMutableList()
        targetdata.remove(oldValue)
        targetdata.add(newValue)
        //5 removed, 40.5 added

        val td_src = AVLTreeDigest(5.0)
        for (datum in srcdata) {
            td_src.add(datum)
        }
        val td_target=AVLTreeDigest(5.0)
        for (datum in targetdata) {
            td_target.add(datum)
        }
        td_src.updateSample(oldValue,newValue)
//        Assert.assertEquals(td_src.toString(),td_target.toString())


//        Assert.assertEquals(td_src.quantile(0.1), td_target.quantile(0.1), 1e-10)
        Assert.assertEquals(td_src.quantile(0.2), td_target.quantile(0.2), 1e-10)
        Assert.assertEquals(td_src.quantile(0.3), td_target.quantile(0.3), 1e-10)
        Assert.assertEquals(td_src.quantile(0.4), td_target.quantile(0.4), 1e-10)
        Assert.assertEquals(td_src.quantile(0.5), td_target.quantile(0.5), 1e-10)
        Assert.assertEquals(td_src.quantile(0.6), td_target.quantile(0.6), 1e-10)
        Assert.assertEquals(td_src.quantile(0.7), td_target.quantile(0.7), 1e-10)
        Assert.assertEquals(td_src.quantile(0.8), td_target.quantile(0.8), 1e-10)
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
