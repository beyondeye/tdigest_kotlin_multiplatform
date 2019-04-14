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
import org.junit.Test

import java.util.*


class IntAVLTreeTest : AbstractTest() {

    internal class IntBag : IntAVLTree() {

        var value: Int = 0
        var values: IntArray
        var counts: IntArray

        init {
            values = IntArray(capacity())
            counts = IntArray(capacity())
        }

        fun addValue(value: Int): Boolean {
            this.value = value
            return super.add()
        }

        fun removeValue(value: Int): Boolean {
            this.value = value
            val node = find()
            if (node == IntAVLTree.NIL) {
                return false
            } else {
                super.remove(node)
                return true
            }
        }

        override fun resize(newCapacity: Int) {
            super.resize(newCapacity)
            values = Arrays.copyOf(values, newCapacity)
            counts = Arrays.copyOf(counts, newCapacity)
        }

        override fun compare(node: Int): Int {
            return value - values[node]
        }

        override fun copy(node: Int) {
            values[node] = value
            counts[node] = 1
        }

        override fun merge(node: Int) {
            values[node] = value
            counts[node]++
        }

    }

    @Test
    fun dualAdd() {
        val r = Random(0)
        val map = TreeMap<Int, Int>()
        val bag = IntBag()
        for (i in 0..99999) {
            val v = r.nextInt(100000)
            if (map.containsKey(v)) {
                map[v] = map[v]!! + 1
                Assert.assertFalse(bag.addValue(v))
            } else {
                map[v] = 1
                Assert.assertTrue(bag.addValue(v))
            }
        }
        val it = map.entries.iterator()
        var node = bag.first(bag.root())
        while (node != IntAVLTree.NIL) {
            val next = it.next()
            Assert.assertEquals(next.key.toLong(), bag.values[node].toLong())
            Assert.assertEquals(next.value.toLong(), bag.counts[node].toLong())
            node = bag.next(node)
        }
        Assert.assertFalse(it.hasNext())
    }

    @Test
    fun dualAddRemove() {
        val r = Random(0)
        val map = TreeMap<Int, Int>()
        val bag = IntBag()
        for (i in 0..99999) {
            val v = r.nextInt(1000)
            if (r.nextBoolean()) {
                // add
                if (map.containsKey(v)) {
                    map[v] = map[v]!! + 1
                    Assert.assertFalse(bag.addValue(v))
                } else {
                    map[v] = 1
                    Assert.assertTrue(bag.addValue(v))
                }
            } else {
                // remove
                Assert.assertEquals(map.remove(v) != null, bag.removeValue(v))
            }
        }
        val it = map.entries.iterator()
        var node = bag.first(bag.root())
        while (node != IntAVLTree.NIL) {
            val next = it.next()
            Assert.assertEquals(next.key.toLong(), bag.values[node].toLong())
            Assert.assertEquals(next.value.toLong(), bag.counts[node].toLong())
            node = bag.next(node)
        }
        Assert.assertFalse(it.hasNext())
    }

}
