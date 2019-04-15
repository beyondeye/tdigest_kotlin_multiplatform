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
import org.apache.mahout.common.RandomUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AVLGroupTreeTest : AbstractTest() {

    @Before
    fun setUp() {
        RandomUtils.useTestSeed()
    }

    @Test
    fun testSimpleAdds() {
        val x = AVLGroupTree(false)
        Assert.assertEquals(IntAVLTree.NIL.toLong(), x.floor(34.0).toLong())
        Assert.assertEquals(IntAVLTree.NIL.toLong(), x.first().toLong())
        Assert.assertEquals(IntAVLTree.NIL.toLong(), x.last().toLong())
        Assert.assertEquals(0, x.size.toLong())
        Assert.assertEquals(0, x.sum().toLong())

        x.add(Centroid(1.0))
        Assert.assertEquals(1, x.sum().toLong())
        val centroid = Centroid(2.0)
        centroid.add(3.0, 1)
        centroid.add(4.0, 1)
        x.add(centroid)

        Assert.assertEquals(2, x.size.toLong())
        Assert.assertEquals(4, x.sum().toLong())
    }

    @Test
    fun testBalancing() {
        val x = AVLGroupTree(false)
        for (i in 0..100) {
            x.add(Centroid(i.toDouble()))
        }

        Assert.assertEquals(101, x.size.toLong())
        Assert.assertEquals(101, x.sum().toLong())

        x.checkBalance()
        x.checkAggregates()
    }

    @Test
    fun testFloor() {
        // mostly tested in other tests
        val x = AVLGroupTree(false)
        for (i in 0..100) {
            x.add(Centroid((i / 2).toDouble()))
        }

        Assert.assertEquals(IntAVLTree.NIL.toLong(), x.floor(-30.0).toLong())

        for (centroid in x) {
            Assert.assertEquals(centroid.mean(), x.mean(x.floor(centroid.mean() + 0.1)), 0.0)
        }
    }

    @Test
    fun testHeadSum() {
        val x = AVLGroupTree(false)
        for (i in 0..999) {
            x.add(RandomizedTest.randomDouble(), RandomizedTest.randomIntBetween(1, 10), null)
        }
        var sum: Long = 0
        var last: Long = -1
        var node = x.first()
        while (node != IntAVLTree.NIL) {
            Assert.assertEquals(sum, x.headSum(node))
            sum += x.count(node).toLong()
            last = x.count(node).toLong()
            node = x.next(node)
        }
        Assert.assertEquals(last, x.count(x.last()).toLong())
    }

    @Test
    fun testFloorSum() {
        val x = AVLGroupTree(false)
        var total = 0
        for (i in 0..999) {
            val count = RandomizedTest.randomIntBetween(1, 10)
            x.add(RandomizedTest.randomDouble(), count, null)
            total += count
        }
        Assert.assertEquals(IntAVLTree.NIL.toLong(), x.floorSum(-1).toLong())
        for (i in 0 until total + 10) {
            val floorNode = x.floorSum(i.toLong())
            Assert.assertTrue(x.headSum(floorNode) <= i)
            val next = x.next(floorNode)
            Assert.assertTrue(next == IntAVLTree.NIL || x.headSum(next) > i)
        }
    }

}
