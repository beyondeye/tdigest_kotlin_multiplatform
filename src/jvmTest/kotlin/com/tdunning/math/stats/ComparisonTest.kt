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

import java.io.FileNotFoundException
import java.io.PrintStream
import java.util.Random

import org.junit.Assert.*

class ComparisonTest {
    /**
     * This is a demo as well as a test. The scenario is that we have a thing that
     * normally has a moderately long-tailed distribution of response times. Then
     * some small fraction of transactions take 5x longer than normal. We need to
     * detect this by looking at the overall response time distribution.
     */
    @Test
    @Throws(FileNotFoundException::class)
    fun detectLatencyProblem() {
        val gen = Random()

        PrintStream("detector.csv").use { out ->
            out.printf("name,rate,t,failure,llr\n")
            runSimulation(gen, TdigestDetector(), out, 1000.0)
            runSimulation(gen, TdigestDetector(), out, 100.0)
            runSimulation(gen, TdigestDetector(), out, 10.0)

            runSimulation(gen, LogHistogramDetector(), out, 1000.0)
            runSimulation(gen, LogHistogramDetector(), out, 100.0)
            runSimulation(gen, LogHistogramDetector(), out, 10.0)
        }
    }

    private fun runSimulation(gen: Random, d: Detector, out: PrintStream, rate: Double) {
        val dt = 1 / rate

        var t = 0.0
        var currentMinute = 0.0

        // compare the distribution each minute against the previous hour
        var failureRate = 0.0
        while (t < 2 * 7200) {
            if (t - currentMinute >= 60) {
                currentMinute += 60.0
                if (d.isReady) {
                    out.printf(
                        "%s, %.0f, %.0f, %.0f, %.3f\n",
                        d.name(), rate, currentMinute, if (failureRate > 0) -Math.log10(failureRate) else 0.0, d.score()
                    )
                }
                d.flush()
            }

            if (t >= 7200) {
                // after one hour of no failure, we add 0.1% failures, half an hour later we go to 1% failure rate
                if (t >= 7200 + 3600) {
                    failureRate = 0.01
                } else {
                    failureRate = 0.001
                }
            } else {
                failureRate = 0.0
            }

            d.add(latencySampler(failureRate, gen))
            t += -dt * Math.log(gen.nextDouble())
        }
    }

    private interface Detector {
        val isReady: Boolean
        fun add(sample: Double)
        fun flush()
        fun score(): Double
        fun name(): String
    }

    private class TdigestDetector : Detector {
        internal var cuts = doubleArrayOf(0.9, 0.99, 0.999, 0.9999)

        internal var history: MutableList<TDigest> = Lists.newArrayList()
        internal var current: TDigest = MergingDigest(100.0)

        override val isReady: Boolean
            get() = history.size >= 60

        override fun add(sample: Double) {
            current.add(sample)
        }

        override fun flush() {
            history.add(current)
            current = MergingDigest(100.0)
        }

        override fun score(): Double {
            val ref = MergingDigest(100.0)
            ref.add(history.subList(history.size - 60, history.size))
            return Comparison.compareChi2(ref, current, cuts)
        }

        override fun name(): String {
            return "t-digest"
        }
    }

    private class LogHistogramDetector : Detector {
        internal var history: MutableList<Histogram> = Lists.newArrayList()
        internal var current = LogHistogram(0.1e-3, 1.0)

        override val isReady: Boolean
            get() = history.size >= 60

        override fun add(sample: Double) {
            current.add(sample)
        }

        override fun flush() {
            history.add(current)
            current = LogHistogram(0.1e-3, 1.0)
        }

        override fun score(): Double {
            val ref = LogHistogram(0.1e-3, 1.0)
            ref.add(history)
            return Comparison.compareChi2(ref, current)
        }

        override fun name(): String {
            return "log-histogram"
        }
    }

    private fun latencySampler(failed: Double, gen: Random): Double {
        return if (gen.nextDouble() < failed) {
            50e-3 * Math.exp(gen.nextGaussian() / 2)
        } else {
            10e-3 * Math.exp(gen.nextGaussian() / 2)
        }
    }

    @Test
    fun compareMergingDigests() {
        var d1: TDigest = MergingDigest(100.0)
        var d2: TDigest = MergingDigest(100.0)

        d1.add(1.0)
        d2.add(3.0)
        assertEquals(2.77, Comparison.compareChi2(d1, d2, doubleArrayOf(1.0)), 0.01)

        val r = Random()
        var failed = 0
        for (i in 0..999) {
            d1 = MergingDigest(100.0)
            d2 = MergingDigest(100.0)
            val d3 = MergingDigest(100.0)
            for (j in 0..9999) {
                // these should look the same
                d1.add(r.nextGaussian())
                d2.add(r.nextGaussian())
                // can we see a small difference
                d3.add(r.nextGaussian() + 0.3)
            }

            // 5 degrees of freedom, Pr(llr > 20) < 0.005
            if (Comparison.compareChi2(d1, d2, doubleArrayOf(0.1, 0.3, 0.5, 0.8, 0.9)) > 25) {
                failed++
            }

            // 1 degree of freedom, Pr(llr > 10) < 0.005
            if (Comparison.compareChi2(d1, d2, doubleArrayOf(0.1)) > 20) {
                failed++
            }

            // 1 degree of freedom, Pr(llr > 10) < 0.005
            if (Comparison.compareChi2(d1, d2, doubleArrayOf(0.5)) > 20) {
                failed++
            }

            if (Comparison.compareChi2(d1, d3, doubleArrayOf(0.1, 0.5, 0.9)) < 90) {
                failed++
            }
        }
        assertEquals(0f, failed.toFloat(), 5f)
        System.out.printf("Failed %d times (up to 5 acceptable)", failed)
    }

    @Test
    fun ks() {
        val r = Random()
        var mean = 0.0
        var s2 = 0.0
        for (i in 0..9) {
            val d1 = MergingDigest(100.0)
            val d2 = MergingDigest(100.0)
            val d3 = MergingDigest(100.0)
            for (j in 0..999999) {
                d1.add(r.nextGaussian())
                d2.add(r.nextGaussian() + 1)
                d3.add(r.nextGaussian())
            }
            val ks = Comparison.ks(d1, d2)
            // this value is slightly lower than it should be (by about 0.9)
            assertEquals(269.5, ks, 3.0)
            val newMean = mean + (ks - mean) / (i + 1)
            s2 += (ks - mean) * (ks - newMean)
            mean = newMean

            assertEquals(0.0, Comparison.ks(d1, d3), 3.5)
        }

        System.out.printf("%.5f %.5f\n", mean, Math.sqrt(s2 / 10))
    }

    @Test
    fun compareLogHistograms() {
        val r = Random()
        var failed = 0

        try {
            Comparison.compareChi2(LogHistogram(10e-6, 10.0), LogHistogram(1e-6, 1.0))
            fail("Should have detected incompatible histograms (lower bound)")
        } catch (e: IllegalArgumentException) {
            assertEquals("Incompatible histograms in terms of size or bounds", e.message)
        }

        try {
            Comparison.compareChi2(LogHistogram(10e-6, 10.0), LogHistogram(10e-6, 1.0))
            fail("Should have detected incompatible histograms (size)")
        } catch (e: IllegalArgumentException) {
            assertEquals("Incompatible histograms in terms of size or bounds", e.message)
        }

        for (i in 0..999) {
            val d1 = LogHistogram(10e-6, 10.0)
            val d2 = LogHistogram(10e-6, 10.0)
            val d3 = LogHistogram(10e-6, 10.0)
            for (j in 0..9999) {
                // these should look the same
                d1.add(Math.exp(r.nextGaussian()))
                d2.add(Math.exp(r.nextGaussian()))
                // can we see a small difference
                d3.add(Math.exp(r.nextGaussian() + 0.5))
            }

            // 144 degrees of freedom, Pr(llr > 250) < 1e-6
            if (Comparison.compareChi2(d1, d2) > 250) {
                failed++
            }

            if (Comparison.compareChi2(d1, d3) < 1000) {
                failed++
            }
        }
        assertEquals(0f, failed.toFloat(), 5f)
    }

    @Test
    fun llr() {
        val count = Array(2) { DoubleArray(2) }
        count[0][0] = 1.0
        count[1][1] = 1.0
        assertEquals(2.77, Comparison.llr(count), 0.01)

        count[0][0] = 3.0
        count[0][1] = 1.0
        count[1][0] = 1.0
        count[1][1] = 3.0
        assertEquals(2.09, Comparison.llr(count), 0.01)

        count[1][1] = 5.0
        assertEquals(3.55, Comparison.llr(count), 0.01)
    }
}