package com.tdunning.tdigest.quality

import com.tdunning.math.stats.AVLTreeDigest
import com.tdunning.math.stats.MergingDigest
import com.tdunning.math.stats.TDigest
import org.apache.mahout.math.jet.random.AbstractContinousDistribution
import org.apache.mahout.math.jet.random.Gamma
import org.apache.mahout.math.jet.random.Uniform
import java.util.*


/**
 * Handy routings for computing cdf and quantile from a list of numbers
 */
internal class Util {
    internal enum class Factory {
        MERGE {
            override fun create(compression: Double): TDigest {
                val digest = MergingDigest(compression, (10 * compression).toInt())
                digest.useAlternatingSort = true
                digest.useTwoLevelCompression = true
                return digest
            }

            override fun create(compression: Double, bufferSize: Int): TDigest {
                val digest = MergingDigest(compression, bufferSize)
                digest.useAlternatingSort = true
                digest.useTwoLevelCompression = true
                return digest
            }

            override fun create(): TDigest {
                return create(100.0)
            }
        },
        MERGE_OLD_STYLE {
            override fun create(compression: Double): TDigest {
                val digest = MergingDigest(compression, (10 * compression).toInt())
                digest.useAlternatingSort = false
                digest.useTwoLevelCompression = false
                return digest
            }

            override fun create(compression: Double, bufferSize: Int): TDigest {
                val digest = MergingDigest(compression, bufferSize)
                digest.useAlternatingSort = false
                digest.useTwoLevelCompression = false
                return digest
            }

            override fun create(): TDigest {
                return create(100.0)
            }
        },
        TREE {
            override fun create(compression: Double): TDigest {
                return AVLTreeDigest(compression)
            }

            override fun create(): TDigest {
                return create(20.0)
            }
        };

        abstract fun create(compression: Double): TDigest
        abstract fun create(): TDigest?
        open fun create(compression: Double, bufferSize: Int): TDigest {
            return create(compression)
        }
    }

    internal enum class Distribution {
        UNIFORM {
            override fun create(gen: Random?): AbstractContinousDistribution {
                return Uniform(0.0, 1.0, gen)
            }
        },
        GAMMA {
            override fun create(gen: Random?): AbstractContinousDistribution {
                return Gamma(0.1, 0.1, gen)
            }
        };

        abstract fun create(gen: Random?): AbstractContinousDistribution?
    }
}
