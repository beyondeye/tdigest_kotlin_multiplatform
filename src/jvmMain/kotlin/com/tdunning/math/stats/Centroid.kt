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

import java.io.IOException
import java.io.ObjectInputStream
import java.io.Serializable
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * A single centroid which represents a number of data points.
 */
class Centroid private constructor(record: Boolean) : Comparable<Centroid>, Serializable {

    private var centroid = 0.0
    private var count = 0

    // The ID is transient because it must be unique within a given JVM. A new
    // ID should be generated from uniqueCount when a Centroid is deserialized.
    @Transient
    private var id: Int = 0

    private var actualData: MutableList<Double>? = null

    init {
        id = uniqueCount.getAndIncrement()
        if (record) {
            actualData = ArrayList()
        }
    }

    constructor(x: Double) : this(false) {
        start(x, 1, uniqueCount.getAndIncrement())
    }

    constructor(x: Double, w: Int) : this(false) {
        start(x, w, uniqueCount.getAndIncrement())
    }

    constructor(x: Double, w: Int, id: Int) : this(false) {
        start(x, w, id)
    }

    constructor(x: Double, id: Int, record: Boolean) : this(record) {
        start(x, 1, id)
    }

    internal constructor(x: Double, w: Int, data: MutableList<Double>?) : this(x, w) {
        actualData = data
    }

    private fun start(x: Double, w: Int, id: Int) {
        this.id = id
        add(x, w)
    }

    fun add(x: Double, w: Int) {
        if (actualData != null) {
            actualData!!.add(x)
        }
        count += w
        centroid += w * (x - centroid) / count
    }

    fun mean(): Double {
        return centroid
    }

    fun count(): Int {
        return count
    }

    fun id(): Int {
        return id
    }

    override fun toString(): String {
        return "Centroid{" +
                "centroid=" + centroid +
                ", count=" + count +
                '}'.toString()
    }

    override fun hashCode(): Int {
        return id
    }

    override fun compareTo(o: Centroid): Int {
        var r = java.lang.Double.compare(centroid, o.centroid)
        if (r == 0) {
            r = id - o.id
        }
        return r
    }

    fun data(): MutableList<Double>? {
        return actualData
    }

    fun insertData(x: Double) {
        if (actualData == null) {
            actualData = ArrayList()
        }
        actualData!!.add(x)
    }

    fun add(x: Double, w: Int, data: Iterable<Double>?) {
        if (actualData != null) {
            if (data != null) {
                for (old in data) {
                    actualData!!.add(old)
                }
            } else {
                actualData!!.add(x)
            }
        }
        centroid = AbstractTDigest.weightedAverage(centroid, count.toDouble(), x, w.toDouble())
        count += w
    }

    @Throws(ClassNotFoundException::class, IOException::class)
    private fun readObject(`in`: ObjectInputStream) {
        `in`.defaultReadObject()
        id = uniqueCount.getAndIncrement()
    }

    companion object {
        private val uniqueCount = AtomicInteger(1)

        fun createWeighted(x: Double, w: Int, data: Iterable<Double>?): Centroid {
            val r = Centroid(data != null)
            r.add(x, w, data)
            return r
        }
    }
}
