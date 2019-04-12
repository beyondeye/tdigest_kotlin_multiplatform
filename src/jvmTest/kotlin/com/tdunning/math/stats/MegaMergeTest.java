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

package com.tdunning.math.stats;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static org.junit.Assume.assumeTrue;

public class MegaMergeTest {

    private static final int DAY = 280;
    private static final int WIDTH = 1000;
    private static final int DATA_STRIDE = 23;

    @Test
    public void testLargeMerge() throws InterruptedException, ExecutionException {
        assumeTrue(Boolean.parseBoolean(System.getProperty("runSlowTests")));
        // prove we can summarize a days worth of data at 5 minute intervals. Each interval has
        // 1000 samples each with 1500 data points
        double t0 = System.nanoTime() * 1e-9;
        // we cheat by only having 23 samples that we rotate into the data summaries
        // the raw data
        double[][] data = new double[DATA_STRIDE][1500];
        Random gen = new Random();
        for (int i = 0; i < DATA_STRIDE; i++) {
            for (int j = 0; j < 1500; j++) {
                data[i][j] = gen.nextGaussian();
            }
        }
        double t1 = System.nanoTime() * 1e-9;
        System.out.printf("Data has been generated\n");
        // record the basic summaries
        final MergingDigest[][] td = new MergingDigest[DAY][WIDTH];
        int m = 0;
        for (int i = 0; i < DAY; i++) {
            if (i % 10 == 0) {
                System.out.printf("%d\n", i);
            }
            for (int j = 0; j < WIDTH; j++) {
                td[i][j] = new MergingDigest(100);
                for (int k = 0; k < 1500; k++) {
                    td[i][j].add(data[m][k]);
                }
                m = (m + 1) % DATA_STRIDE;
            }
        }
        double t2 = System.nanoTime() * 1e-9;
        System.out.printf("Micro-summaries filled\n");
        System.out.printf("%.3f,%.3f\n", t1 - t0, t2 - t1);
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.printf("using %d cores\n", cores);
        for (int threads = 1; threads < 2 * cores; threads++) {
            t2 = System.nanoTime() * 1e-9;
            // pull the summaries together into 288 reasonably high resolution t-digests
            List<Callable<MergingDigest>> tasks = new ArrayList<>();
            for (int i = 0; i < DAY; i++) {
                final MergingDigest[] elements = td[i];
                tasks.add(new Callable<MergingDigest>() {
                    @Override
                    public MergingDigest call() throws Exception {
                        MergingDigest rx = new MergingDigest(1000);
                        rx.add(Lists.newArrayList(elements));
                        return rx;
                    }
                });
            }
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            List<Future<MergingDigest>> results = pool.invokeAll(tasks);
            final MergingDigest[] r = new MergingDigest[DAY];
            try {
                int i = 0;
                for (Future<MergingDigest> result : results) {
                    r[i++] = result.get();
                }
            } finally {
                pool.shutdown();
                pool.awaitTermination(2, TimeUnit.SECONDS);
            }
            double t3 = System.nanoTime() * 1e-9;
            System.out.printf("%.3f,%.3f,%.3f,%.3f\n",
                    r[0].quantile(0.99), r[100].quantile(0.99),
                    r[150].quantile(0.99), r[250].quantile(0.99));
            System.out.printf("%d,%.3f\n", threads, t3 - t2);
        }
    }
}
