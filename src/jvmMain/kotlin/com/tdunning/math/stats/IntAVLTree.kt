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



/**
 * An AVL-tree structure stored in parallel arrays.
 * This class only stores the tree structure, so you need to extend it if you
 * want to add data to the nodes, typically by using arrays and node
 * identifiers as indices.
 */
internal abstract class IntAVLTree @JvmOverloads constructor(initialCapacity: Int = 16)
//    : Serializable
{

    private val nodeAllocator: NodeAllocator
    private var root: Int = 0
    private var parent: IntArray
    private var left: IntArray
    private var right: IntArray
    private var depth: ByteArray

    init {
        nodeAllocator = NodeAllocator()
        root = NIL
        parent = IntArray(initialCapacity)
        left = IntArray(initialCapacity)
        right = IntArray(initialCapacity)
        depth = ByteArray(initialCapacity)
    }

    /**
     * Return the current root of the tree.
     */
    fun root(): Int {
        return root
    }

    /**
     * Return the current capacity, which is the number of nodes that this tree
     * can hold.
     */
    fun capacity(): Int {
        return parent.size
    }

    /**
     * Resize internal storage in order to be able to store data for nodes up to
     * `newCapacity` (excluded).
     */
    protected open fun resize(newCapacity: Int) {
        parent = parent.copyOf( newCapacity)
        left = left.copyOf( newCapacity)
        right = right.copyOf( newCapacity)
        depth = depth.copyOf( newCapacity)
    }

    /**
     * Return the size of this tree.
     */
    fun size(): Int {
        return nodeAllocator.size()
    }

    /**
     * Return the parent of the provided node.
     */
    fun parent(node: Int): Int {
        return parent[node]
    }

    /**
     * Return the left child of the provided node.
     */
    fun left(node: Int): Int {
        return left[node]
    }

    /**
     * Return the right child of the provided node.
     */
    fun right(node: Int): Int {
        return right[node]
    }

    /**
     * Return the depth nodes that are stored below `node` including itself.
     */
    fun depth(node: Int): Int {
        return depth[node].toInt()
    }

    /**
     * Return the least node under `node`.
     */
    fun first(node: Int): Int {
        var node = node
        if (node == NIL) {
            return NIL
        }
        while (true) {
            val left = left(node)
            if (left == NIL) {
                break
            }
            node = left
        }
        return node
    }

    /**
     * Return the largest node under `node`.
     */
    fun last(node: Int): Int {
        var node = node
        while (true) {
            val right = right(node)
            if (right == NIL) {
                break
            }
            node = right
        }
        return node
    }

    /**
     * Return the least node that is strictly greater than `node`.
     */
    fun next(node: Int): Int {
        var node = node
        val right = right(node)
        if (right != NIL) {
            return first(right)
        } else {
            var parent = parent(node)
            while (parent != NIL && node == right(parent)) {
                node = parent
                parent = parent(parent)
            }
            return parent
        }
    }

    /**
     * Return the highest node that is strictly less than `node`.
     */
    fun prev(node: Int): Int {
        var node = node
        val left = left(node)
        if (left != NIL) {
            return last(left)
        } else {
            var parent = parent(node)
            while (parent != NIL && node == left(parent)) {
                node = parent
                parent = parent(parent)
            }
            return parent
        }
    }

    /**
     * Compare data against data which is stored in `node`.
     */
    protected abstract fun compare(node: Int): Int

    /**
     * Compare data into `node`.
     */
    protected abstract fun copy(node: Int)

    /**
     * Merge data into `node`.
     */
    protected abstract fun merge(node: Int)


    /**
     * Add current data to the tree and return <tt>true</tt> if a new node was added
     * to the tree or <tt>false</tt> if the node was merged into an existing node.
     */
    fun add(): Boolean {
        if (root == NIL) {
            root = nodeAllocator.newNode()
            copy(root)
            fixAggregates(root)
            return true
        } else {
            var node = root
            assert(parent(root) == NIL)
            var parent: Int
            var cmp: Int
            do {
                cmp = compare(node)
                if (cmp < 0) {
                    parent = node
                    node = left(node)
                } else if (cmp > 0) {
                    parent = node
                    node = right(node)
                } else {
                    merge(node)
                    return false
                }
            } while (node != NIL)

            node = nodeAllocator.newNode()
            if (node >= capacity()) {
                resize(oversize(node + 1))
            }
            copy(node)
            parent(node, parent)
            if (cmp < 0) {
                left(parent, node)
            } else {
                assert(cmp > 0)
                right(parent, node)
            }

            rebalance(node)

            return true
        }
    }

    /**
     * Find a node in this tree.
     */
    fun find(): Int {
        var node = root
        while (node != NIL) {
            val cmp = compare(node)
            if (cmp < 0) {
                node = left(node)
            } else if (cmp > 0) {
                node = right(node)
            } else {
                return node
            }
        }
        return NIL
    }

    /**
     * Update `node` with the current data.
     */
    fun update(node: Int) {
        val prev = prev(node)
        val next = next(node)
        if ((prev == NIL || compare(prev) > 0) && (next == NIL || compare(next) < 0)) {
            // Update can be done in-place
            copy(node)
            var n = node
            while (n != NIL) {
                fixAggregates(n)
                n = parent(n)
            }
        } else {
            // TODO: it should be possible to find the new node position without
            // starting from scratch
            remove(node)
            add()
        }
    }

    /**
     * Remove the specified node from the tree.
     */
    fun remove(node: Int) {
        if (node == NIL) {
            throw IllegalArgumentException()
        }
        if (left(node) != NIL && right(node) != NIL) {
            // inner node
            val next = next(node)
            assert(next != NIL)
            swap(node, next)
        }
        assert(left(node) == NIL || right(node) == NIL)

        val parent = parent(node)
        var child = left(node)
        if (child == NIL) {
            child = right(node)
        }

        if (child == NIL) {
            // no children
            if (node == root) {
                assert(size() == 1) { size() }
                root = NIL
            } else {
                if (node == left(parent)) {
                    left(parent, NIL)
                } else {
                    assert(node == right(parent))
                    right(parent, NIL)
                }
            }
        } else {
            // one single child
            if (node == root) {
                assert(size() == 2)
                root = child
            } else if (node == left(parent)) {
                left(parent, child)
            } else {
                assert(node == right(parent))
                right(parent, child)
            }
            parent(child, parent)
        }

        release(node)
        rebalance(parent)
    }

    private fun release(node: Int) {
        left(node, NIL)
        right(node, NIL)
        parent(node, NIL)
        nodeAllocator.release(node)
    }

    private fun swap(node1: Int, node2: Int) {
        val parent1 = parent(node1)
        val parent2 = parent(node2)
        if (parent1 != NIL) {
            if (node1 == left(parent1)) {
                left(parent1, node2)
            } else {
                assert(node1 == right(parent1))
                right(parent1, node2)
            }
        } else {
            assert(root == node1)
            root = node2
        }
        if (parent2 != NIL) {
            if (node2 == left(parent2)) {
                left(parent2, node1)
            } else {
                assert(node2 == right(parent2))
                right(parent2, node1)
            }
        } else {
            assert(root == node2)
            root = node1
        }
        parent(node1, parent2)
        parent(node2, parent1)

        val left1 = left(node1)
        val left2 = left(node2)
        left(node1, left2)
        if (left2 != NIL) {
            parent(left2, node1)
        }
        left(node2, left1)
        if (left1 != NIL) {
            parent(left1, node2)
        }

        val right1 = right(node1)
        val right2 = right(node2)
        right(node1, right2)
        if (right2 != NIL) {
            parent(right2, node1)
        }
        right(node2, right1)
        if (right1 != NIL) {
            parent(right1, node2)
        }

        val depth1 = depth(node1)
        val depth2 = depth(node2)
        depth(node1, depth2)
        depth(node2, depth1)
    }

    private fun balanceFactor(node: Int): Int {
        return depth(left(node)) - depth(right(node))
    }

    private fun rebalance(node: Int) {
        var n = node
        while (n != NIL) {
            val p = parent(n)

            fixAggregates(n)

            when (balanceFactor(n)) {
                -2 -> {
                    val right = right(n)
                    if (balanceFactor(right) == 1) {
                        rotateRight(right)
                    }
                    rotateLeft(n)
                }
                2 -> {
                    val left = left(n)
                    if (balanceFactor(left) == -1) {
                        rotateLeft(left)
                    }
                    rotateRight(n)
                }
                -1, 0, 1 -> {
                }
                else -> throw AssertionError()
            }// ok

            n = p
        }
    }

    protected open fun fixAggregates(node: Int) {
        depth(node, 1 + kotlin.math.max(depth(left(node)), depth(right(node))))
    }

    /** Rotate left the subtree under `n`  */
    private fun rotateLeft(n: Int) {
        val r = right(n)
        val lr = left(r)
        right(n, lr)
        if (lr != NIL) {
            parent(lr, n)
        }
        val p = parent(n)
        parent(r, p)
        if (p == NIL) {
            root = r
        } else if (left(p) == n) {
            left(p, r)
        } else {
            assert(right(p) == n)
            right(p, r)
        }
        left(r, n)
        parent(n, r)
        fixAggregates(n)
        fixAggregates(parent(n))
    }

    /** Rotate right the subtree under `n`  */
    private fun rotateRight(n: Int) {
        val l = left(n)
        val rl = right(l)
        left(n, rl)
        if (rl != NIL) {
            parent(rl, n)
        }
        val p = parent(n)
        parent(l, p)
        if (p == NIL) {
            root = l
        } else if (right(p) == n) {
            right(p, l)
        } else {
            assert(left(p) == n)
            left(p, l)
        }
        right(l, n)
        parent(n, l)
        fixAggregates(n)
        fixAggregates(parent(n))
    }

    private fun parent(node: Int, parent: Int) {
        assert(node != NIL)
        this.parent[node] = parent
    }

    private fun left(node: Int, left: Int) {
        assert(node != NIL)
        this.left[node] = left
    }

    private fun right(node: Int, right: Int) {
        assert(node != NIL)
        this.right[node] = right
    }

    private fun depth(node: Int, depth: Int) {
        assert(node != NIL)
        assert(depth >= 0 && depth <= Byte.MAX_VALUE)
        this.depth[node] = depth.toByte()
    }

    fun checkBalance(node: Int) {
        if (node == NIL) {
            assert(depth(node) == 0)
        } else {
            assert(depth(node) == 1 + kotlin.math.max(depth(left(node)), depth(right(node))))
            assert(kotlin.math.abs(depth(left(node)) - depth(right(node))) <= 1)
            checkBalance(left(node))
            checkBalance(right(node))
        }
    }

    /**
     * A stack of int values.
     */
    private class IntStack internal constructor()
    //   : Serializable
    {

        private var stack: IntArray
        private var size: Int = 0

        init {
            stack = IntArray(0)
            size = 0
        }

        internal fun size(): Int {
            return size
        }

        internal fun pop(): Int {
            return stack[--size]
        }

        internal fun push(v: Int) {
            if (size >= stack.size) {
                val newLength = oversize(size + 1)
                stack = stack.copyOf(newLength)
            }
            stack[size++] = v
        }

    }

    private class NodeAllocator internal constructor()
//        : Serializable
    {

        private var nextNode: Int = 0
        private val releasedNodes: IntStack

        init {
            nextNode = NIL + 1
            releasedNodes = IntStack()
        }

        internal fun newNode(): Int {
            return if (releasedNodes.size() > 0) {
                releasedNodes.pop()
            } else {
                nextNode++
            }
        }

        internal fun release(node: Int) {
            assert(node < nextNode)
            releasedNodes.push(node)
        }

        internal fun size(): Int {
            return nextNode - releasedNodes.size() - 1
        }

    }

    companion object {

        /**
         * We use <tt>0</tt> instead of <tt>-1</tt> so that left(NIL) works without
         * condition.
         */
        val NIL = 0

        /** Grow a size by 1/8.  */
        fun oversize(size: Int): Int {
            return size + size.ushr(3)
        }
    }

}
