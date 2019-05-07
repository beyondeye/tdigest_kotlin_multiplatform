// Type definitions for tdigest-kt 0.1
// Project: https://github.com/beyondeye/tdigest_kotlin_multiplatform
// Definitions by: My Self <https://github.com/me>
// Definitions: https://github.com/DefinitelyTyped/DefinitelyTyped

export = tdigest_kt;

declare namespace Kotlin {
    class Long {

    }
    interface Collection<T> {

    }
}
declare namespace tdigesttypes {
    class Centroid {
        Companion:Centroid$Companion
    }
    class Centroid$Companion {
    }
    interface BinaryOutput {

    }
    interface BinaryInput {

    }
    interface TDigest {
        /**
         * Adds a sample to a histogram
         *
         * @param x the sample to add
         * @param w the weight for the sample: MUST BE AN INTEGER
         */
        addWeightedSample(x: number, w: number):void
        updateSample(oldValue: number, newValue: number):void

        /**
         * Re-examines a t-digest to determine whether some centroids are redundant.  If your data are
         * perversely ordered, this may be a good idea.  Even if not, this may save 20% or so in space.
         *
         * The cost is roughly the same as adding as many data points as there are centroids.  This
         * is typically &lt; 10 * compression, but could be as high as 100 * compression.
         *
         * This is a destructive operation that is not thread-safe.
         */
        compress():void

        /**
         * Returns the number of points that have been added to this TDigest.
         *
         * @return The sum of the weights on all centroids.
         */
         size():Kotlin.Long


        /**
         * Returns the fraction of all points added which are  x.
         *
         * @param x The cutoff for the cdf.
         * @return The fraction of all data which is less or equal to x.
         */
        cdf(x: number): number

        /**
         * Returns an estimate of the cutoff such that a specified fraction of the data
         * added to this TDigest would be less than or equal to the cutoff.
         *
         * @param q The desired fraction
         * @return The value x such that cdf(x) == q
         */
        quantile(q: number): number

        /**
         * A [Collection] that lets you go through the centroids in ascending order by mean.  Centroids
         * returned will not be re-used, but may or may not share storage with this TDigest.
         *
         * @return The centroids in the form of a Collection.
         */
        centroids(): Kotlin.Collection<Centroid>

        /**
         * Returns the current compression factor.
         *
         * @return The compression factor originally used to set up the TDigest.
         */
        compression(): number

        /**
         * Returns the number of bytes required to encode this TDigest using #asBytes().
         *
         * @return The number of bytes required (an integer)
         */
        byteSize(): number

        /**
         * Returns the number of bytes required to encode this TDigest using #asSmallBytes().
         *
         * Note that this is just as expensive as actually compressing the digest. If you don't
         * care about time, but want to never over-allocate, this is fine. If you care about compression
         * and speed, you pretty much just have to overallocate by using allocating #byteSize() bytes.
         *
         * @return The number of bytes required.(an integer)
         */
        smallByteSize(): number


        /**
         * Serialize this TDigest into a byte buffer.  Note that the serialization used is
         * very straightforward and is considerably larger than strictly necessary.
         *
         * @param buf The byte buffer into which the TDigest should be serialized.
         */
        asBytes(buf: BinaryOutput):void

        /**
         * Serialize this TDigest into a byte buffer.  Some simple compression is used
         * such as using variable byte representation to store the centroid weights and
         * using delta-encoding on the centroid means so that floats can be reasonably
         * used to store the centroid means.
         *
         * @param buf The byte buffer into which the TDigest should be serialized.
         */
        asSmallBytes(buf: BinaryOutput):void

        /**
         * Add a sample to this TDigest.
         *
         * @param x The data value to add
         */
        addSample(x: number):void

        /**
         * Add all of the centroids of another TDigest to this one.
         *
         * @param other The other TDigest
         */
         addOtherDigest(other: TDigest):void

        centroidCount(): number


        Companion:TDigest$Companion
    }
    class TDigest$Companion {
        /**
         * create a TDigest with the specified compression
         * use [createAvlTreeDigest] or [createMergingDigest] if you use a specific algorithm
         * @param compression
         */
        createDigest(compression: number):TDigest
        createMergingDigest(compression: number): TDigest
        createAvlTreeDigest(compression: number): TDigest
    }
}
declare const tdigest_kt: {
    com: {
        tdunning: {
            math: {
                stats: {
                    AVLGroupTree: any;
                    AVLTreeDigest: any;
                    AVLTreeDigest_init_14dthe$: any;
                    AbstractTDigest: any;
                    BinaryInput: any;
                    BinaryInputFromByteBuffer: any;
                    BinaryOutput: any;
                    BinaryOutputFromByteBuffer: any;
                    Centroid: tdigesttypes.Centroid;
                    Centroid_init_12fank$: any;
                    Centroid_init_14dthe$: any;
                    Centroid_init_87xbef$: any;
                    Centroid_init_mqu1mq$: any;
                    Centroid_init_scvtgg$: any;
                    Dist: {
                        cdf_4avpt5$: any;
                        cdf_7si1j9$: any;
                        quantile_4avpt5$: any;
                        quantile_9iknyd$: any;
                    };
                    IntAVLTree: any;
                    MergingDigest: any;
                    ScaleFunction: any;
                    Sort: {
                        checkPartition_bmjou6$: any;
                        insertionSort_0: any;
                        insertionSort_1: any;
                        prng_0: {
                            addend_0: number;
                            nextBits_za3lpa$: any;
                            nextBoolean: any;
                            nextBytes_fqrh44$: any;
                            nextBytes_mj6st8$: any;
                            nextBytes_mj6st8$$default: any;
                            nextBytes_za3lpa$: any;
                            nextDouble: any;
                            nextDouble_14dthe$: any;
                            nextDouble_lu1900$: any;
                            nextFloat: any;
                            nextInt: any;
                            nextInt_vux9f0$: any;
                            nextInt_za3lpa$: any;
                            nextLong: any;
                            nextLong_3pjtqy$: any;
                            nextLong_s8cxhz$: any;
                            v_0: number;
                            w_0: number;
                            x_0: number;
                            y_0: number;
                            z_0: number;
                        };
                        quickSort_0: any;
                        quickSort_1: any;
                        reverse_6icyh1$: any;
                        reverse_nd5v6f$: any;
                        sort_808vjj$: any;
                        sort_f11c34$: any;
                        sort_kbza6$: any;
                        sort_tgjelr$: any;
                        swap_0: any;
                        swap_1: any;
                    };
                    TDigest: tdigesttypes.TDigest;
                    Utils: {
                        arraycopy_dgpv4k$: any;
                        arraycopy_lvhpry$: any;
                        arraycopy_m70dtq$: any;
                    };
                    buildBinaryInputFromB64_61zpoe$: any;
                    buildBinaryOutput_h6gvc6$: any;
                    mpassert_4ina18$: any;
                    mpassert_6taknv$: any;
                    toBinaryInput_2wezti$: any;
                    toBinaryOutput_2wezti$: any;
                };
            };
        };
    };
    sample: {
        Platform: {
            name: string;
        };
        Sample: any;
        hello: any;
    };
};

