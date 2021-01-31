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
 * Encodes the various scale functions for t-digests. These limits trade accuracy near the tails against accuracy near
 * the median in different ways. For instance, K_0 has uniform cluster sizes and results in constant accuracy (in terms
 * of q) while K_3 has cluster sizes proportional to min(q,1-q) which results in very much smaller error near the tails
 * and modestly increased error near the median.
 *
 *
 * The base forms (K_0, K_1, K_2 and K_3) all result in t-digests limited to a number of clusters equal to the
 * compression factor. The K_2_NO_NORM and K_3_NO_NORM versions result in the cluster count increasing roughly with
 * log(n).
 */
enum class ScaleFunction {
    /**
     * Generates uniform cluster sizes. Used for comparison only.
     */
    K_0 {
        override fun k(q: Double, compression: Double, n: Double): Double {
            return compression * q / 2
        }

        override fun k(q: Double, normalizer: Double): Double {
            return normalizer * q
        }

        override fun q(k: Double, compression: Double, n: Double): Double {
            return 2 * k / compression
        }

        override fun q(k: Double, normalizer: Double): Double {
            return k / normalizer
        }

        override fun max(q: Double, compression: Double, n: Double): Double {
            return 2 / compression
        }

        override fun max(q: Double, normalizer: Double): Double {
            return 1 / normalizer
        }

        override fun normalizer(compression: Double, n: Double): Double {
            return compression / 2
        }
    },

    /**
     * Generates cluster sizes proportional to sqrt(q*(1-q)). This gives constant relative accuracy if accuracy is
     * proportional to squared cluster size. It is expected that K_2 and K_3 will give better practical results.
     */
    K_1 {
        override fun k(q: Double, compression: Double, n: Double): Double {
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return compression * kotlin.math.asin(2 * q - 1) / (2 * kotlin.math.PI)
                }
            }
            return limitCall(f, q, 1e-15, 1 - 1e-15)
        }

        override fun k(q: Double, normalizer: Double): Double {
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return normalizer * kotlin.math.asin(2 * q - 1)
                }
            }
            return limitCall(f, q, 1e-15, 1 - 1e-15)
        }

        override fun q(k: Double, compression: Double, n: Double): Double {
            val f: Function = object : Function() {
                override fun apply(k: Double): Double {
                    return (kotlin.math.sin(k * (2 * kotlin.math.PI / compression)) + 1) / 2
                }
            }
            return limitCall(f, k, -compression / 4, compression / 4)
        }

        override fun q(k: Double, normalizer: Double): Double {
            val f: Function = object : Function() {
                override fun apply(x: Double): Double {
                    return (kotlin.math.sin(x) + 1) / 2
                }
            }
            val x = k / normalizer
            return limitCall(f, x, -kotlin.math.PI / 2, kotlin.math.PI / 2)
        }

        override fun max(q: Double, compression: Double, n: Double): Double {
            return if (q <= 0) {
                0.0
            } else if (q >= 1) {
                0.0
            } else {
                2 * kotlin.math.sin(kotlin.math.PI / compression) * kotlin.math.sqrt(q * (1 - q))
            }
        }

        override fun max(q: Double, normalizer: Double): Double {
            return if (q <= 0) {
                0.0
            } else if (q >= 1) {
                0.0
            } else {
                2 * kotlin.math.sin(0.5 / normalizer) * kotlin.math.sqrt(q * (1 - q))
            }
        }

        override fun normalizer(compression: Double, n: Double): Double {
            return compression / (2 * kotlin.math.PI)
        }
    },

    /**
     * Generates cluster sizes proportional to sqrt(q*(1-q)) but avoids computation of asin in the critical path by
     * using an approximate version.
     */
    K_1_FAST {
        override fun k(q: Double, compression: Double, n: Double): Double {
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return compression * fastAsin(2 * q - 1) / (2 * kotlin.math.PI)
                }
            }
            return limitCall(f, q, 0.0, 1.0)
        }

        override fun k(q: Double, normalizer: Double): Double {
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return normalizer * fastAsin(2 * q - 1)
                }
            }
            return limitCall(f, q, 0.0, 1.0)
        }

        override fun q(k: Double, compression: Double, n: Double): Double {
            return (kotlin.math.sin(k * (2 * kotlin.math.PI / compression)) + 1) / 2
        }

        override fun q(k: Double, normalizer: Double): Double {
            return (kotlin.math.sin(k / normalizer) + 1) / 2
        }

        override fun max(q: Double, compression: Double, n: Double): Double {
            return if (q <= 0) {
                0.0
            } else if (q >= 1) {
                0.0
            } else {
                2 * kotlin.math.sin(kotlin.math.PI / compression) * kotlin.math.sqrt(q * (1 - q))
            }
        }

        override fun max(q: Double, normalizer: Double): Double {
            return if (q <= 0) {
                0.0
            } else if (q >= 1) {
                0.0
            } else {
                2 * kotlin.math.sin(0.5 / normalizer) * kotlin.math.sqrt(q * (1 - q))
            }
        }

        override fun normalizer(compression: Double, n: Double): Double {
            return compression / (2 * kotlin.math.PI)
        }
    },

    /**
     * Generates cluster sizes proportional to q*(1-q). This makes tail error bounds tighter than for K_1. The use of a
     * normalizing function results in a strictly bounded number of clusters no matter how many samples.
     */
    K_2 {
        override fun k(q: Double, compression: Double, n: Double): Double {
            if (n <= 1) {
                return if (q <= 0) {
                    (-10).toDouble()
                } else if (q >= 1) {
                    10.0
                } else {
                    0.0
                }
            }
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return compression * kotlin.math.ln(q / (1 - q)) / Z(compression, n)
                }
            }
            return limitCall(f, q, 1e-15, 1 - 1e-15)
        }

        override fun k(q: Double, normalizer: Double): Double {
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return kotlin.math.ln(q / (1 - q)) * normalizer
                }
            }
            return limitCall(f, q, 1e-15, 1 - 1e-15)
        }

        override fun q(k: Double, compression: Double, n: Double): Double {
            val w: Double = kotlin.math.exp(k * Z(compression, n) / compression)
            return w / (1 + w)
        }

        override fun q(k: Double, normalizer: Double): Double {
            val w: Double = kotlin.math.exp(k / normalizer)
            return w / (1 + w)
        }

        override fun max(q: Double, compression: Double, n: Double): Double {
            return Z(compression, n) * q * (1 - q) / compression
        }

        override fun max(q: Double, normalizer: Double): Double {
            return q * (1 - q) / normalizer
        }

        override fun normalizer(compression: Double, n: Double): Double {
            return compression / Z(compression, n)
        }

        private fun Z(compression: Double, n: Double): Double {
            return 4 * kotlin.math.ln(n / compression) + 24
        }
    },

    /**
     * Generates cluster sizes proportional to min(q, 1-q). This makes tail error bounds tighter than for K_1 or K_2.
     * The use of a normalizing function results in a strictly bounded number of clusters no matter how many samples.
     */
    K_3 {
        override fun k(q: Double, compression: Double, n: Double): Double {
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return if (q <= 0.5) {
                        compression * kotlin.math.ln(2 * q) / Z(compression, n)
                    } else {
                        -k(1 - q, compression, n)
                    }
                }
            }
            return limitCall(f, q, 1e-15, 1 - 1e-15)
        }

        override fun k(q: Double, normalizer: Double): Double {
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return if (q <= 0.5) {
                        kotlin.math.ln(2 * q) * normalizer
                    } else {
                        -k(1 - q, normalizer)
                    }
                }
            }
            return limitCall(f, q, 1e-15, 1 - 1e-15)
        }

        override fun q(k: Double, compression: Double, n: Double): Double {
            return if (k <= 0) {
                kotlin.math.exp(k * Z(compression, n) / compression) / 2
            } else {
                1 - q(-k, compression, n)
            }
        }

        override fun q(k: Double, normalizer: Double): Double {
            return if (k <= 0) {
                kotlin.math.exp(k / normalizer) / 2
            } else {
                1 - q(-k, normalizer)
            }
        }

        override fun max(q: Double, compression: Double, n: Double): Double {
            return Z(compression, n) * kotlin.math.min(q, 1 - q) / compression
        }

        override fun max(q: Double, normalizer: Double): Double {
            return kotlin.math.min(q, 1 - q) / normalizer
        }

        override fun normalizer(compression: Double, n: Double): Double {
            return compression / Z(compression, n)
        }

        private fun Z(compression: Double, n: Double): Double {
            return 4 * kotlin.math.ln(n / compression) + 21
        }
    },

    /**
     * Generates cluster sizes proportional to q*(1-q). This makes the tail error bounds tighter. This version does not
     * use a normalizer function and thus the number of clusters increases roughly proportional to log(n). That is good
     * for accuracy, but bad for size and bad for the statically allocated MergingDigest, but can be useful for
     * tree-based implementations.
     */
    K_2_NO_NORM {
        override fun k(q: Double, compression: Double, n: Double): Double {
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return compression * kotlin.math.ln(q / (1 - q))
                }
            }
            return limitCall(f, q, 1e-15, 1 - 1e-15)
        }

        override fun k(q: Double, normalizer: Double): Double {
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return normalizer * kotlin.math.ln(q / (1 - q))
                }
            }
            return limitCall(f, q, 1e-15, 1 - 1e-15)
        }

        override fun q(k: Double, compression: Double, n: Double): Double {
            val w: Double = kotlin.math.exp(k / compression)
            return w / (1 + w)
        }

        override fun q(k: Double, normalizer: Double): Double {
            val w: Double = kotlin.math.exp(k / normalizer)
            return w / (1 + w)
        }

        override fun max(q: Double, compression: Double, n: Double): Double {
            return q * (1 - q) / compression
        }

        override fun max(q: Double, normalizer: Double): Double {
            return q * (1 - q) / normalizer
        }

        override fun normalizer(compression: Double, n: Double): Double {
            return compression
        }
    },

    /**
     * Generates cluster sizes proportional to min(q, 1-q). This makes the tail error bounds tighter. This version does
     * not use a normalizer function and thus the number of clusters increases roughly proportional to log(n). That is
     * good for accuracy, but bad for size and bad for the statically allocated MergingDigest, but can be useful for
     * tree-based implementations.
     */
    K_3_NO_NORM {
        override fun k(q: Double, compression: Double, n: Double): Double {
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return if (q <= 0.5) {
                        compression * kotlin.math.ln(2 * q)
                    } else {
                        -k(1 - q, compression, n)
                    }
                }
            }
            return limitCall(f, q, 1e-15, 1 - 1e-15)
        }

        override fun k(q: Double, normalizer: Double): Double {
            // poor man's lambda, sigh
            val f: Function = object : Function() {
                override fun apply(q: Double): Double {
                    return if (q <= 0.5) {
                        normalizer * kotlin.math.ln(2 * q)
                    } else {
                        -k(1 - q, normalizer)
                    }
                }
            }
            return limitCall(f, q, 1e-15, 1 - 1e-15)
        }

        override fun q(k: Double, compression: Double, n: Double): Double {
            return if (k <= 0) {
                kotlin.math.exp(k / compression) / 2
            } else {
                1 - q(-k, compression, n)
            }
        }

        override fun q(k: Double, normalizer: Double): Double {
            return if (k <= 0) {
                kotlin.math.exp(k / normalizer) / 2
            } else {
                1 - q(-k, normalizer)
            }
        }

        override fun max(q: Double, compression: Double, n: Double): Double {
            return kotlin.math.min(q, 1 - q) / compression
        }

        override fun max(q: Double, normalizer: Double): Double {
            return kotlin.math.min(q, 1 - q) / normalizer
        }

        override fun normalizer(compression: Double, n: Double): Double {
            return compression
        }
    };
    // max weight is min(q,1-q), should improve tail accuracy even more
    /**
     * Converts a quantile to the k-scale. The total number of points is also provided so that a normalizing function
     * can be computed if necessary.
     *
     * @param q           The quantile
     * @param compression Also known as delta in literature on the t-digest
     * @param n           The total number of samples
     * @return The corresponding value of k
     */
    abstract fun k(q: Double, compression: Double, n: Double): Double

    /**
     * Converts  a quantile to the k-scale. The normalizer value depends on compression and (possibly) number of points
     * in the digest. #normalizer(double, double)
     *
     * @param q          The quantile
     * @param normalizer The normalizer value which depends on compression and (possibly) number of points in the
     * digest.
     * @return The corresponding value of k
     */
    abstract fun k(q: Double, normalizer: Double): Double

    /**
     * Computes q as a function of k. This is often faster than finding k as a function of q for some scales.
     *
     * @param k           The index value to convert into q scale.
     * @param compression The compression factor (often written as )
     * @param n           The number of samples already in the digest.
     * @return The value of q that corresponds to k
     */
    abstract fun q(k: Double, compression: Double, n: Double): Double

    /**
     * Computes q as a function of k. This is often faster than finding k as a function of q for some scales.
     *
     * @param k          The index value to convert into q scale.
     * @param normalizer The normalizer value which depends on compression and (possibly) number of points in the
     * digest.
     * @return The value of q that corresponds to k
     */
    abstract fun q(k: Double, normalizer: Double): Double

    /**
     * Computes the maximum relative size a cluster can have at quantile q. Note that exactly where within the range
     * spanned by a cluster that q should be isn't clear. That means that this function usually has to be taken at
     * multiple points and the smallest value used.
     *
     *
     * Note that this is the relative size of a cluster. To get the max number of samples in the cluster, multiply this
     * value times the total number of samples in the digest.
     *
     * @param q           The quantile
     * @param compression The compression factor, typically delta in the literature
     * @param n           The number of samples seen so far in the digest
     * @return The maximum number of samples that can be in the cluster
     */
    abstract fun max(q: Double, compression: Double, n: Double): Double

    /**
     * Computes the maximum relative size a cluster can have at quantile q. Note that exactly where within the range
     * spanned by a cluster that q should be isn't clear. That means that this function usually has to be taken at
     * multiple points and the smallest value used.
     *
     *
     * Note that this is the relative size of a cluster. To get the max number of samples in the cluster, multiply this
     * value times the total number of samples in the digest.
     *
     * @param q          The quantile
     * @param normalizer The normalizer value which depends on compression and (possibly) number of points in the
     * digest.
     * @return The maximum number of samples that can be in the cluster
     */
    abstract fun max(q: Double, normalizer: Double): Double

    /**
     * Computes the normalizer given compression and number of points.
     */
    abstract fun normalizer(compression: Double, n: Double): Double
    internal abstract class Function {
        abstract fun apply(x: Double): Double
    }

    companion object {
        /**
         * Approximates asin to within about 1e-6. This approximation works by breaking the range from 0 to 1 into 5 regions
         * for all but the region nearest 1, rational polynomial models get us a very good approximation of asin and by
         * interpolating as we move from region to region, we can guarantee continuity and we happen to get monotonicity as
         * well.  for the values near 1, we just use Math.asin as our region "approximation".
         *
         * @param x sin(theta)
         * @return theta
         */
        fun fastAsin(x: Double): Double {
            return if (x < 0) {
                -fastAsin(-x)
            } else if (x > 1) {
                Double.NaN
            } else {
                // Cutoffs for models. Note that the ranges overlap. In the
                // overlap we do linear interpolation to guarantee the overall
                // result is "nice"
                val c0High = 0.1
                val c1High = 0.55
                val c2Low = 0.5
                val c2High = 0.8
                val c3Low = 0.75
                val c3High = 0.9
                val c4Low = 0.87
                if (x > c3High) {
                    kotlin.math.asin(x)
                } else {
                    // the models
                    val m0 = doubleArrayOf(
                        0.2955302411,
                        1.2221903614,
                        0.1488583743,
                        0.2422015816,
                        -0.3688700895,
                        0.0733398445
                    )
                    val m1 = doubleArrayOf(
                        -0.0430991920,
                        0.9594035750,
                        -0.0362312299,
                        0.1204623351,
                        0.0457029620,
                        -0.0026025285
                    )
                    val m2 = doubleArrayOf(
                        -0.034873933724,
                        1.054796752703,
                        -0.194127063385,
                        0.283963735636,
                        0.023800124916,
                        -0.000872727381
                    )
                    val m3 = doubleArrayOf(
                        -0.37588391875,
                        2.61991859025,
                        -2.48835406886,
                        1.48605387425,
                        0.00857627492,
                        -0.00015802871
                    )

                    // the parameters for all of the models
                    val vars = doubleArrayOf(1.0, x, x * x, x * x * x, 1 / (1 - x), 1 / (1 - x) / (1 - x))

                    // raw grist for interpolation coefficients
                    val x0 = bound((c0High - x) / c0High)
                    val x1 = bound((c1High - x) / (c1High - c2Low))
                    val x2 = bound((c2High - x) / (c2High - c3Low))
                    val x3 = bound((c3High - x) / (c3High - c4Low))

                    // interpolation coefficients
                    val mix1 = (1 - x0) * x1
                    val mix2 = (1 - x1) * x2
                    val mix3 = (1 - x2) * x3
                    val mix4 = 1 - x3

                    // now mix all the results together, avoiding extra evaluations
                    var r = 0.0
                    if (x0 > 0) {
                        r += x0 * eval(m0, vars)
                    }
                    if (mix1 > 0) {
                        r += mix1 * eval(m1, vars)
                    }
                    if (mix2 > 0) {
                        r += mix2 * eval(m2, vars)
                    }
                    if (mix3 > 0) {
                        r += mix3 * eval(m3, vars)
                    }
                    if (mix4 > 0) {
                        // model 4 is just the real deal
                        r += mix4 * kotlin.math.asin(x)
                    }
                    r
                }
            }
        }

        internal fun limitCall(f: Function, x: Double, low: Double, high: Double): Double {
            return if (x < low) {
                f.apply(low)
            } else if (x > high) {
                f.apply(high)
            } else {
                f.apply(x)
            }
        }

        private fun eval(model: DoubleArray, vars: DoubleArray): Double {
            var r = 0.0
            for (i in model.indices) {
                r += model[i] * vars[i]
            }
            return r
        }

        private fun bound(v: Double): Double {
            return if (v <= 0) {
                0.0
            } else if (v >= 1) {
                1.0
            } else {
                v
            }
        }
    }
}