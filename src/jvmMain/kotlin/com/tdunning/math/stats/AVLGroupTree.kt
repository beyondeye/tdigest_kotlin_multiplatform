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

import java.io.Serializable

/**
 * A tree of t-digest centroids.
 */
internal class AVLGroupTree @JvmOverloads constructor(record: Boolean = false) : AbstractCollection<Centroid>(),
    Serializable {

    /* For insertions into the tree */
    private var centroid: Double = 0.toDouble()
    private var count: Int = 0
    private var data: MutableList<Double>? = null

    private var centroids: DoubleArray? = null
    private var counts: IntArray? = null
    private var datas: Array<MutableList<Double>?>? = null
    private var aggregatedCounts: IntArray? = null
    private val tree: IntAVLTree

    init {
        tree = object : IntAVLTree() {

            override fun resize(newCapacity: Int) {
                super.resize(newCapacity)
                centroids = centroids!!.copyOf(newCapacity)
                counts = counts!!.copyOf(newCapacity)
                aggregatedCounts = aggregatedCounts!!.copyOf(newCapacity)
                if (datas != null) {
                    datas = datas!!.copyOf(newCapacity)
                }
            }

            override fun merge(node: Int) {
                // two nodes are never considered equal
                throw UnsupportedOperationException()
            }

            override fun copy(node: Int) {
                centroids!![node] = centroid
                counts!![node] = count
                if (datas != null) {
                    if (data == null) {
                        if (count != 1) {
                            throw IllegalStateException()
                        }
                        data = mutableListOf()
                        data!!.add(centroid)
                    }
                    datas!![node] = data!!
                }
            }

            override fun compare(node: Int): Int {
                return if (centroid < centroids!![node]) {
                    -1
                } else {
                    // upon equality, the newly added node is considered greater
                    1
                }
            }

            override fun fixAggregates(node: Int) {
                super.fixAggregates(node)
                aggregatedCounts!![node] =
                    counts!![node] + aggregatedCounts!![left(node)] + aggregatedCounts!![right(node)]
            }

        }
        centroids = DoubleArray(tree.capacity())
        counts = IntArray(tree.capacity())
        aggregatedCounts = IntArray(tree.capacity())
        if (record) {
            val datas = arrayOfNulls<MutableList<Double>>(tree.capacity())
            this.datas = datas
        }
    }

    /**
     * Return the number of centroids in the tree.
     */
     override val  size: Int
            get() { return  tree.size() }

    /**
     * Return the previous node.
     */
    fun prev(node: Int): Int {
        return tree.prev(node)
    }

    /**
     * Return the next node.
     */
    fun next(node: Int): Int {
        return tree.next(node)
    }

    /**
     * Return the mean for the provided node.
     */
    fun mean(node: Int): Double {
        return centroids!![node]
    }

    /**
     * Return the count for the provided node.
     */
    fun count(node: Int): Int {
        return counts!![node]
    }

    /**
     * Return the data for the provided node.
     */
    fun data(node: Int): MutableList<Double>? {
        return if (datas == null) null else datas!![node]
    }

    /**
     * Add the provided centroid to the tree.
     */
    fun add(centroid: Double, count: Int, data: MutableList<Double>?) {
        this.centroid = centroid
        this.count = count
        this.data = data
        tree.add()
    }

    fun add(centroid: Centroid): Boolean {
        add(centroid.mean(), centroid.count(), centroid.data())
        return true
    }

    /**
     * Update values associated with a node, readjusting the tree if necessary.
     */
    fun update(node: Int, centroid: Double, count: Int, data: MutableList<Double>?) {
        this.centroid = centroid
        this.count = count
        this.data = data
        tree.update(node)
    }

    /**
     * Return the last node whose centroid is less than `centroid`.
     */
    fun floor(centroid: Double): Int {
        var floor = IntAVLTree.NIL
        var node = tree.root()
        while (node != IntAVLTree.NIL) {
            val cmp = java.lang.Double.compare(centroid, mean(node))
            if (cmp <= 0) {
                node = tree.left(node)
            } else {
                floor = node
                node = tree.right(node)
            }
        }
        return floor
    }

    /**
     * Return the last node so that the sum of counts of nodes that are before
     * it is less than or equal to `sum`.
     */
    fun floorSum(sum: Long): Int {
        var sum = sum
        var floor = IntAVLTree.NIL
        var node = tree.root()
        while (node != IntAVLTree.NIL) {
            val left = tree.left(node)
            val leftCount = aggregatedCounts!![left].toLong()
            if (leftCount <= sum) {
                floor = node
                sum -= leftCount + count(node)
                node = tree.right(node)
            } else {
                node = tree.left(node)
            }
        }
        return floor
    }

    /**
     * Return the least node in the tree.
     */
    fun first(): Int {
        return tree.first(tree.root())
    }

    /**
     * Compute the number of elements and sum of counts for every entry that
     * is strictly before `node`.
     */
    fun headSum(node: Int): Long {
        val left = tree.left(node)
        var sum = aggregatedCounts!![left].toLong()
        var n = node
        var p = tree.parent(node)
        while (p != IntAVLTree.NIL) {
            if (n == tree.right(p)) {
                val leftP = tree.left(p)
                sum += (counts!![p] + aggregatedCounts!![leftP]).toLong()
            }
            n = p
            p = tree.parent(n)
        }
        return sum
    }

    override fun iterator(): MutableIterator<Centroid> {
        return iterator(first())
    }

    private fun iterator(startNode: Int): MutableIterator<Centroid> {
        return object : MutableIterator<Centroid> {

            var nextNode = startNode

            override fun hasNext(): Boolean {
                return nextNode != IntAVLTree.NIL
            }

            override fun next(): Centroid {
                val next = Centroid(mean(nextNode), count(nextNode))
                val data = data(nextNode)
                if (data != null) {
                    for (x in data) {
                        next.insertData(x)
                    }
                }
                nextNode = tree.next(nextNode)
                return next
            }

            override fun remove() {
                throw UnsupportedOperationException("Read-only iterator")
            }

        }
    }

    /**
     * Return the total count of points that have been added to the tree.
     */
    fun sum(): Int {
        return aggregatedCounts!![tree.root()]
    }

    fun checkBalance() {
        tree.checkBalance(tree.root())
    }

    fun checkAggregates() {
        checkAggregates(tree.root())
    }

    private fun checkAggregates(node: Int) {
        assert(
            aggregatedCounts!![node] == counts!![node] + aggregatedCounts!![tree.left(node)] + aggregatedCounts!![tree.right(
                node
            )]
        )
        if (node != IntAVLTree.NIL) {
            checkAggregates(tree.left(node))
            checkAggregates(tree.right(node))
        }
    }

}
