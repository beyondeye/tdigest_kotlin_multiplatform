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

import com.google.common.collect.Lists
import org.junit.Test

import java.util.ArrayList
import java.util.Random
import java.util.concurrent.*

import org.junit.Assume.assumeTrue

class MegaMergeTest {

    @Test
    @Throws(InterruptedException::class, ExecutionException::class)
    fun testLargeMerge() {
        assumeTrue(java.lang.Boolean.parseBoolean(System.getProperty("runSlowTests")))
        // prove we can summarize a days worth of data at 5 minute intervals. Each interval has
        // 1000 samples each with 1500 data points
        val t0 = System.nanoTime() * 1e-9
        // we cheat by only having 23 samples that we rotate into the data summaries
        // the raw data
        val data = Array(DATA_STRIDE) { DoubleArray(1500) }
        val gen = Random()
        for (i in 0 until DATA_STRIDE) {
            for (j in 0..1499) {
                data[i][j] = gen.nextGaussian()
            }
        }
        val t1 = System.nanoTime() * 1e-9
        System.out.printf("Data has been generated\n")
        // record the basic summaries
        val td = Array<Array<MergingDigest?>>(DAY) { arrayOfNulls(WIDTH) }
        var m = 0
        for (i in 0 until DAY) {
            if (i % 10 == 0) {
                System.out.printf("%d\n", i)
            }
            for (j in 0 until WIDTH) {
                td[i][j] = MergingDigest(100.0)
                for (k in 0..1499) {
                    td[i][j]!!.add(data[m][k])
                }
                m = (m + 1) % DATA_STRIDE
            }
        }
        var t2 = System.nanoTime() * 1e-9
        System.out.printf("Micro-summaries filled\n")
        System.out.printf("%.3f,%.3f\n", t1 - t0, t2 - t1)
        val cores = Runtime.getRuntime().availableProcessors()
        System.out.printf("using %d cores\n", cores)
        for (threads in 1 until 2 * cores) {
            t2 = System.nanoTime() * 1e-9
            // pull the summaries together into 288 reasonably high resolution t-digests
            val tasks = ArrayList<Callable<MergingDigest>>()
            for (i in 0 until DAY) {
                val elements: Array<MergingDigest> = td[i].map { it!! }.toTypedArray()
                tasks.add(Callable {
                    val rx = MergingDigest(100.0)
                    rx.add(Lists.newArrayList(*elements))
                    rx
                })
            }
            val pool = Executors.newFixedThreadPool(threads)
            val results = pool.invokeAll(tasks)
            val r = arrayOfNulls<MergingDigest>(DAY)
            try {
                var i = 0
                for (result in results) {
                    r[i++] = result.get()
                }
            } finally {
                pool.shutdown()
                pool.awaitTermination(2, TimeUnit.SECONDS)
            }
            val t3 = System.nanoTime() * 1e-9
            System.out.printf(
                "%.3f,%.3f,%.3f,%.3f\n",
                r[0]!!.quantile(0.99), r[100]!!.quantile(0.99),
                r[150]!!.quantile(0.99), r[250]!!.quantile(0.99)
            )
            System.out.printf("%d,%.3f\n", threads, t3 - t2)
        }
    }

    @Test
    fun megaMerge() {
        assumeTrue(java.lang.Boolean.parseBoolean(System.getProperty("runSlowTests")))
        val SUMMARIES = 1000
        val POINTS = 1000000
        val t0 = System.nanoTime() * 1e-9
        val data = DoubleArray(10013)
        val gen = Random()
        for (i in data.indices) {
            data[i] = gen.nextGaussian()
        }
        val t1 = System.nanoTime() * 1e-9
        System.out.printf("Data has been generated\n")

        // record the basic summaries
        val td = arrayOfNulls<MergingDigest>(SUMMARIES)
        var k = 0
        for (i in 0 until SUMMARIES) {
            if (i % 100 == 0) {
                System.out.printf("%d\n", i)
            }
            td[i] = MergingDigest(200.0)
            for (j in 0 until POINTS) {
                td[i]!!.add(data[k])
                k = (k + 1) % data.size
            }
        }
        System.out.printf("Partials built\n")
        val t2 = System.nanoTime() * 1e-9

        val tAll = MergingDigest(200.0)
        tAll.add(Lists.newArrayList<TDigest>(*td))
        val t3 = System.nanoTime() * 1e-9
        System.out.printf("%.3f, %.3f, %.3f\n", t1 - t0, t2 - t1, t3 - t2)
    }

    companion object {

        private val DAY = 280
        private val WIDTH = 1000
        private val DATA_STRIDE = 23
    }
}
