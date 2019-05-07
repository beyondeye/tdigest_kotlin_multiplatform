// Type definitions for tdigest-kt 0.1
// Project: https://github.com/beyondeye/tdigest_kotlin_multiplatform
// Definitions by: My Self <https://github.com/me>
// Definitions: https://github.com/DefinitelyTyped/DefinitelyTyped

export = tdigest_kt;

declare namespace tdigest {
    class Centroid {

    }
}
declare const tdigest_kt: {
    $$importsForInline$$: {
        "tdigest-kt": any;
    };
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
                    Centroid: any;
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
                    TDigest: any;
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

