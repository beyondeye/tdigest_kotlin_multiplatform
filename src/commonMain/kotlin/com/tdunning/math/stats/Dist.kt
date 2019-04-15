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
 * Reference implementations for cdf and quantile if we have all data.
 */
object Dist {
    fun cdf(x: Double, data: DoubleArray): Double {
        var n1 = 0
        var n2 = 0
        for (v in data) {
            n1 += if (v < x) 1 else 0
            n2 += if (v == x) 1 else 0
        }
        return (n1 + n2 / 2.0) / data.size
    }

    fun cdf(x: Double, data: Collection<Double>): Double {
        var n1 = 0
        var n2 = 0
        for (v in data) {
            n1 += if (v < x) 1 else 0
            n2 += if (v == x) 1 else 0
        }
        return (n1 + n2 / 2.0) / data.size
    }

    fun quantile(q: Double, data: DoubleArray): Double {
        val n = data.size
        if (n == 0) {
            return Double.NaN
        }
        var index = q * n
        if (index < 0) {
            index = 0.0
        }
        if (index > n - 1) {
            index = (n - 1).toDouble()
        }
        return data[kotlin.math.floor(index).toInt()]
    }

    fun quantile(q: Double, data: List<Double>): Double {
        val n = data.size
        if (n == 0) {
            return Double.NaN
        }
        var index = q * n
        if (index < 0) {
            index = 0.0
        }
        if (index > n - 1) {
            index = (n - 1).toDouble()
        }
        return data[kotlin.math.floor(index).toInt()]
    }
}
