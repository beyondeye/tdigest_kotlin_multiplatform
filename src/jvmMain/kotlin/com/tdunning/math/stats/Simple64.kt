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


import java.nio.LongBuffer

/**
 * Very simple variable byte encoding that always uses 64bit units.  The idea is that the next few values
 * are smashed into 64 bits using a few bits to indicate how they are fitted in and the rest of the bits
 * to fit each value into equal-sized chunks.
 *
 * In this encoding, 4 bits are used to indicate how the remaining 60 bits are divided. The possible ways are shown
 * in the following table:
 * <table>
 * <tr><th>Code</th><th>Arrangement</th></tr>
 * <tr><td>14</td> <td>1 X 60BITS</td></tr>
 * <tr><td>13</td> <td>2 X 30BITS</td></tr>
 * <tr><td>12</td> <td>3 X 20BITS</td></tr>
 * <tr><td>11</td> <td>4 X 15BITS</td></tr>
 * <tr><td>10</td> <td>5 X 12BITS</td></tr>
 * <tr><td>9</td>  <td>6 X 10BITS</td></tr>
 * <tr><td>8</td>  <td> 7 X 8BITS</td></tr>
 * <tr><td>7</td>  <td> 8 X 7BITS </td></tr>
 * <tr><td>6</td>  <td>10 X 6BITS</td></tr>
 * <tr><td>5</td>  <td>12 X 5BITS</td></tr>
 * <tr><td>4</td>  <td>15 X 4BITS</td></tr>
 * <tr><td>3</td>  <td>20 X 3BITS</td></tr>
 * <tr><td>2</td>  <td>30 X 2BITS</td></tr>
 * <tr><td>1</td>  <td>60 X 1BITS</td></tr>
 * <caption>Size codes for Simple64 compression</caption>
</table> *
 */
class Simple64 {

    private var inputCompressable = 1
    private var minBits = 1
    private var maxFitPlus1 = (1 shl minBits).toLong()
    private val pending = LongArray(100)     // nocommit -- 60 or 61 should do?
    private var inputCount: Int = 0

    private fun reset() {
        inputCompressable = 1
        minBits = 1
        inputCount = 0
        maxFitPlus1 = (1 shl minBits).toLong()
    }

    // nocommit -- need low level test that streaming api
    // didn't break anything

    // Returns 0 if no new long written, else returns number
    // of input values and out[0] has the long to write
    fun add(v: Long, out: LongArray): Int {
        //System.out.println("S64.add v=" + v + " " + (1 + inputCount - inputCompressable) + " waiting");
        pending[inputCount++] = v
        while (inputCompressable <= inputCount) {
            val nextData = pending[inputCompressable - 1]
            //System.out.println("  cycle: data=" + nextData);
            while (nextData >= maxFitPlus1 && minBits < NUM_DATA_BITS) {
                //System.out.println("  cycle maxFitPlus1=" + maxFitPlus1 + " minBits=" + minBits);
                if (minBits == 7 && inputCompressable == 8 && nextData < maxFitPlus1 shl 4) {
                    break
                } else if (minBits == 8 && inputCompressable == 7 && nextData < maxFitPlus1 shl 4) {
                    break
                } else {
                    //System.out.println("  advance");
                    minBits++
                    maxFitPlus1 = maxFitPlus1 shl 1
                    if (inputCompressable * minBits > NUM_DATA_BITS) {
                        inputCompressable--
                        //System.out.println("  hard break");
                        break
                    }
                }
            }
            inputCompressable++

            //System.out.println("  minBits=" + minBits + " count=" + (inputCompressable-1) + " inputCount=" + inputCount);

            if (inputCompressable * minBits > NUM_DATA_BITS) {
                // Time to compress!
                inputCompressable--
                //System.out.println("  FLUSH count=" + inputCompressable);

                // nocommit -- it should always be > 0... right??
                mpassert(inputCompressable > 0)

                // Check whether a bigger number of bits can be used:
                while (inputCompressable * (minBits + 1) <= NUM_DATA_BITS) {
                    minBits++
                    //System.out.println("  incr minBits=" + minBits);
                }

                /*
          if (((inputCompressable+1) * minBits) <= NUM_DATA_BITS) {
          // not enough input available for minBits
          minBits++;
          // do not compress all available input
          inputCompressable = NUM_DATA_BITS / minBits;
          }
        */

                // Put compression method in status bits and encode input data
                var s9: Long
                val consumed: Int
                when (minBits) {
                    // add status bits and later input values
                    60 -> {
                        s9 = STATUS_1NUM_60BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        consumed = 1
                    }
                    30 -> {
                        s9 = STATUS_2NUM_30BITS.toLong()
                        // nocommit -- make a single expr instead of |'ing ?
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 34)
                        consumed = 2
                    }
                    20 -> {
                        s9 = STATUS_3NUM_20BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 24)
                        s9 = s9 or (pending[2] shl 44)
                        consumed = 3
                    }
                    15 -> {
                        s9 = STATUS_4NUM_15BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 19)
                        s9 = s9 or (pending[2] shl 34)
                        s9 = s9 or (pending[3] shl 49)
                        consumed = 4
                    }
                    12 -> {
                        s9 = STATUS_5NUM_12BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 16)
                        s9 = s9 or (pending[2] shl 28)
                        s9 = s9 or (pending[3] shl 40)
                        s9 = s9 or (pending[4] shl 52)
                        consumed = 5
                    }
                    10 -> {
                        s9 = STATUS_6NUM_10BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 14)
                        s9 = s9 or (pending[2] shl 24)
                        s9 = s9 or (pending[3] shl 34)
                        s9 = s9 or (pending[4] shl 44)
                        s9 = s9 or (pending[5] shl 54)
                        consumed = 6
                    }
                    8 -> {
                        s9 = STATUS_7NUM_8BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 12)
                        s9 = s9 or (pending[2] shl 20)
                        s9 = s9 or (pending[3] shl 28)
                        s9 = s9 or (pending[4] shl 36)
                        s9 = s9 or (pending[5] shl 44)
                        s9 = s9 or (pending[6] shl 52) // 4 more bits
                        consumed = 7
                    }
                    7 -> {
                        s9 = STATUS_8NUM_7BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 11)
                        s9 = s9 or (pending[2] shl 18)
                        s9 = s9 or (pending[3] shl 25)
                        s9 = s9 or (pending[4] shl 32)
                        s9 = s9 or (pending[5] shl 39)
                        s9 = s9 or (pending[6] shl 46)
                        s9 = s9 or (pending[7] shl 53) // 4 more bits
                        consumed = 8
                    }
                    6 -> {
                        s9 = STATUS_10NUM_6BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 10)
                        s9 = s9 or (pending[2] shl 16)
                        s9 = s9 or (pending[3] shl 22)
                        s9 = s9 or (pending[4] shl 28)
                        s9 = s9 or (pending[5] shl 34)
                        s9 = s9 or (pending[6] shl 40)
                        s9 = s9 or (pending[7] shl 46)
                        s9 = s9 or (pending[8] shl 52)
                        s9 = s9 or (pending[9] shl 58)
                        consumed = 10
                    }
                    5 -> {
                        s9 = STATUS_12NUM_5BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 9)
                        s9 = s9 or (pending[2] shl 14)
                        s9 = s9 or (pending[3] shl 19)
                        s9 = s9 or (pending[4] shl 24)
                        s9 = s9 or (pending[5] shl 29)
                        s9 = s9 or (pending[6] shl 34)
                        s9 = s9 or (pending[7] shl 39)
                        s9 = s9 or (pending[8] shl 44)
                        s9 = s9 or (pending[9] shl 49)
                        s9 = s9 or (pending[10] shl 54)
                        s9 = s9 or (pending[11] shl 59)
                        consumed = 12
                    }
                    4 -> {
                        s9 = STATUS_15NUM_4BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 8)
                        s9 = s9 or (pending[2] shl 12)
                        s9 = s9 or (pending[3] shl 16)
                        s9 = s9 or (pending[4] shl 20)
                        s9 = s9 or (pending[5] shl 24)
                        s9 = s9 or (pending[6] shl 28)
                        s9 = s9 or (pending[7] shl 32)
                        s9 = s9 or (pending[8] shl 36)
                        s9 = s9 or (pending[9] shl 40)
                        s9 = s9 or (pending[10] shl 44)
                        s9 = s9 or (pending[11] shl 48)
                        s9 = s9 or (pending[12] shl 52)
                        s9 = s9 or (pending[13] shl 56)
                        s9 = s9 or (pending[14] shl 60)
                        consumed = 15
                    }
                    3 -> {
                        s9 = STATUS_20NUM_3BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 7)
                        s9 = s9 or (pending[2] shl 10)
                        s9 = s9 or (pending[3] shl 13)
                        s9 = s9 or (pending[4] shl 16)
                        s9 = s9 or (pending[5] shl 19)
                        s9 = s9 or (pending[6] shl 22)
                        s9 = s9 or (pending[7] shl 25)
                        s9 = s9 or (pending[8] shl 28)
                        s9 = s9 or (pending[9] shl 31)
                        s9 = s9 or (pending[10] shl 34)
                        s9 = s9 or (pending[11] shl 37)
                        s9 = s9 or (pending[12] shl 40)
                        s9 = s9 or (pending[13] shl 43)
                        s9 = s9 or (pending[14] shl 46)
                        s9 = s9 or (pending[15] shl 49)
                        s9 = s9 or (pending[16] shl 52)
                        s9 = s9 or (pending[17] shl 55)
                        s9 = s9 or (pending[18] shl 58)
                        s9 = s9 or (pending[19] shl 61)
                        consumed = 20
                    }
                    2 -> {
                        s9 = STATUS_30NUM_2BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 6)
                        s9 = s9 or (pending[2] shl 8)
                        s9 = s9 or (pending[3] shl 10)
                        s9 = s9 or (pending[4] shl 12)
                        s9 = s9 or (pending[5] shl 14)
                        s9 = s9 or (pending[6] shl 16)
                        s9 = s9 or (pending[7] shl 18)
                        s9 = s9 or (pending[8] shl 20)
                        s9 = s9 or (pending[9] shl 22)
                        s9 = s9 or (pending[10] shl 24)
                        s9 = s9 or (pending[11] shl 26)
                        s9 = s9 or (pending[12] shl 28)
                        s9 = s9 or (pending[13] shl 30)
                        s9 = s9 or (pending[14] shl 32)
                        s9 = s9 or (pending[15] shl 34)
                        s9 = s9 or (pending[16] shl 36)
                        s9 = s9 or (pending[17] shl 38)
                        s9 = s9 or (pending[18] shl 40)
                        s9 = s9 or (pending[19] shl 42)
                        s9 = s9 or (pending[20] shl 44)
                        s9 = s9 or (pending[21] shl 46)
                        s9 = s9 or (pending[22] shl 48)
                        s9 = s9 or (pending[23] shl 50)
                        s9 = s9 or (pending[24] shl 52)
                        s9 = s9 or (pending[25] shl 54)
                        s9 = s9 or (pending[26] shl 56)
                        s9 = s9 or (pending[27] shl 58)
                        s9 = s9 or (pending[28] shl 60)
                        s9 = s9 or (pending[29] shl 62)
                        consumed = 30
                    }
                    1 -> {
                        s9 = STATUS_60NUM_1BITS.toLong()
                        s9 = s9 or (pending[0] shl 4)
                        s9 = s9 or (pending[1] shl 5)
                        s9 = s9 or (pending[2] shl 6)
                        s9 = s9 or (pending[3] shl 7)
                        s9 = s9 or (pending[4] shl 8)
                        s9 = s9 or (pending[5] shl 9)
                        s9 = s9 or (pending[6] shl 10)
                        s9 = s9 or (pending[7] shl 11)
                        s9 = s9 or (pending[8] shl 12)
                        s9 = s9 or (pending[9] shl 13)
                        s9 = s9 or (pending[10] shl 14)
                        s9 = s9 or (pending[11] shl 15)
                        s9 = s9 or (pending[12] shl 16)
                        s9 = s9 or (pending[13] shl 17)
                        s9 = s9 or (pending[14] shl 18)
                        s9 = s9 or (pending[15] shl 19)
                        s9 = s9 or (pending[16] shl 20)
                        s9 = s9 or (pending[17] shl 21)
                        s9 = s9 or (pending[18] shl 22)
                        s9 = s9 or (pending[19] shl 23)
                        s9 = s9 or (pending[20] shl 24)
                        s9 = s9 or (pending[21] shl 25)
                        s9 = s9 or (pending[22] shl 26)
                        s9 = s9 or (pending[23] shl 27)
                        s9 = s9 or (pending[24] shl 28)
                        s9 = s9 or (pending[25] shl 29)
                        s9 = s9 or (pending[26] shl 30)
                        s9 = s9 or (pending[27] shl 31)
                        s9 = s9 or (pending[28] shl 32)
                        s9 = s9 or (pending[29] shl 33)
                        s9 = s9 or (pending[30] shl 34)
                        s9 = s9 or (pending[31] shl 35)
                        s9 = s9 or (pending[32] shl 36)
                        s9 = s9 or (pending[33] shl 37)
                        s9 = s9 or (pending[34] shl 38)
                        s9 = s9 or (pending[35] shl 39)
                        s9 = s9 or (pending[36] shl 40)
                        s9 = s9 or (pending[37] shl 41)
                        s9 = s9 or (pending[38] shl 42)
                        s9 = s9 or (pending[39] shl 43)
                        s9 = s9 or (pending[40] shl 44)
                        s9 = s9 or (pending[41] shl 45)
                        s9 = s9 or (pending[42] shl 46)
                        s9 = s9 or (pending[43] shl 47)
                        s9 = s9 or (pending[44] shl 48)
                        s9 = s9 or (pending[45] shl 49)
                        s9 = s9 or (pending[46] shl 50)
                        s9 = s9 or (pending[47] shl 51)
                        s9 = s9 or (pending[48] shl 52)
                        s9 = s9 or (pending[49] shl 53)
                        s9 = s9 or (pending[50] shl 54)
                        s9 = s9 or (pending[51] shl 55)
                        s9 = s9 or (pending[52] shl 56)
                        s9 = s9 or (pending[53] shl 57)
                        s9 = s9 or (pending[54] shl 58)
                        s9 = s9 or (pending[55] shl 59)
                        s9 = s9 or (pending[56] shl 60)
                        s9 = s9 or (pending[57] shl 61)
                        s9 = s9 or (pending[58] shl 62)
                        s9 = s9 or (pending[59] shl 63)
                        consumed = 60
                    }
                    else -> {
                        mpassert(false)
                        s9 = 0
                        consumed = 60
                    }
                }//throw new Error("S98b.compressSingle internal error: unknown minBits: " + minBits);

                val leftover = inputCount - consumed
                mpassert(leftover >= 0) { "consumed=$consumed vs $inputCompressable" }

                /*
        for(int x=0;x<consumed;x++) {
          System.out.println(""+pending[x]);
        }
        */
                /*
        // like cd burning!  ;)
        {
        int[] test = new int[consumed];
        int ct = decompressSingle(s9, test, 0);
        mpassert ct == consumed;
        for(int x=0;x<ct;x++) {
        mpassert test[x] == pending[x];
        }
        }
        */

                //System.out.println("  return consumed=" + consumed);
                reset()

                // save leftovers:
                Utils.arraycopy(pending, consumed, pending, 0, leftover)
                inputCount = leftover

                out[0] = s9
                return consumed
            }
        }

        return 0
    }

    companion object {

        private val NUM_DATA_BITS = 60
        private val BITS_30_MASK:Long = (1 shl 30) - 1
        private val BITS_20_MASK:Long = (1 shl 20) - 1
        private val BITS_15_MASK:Long = (1 shl 15) - 1
        private val BITS_12_MASK:Long = (1 shl 12) - 1
        private val BITS_11_MASK:Long = (1 shl 11) - 1
        private val BITS_10_MASK:Long = (1 shl 10) - 1
        private val BITS_8_MASK:Long = (1 shl 8) - 1 // 4 bits unused, then the last value take them
        private val BITS_7_MASK :Long= (1 shl 7) - 1 // 4 bits unused, then the last value take them
        private val BITS_6_MASK:Long = (1 shl 6) - 1
        private val BITS_5_MASK:Long = (1 shl 5) - 1
        private val BITS_4_MASK:Long = (1 shl 4) - 1
        private val BITS_3_MASK:Long = (1 shl 3) - 1
        private val BITS_2_MASK:Long = (1 shl 2) - 1
        private val BITS_1_MASK:Long = (1 shl 1) - 1

        private val STATUS_1NUM_60BITS = 14
        private val STATUS_2NUM_30BITS = 13
        private val STATUS_3NUM_20BITS = 12
        private val STATUS_4NUM_15BITS = 11
        private val STATUS_5NUM_12BITS = 10
        private val STATUS_6NUM_10BITS = 9
        private val STATUS_7NUM_8BITS = 8
        private val STATUS_8NUM_7BITS = 7
        private val STATUS_10NUM_6BITS = 6
        private val STATUS_12NUM_5BITS = 5
        private val STATUS_15NUM_4BITS = 4
        private val STATUS_20NUM_3BITS = 3
        private val STATUS_30NUM_2BITS = 2
        private val STATUS_60NUM_1BITS = 1

        private fun compressSingle(
            uncompressed: LongArray,
            inOffset: Int,
            inSize: Int,
            compressedBuffer: LongBuffer
        ): Int {
            if (inSize < 1) {
                throw IllegalArgumentException("Cannot compress input with non positive size $inSize")
            }
            var inputCompressable = 1
            var minBits = 1
            var maxFitPlus1 = (1 shl minBits).toLong()
            var nextData: Long

            do {
                nextData = uncompressed[inOffset + inputCompressable - 1]
                if (nextData < 0) {
                    throw IllegalArgumentException("Cannot compress negative input " + nextData + " (at index " + (inOffset + inputCompressable - 1) + ")")
                }
                while (nextData >= maxFitPlus1 && minBits < NUM_DATA_BITS) {
                    if (minBits == 7 && inputCompressable == 8 && nextData < maxFitPlus1 shl 4) {
                        break
                    } else if (minBits == 8 && inputCompressable == 7 && nextData < maxFitPlus1 shl 4) {
                        break
                    } else {
                        minBits++
                        maxFitPlus1 = maxFitPlus1 shl 1
                        if (inputCompressable * minBits > NUM_DATA_BITS) {
                            inputCompressable--
                            break
                        }
                    }
                }
                inputCompressable++
            } while (inputCompressable * minBits <= NUM_DATA_BITS && inputCompressable <= inSize)

            inputCompressable--
            if (inputCompressable == 0) {
                throw IllegalArgumentException("Cannot compress input $nextData with more than $NUM_DATA_BITS bits (at offSet $inOffset)")
            }

            // Check whether a bigger number of bits can be used:
            while (inputCompressable * (minBits + 1) <= NUM_DATA_BITS) {
                minBits++
            }

            if ((inputCompressable + 1) * minBits <= NUM_DATA_BITS) {
                // not enough input available for minBits
                minBits++
            }

            // Put compression method in status bits and encode input data
            var s9: Long
            when (minBits) {
                // add status bits and later input values
                60 -> {
                    s9 = STATUS_1NUM_60BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    compressedBuffer.put(s9)
                    return 1
                }
                30 -> {
                    s9 = STATUS_2NUM_30BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 34)
                    compressedBuffer.put(s9)
                    return 2
                }
                20 -> {
                    s9 = STATUS_3NUM_20BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 24)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 44)
                    compressedBuffer.put(s9)
                    return 3
                }
                15 -> {
                    s9 = STATUS_4NUM_15BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 19)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 34)
                    s9 = s9 or (uncompressed[inOffset + 3] shl 49)
                    compressedBuffer.put(s9)
                    return 4
                }
                12 -> {
                    s9 = STATUS_5NUM_12BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 16)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 28)
                    s9 = s9 or (uncompressed[inOffset + 3] shl 40)
                    s9 = s9 or (uncompressed[inOffset + 4] shl 52)
                    compressedBuffer.put(s9)
                    return 5
                }
                10 -> {
                    s9 = STATUS_6NUM_10BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 14)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 24)
                    s9 = s9 or (uncompressed[inOffset + 3] shl 34)
                    s9 = s9 or (uncompressed[inOffset + 4] shl 44)
                    s9 = s9 or (uncompressed[inOffset + 5] shl 54)
                    compressedBuffer.put(s9)
                    return 6
                }
                8 -> {
                    s9 = STATUS_7NUM_8BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 12)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 20)
                    s9 = s9 or (uncompressed[inOffset + 3] shl 28)
                    s9 = s9 or (uncompressed[inOffset + 4] shl 36)
                    s9 = s9 or (uncompressed[inOffset + 5] shl 44)
                    s9 = s9 or (uncompressed[inOffset + 6] shl 52) // 4 more bits
                    compressedBuffer.put(s9)
                    return 7
                }
                7 -> {
                    s9 = STATUS_8NUM_7BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 11)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 18)
                    s9 = s9 or (uncompressed[inOffset + 3] shl 25)
                    s9 = s9 or (uncompressed[inOffset + 4] shl 32)
                    s9 = s9 or (uncompressed[inOffset + 5] shl 39)
                    s9 = s9 or (uncompressed[inOffset + 6] shl 46)
                    s9 = s9 or (uncompressed[inOffset + 7] shl 53) // 4 more bits
                    compressedBuffer.put(s9)
                    return 8
                }
                6 -> {
                    s9 = STATUS_10NUM_6BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 10)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 16)
                    s9 = s9 or (uncompressed[inOffset + 3] shl 22)
                    s9 = s9 or (uncompressed[inOffset + 4] shl 28)
                    s9 = s9 or (uncompressed[inOffset + 5] shl 34)
                    s9 = s9 or (uncompressed[inOffset + 6] shl 40)
                    s9 = s9 or (uncompressed[inOffset + 7] shl 46)
                    s9 = s9 or (uncompressed[inOffset + 8] shl 52)
                    s9 = s9 or (uncompressed[inOffset + 9] shl 58)
                    compressedBuffer.put(s9)
                    return 10
                }
                5 -> {
                    s9 = STATUS_12NUM_5BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 9)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 14)
                    s9 = s9 or (uncompressed[inOffset + 3] shl 19)
                    s9 = s9 or (uncompressed[inOffset + 4] shl 24)
                    s9 = s9 or (uncompressed[inOffset + 5] shl 29)
                    s9 = s9 or (uncompressed[inOffset + 6] shl 34)
                    s9 = s9 or (uncompressed[inOffset + 7] shl 39)
                    s9 = s9 or (uncompressed[inOffset + 8] shl 44)
                    s9 = s9 or (uncompressed[inOffset + 9] shl 49)
                    s9 = s9 or (uncompressed[inOffset + 10] shl 54)
                    s9 = s9 or (uncompressed[inOffset + 11] shl 59)
                    compressedBuffer.put(s9)
                    return 12
                }
                4 -> {
                    s9 = STATUS_15NUM_4BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 8)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 12)
                    s9 = s9 or (uncompressed[inOffset + 3] shl 16)
                    s9 = s9 or (uncompressed[inOffset + 4] shl 20)
                    s9 = s9 or (uncompressed[inOffset + 5] shl 24)
                    s9 = s9 or (uncompressed[inOffset + 6] shl 28)
                    s9 = s9 or (uncompressed[inOffset + 7] shl 32)
                    s9 = s9 or (uncompressed[inOffset + 8] shl 36)
                    s9 = s9 or (uncompressed[inOffset + 9] shl 40)
                    s9 = s9 or (uncompressed[inOffset + 10] shl 44)
                    s9 = s9 or (uncompressed[inOffset + 11] shl 48)
                    s9 = s9 or (uncompressed[inOffset + 12] shl 52)
                    s9 = s9 or (uncompressed[inOffset + 13] shl 56)
                    s9 = s9 or (uncompressed[inOffset + 14] shl 60)
                    compressedBuffer.put(s9)
                    return 15
                }
                3 -> {
                    s9 = STATUS_20NUM_3BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 7)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 10)
                    s9 = s9 or (uncompressed[inOffset + 3] shl 13)
                    s9 = s9 or (uncompressed[inOffset + 4] shl 16)
                    s9 = s9 or (uncompressed[inOffset + 5] shl 19)
                    s9 = s9 or (uncompressed[inOffset + 6] shl 22)
                    s9 = s9 or (uncompressed[inOffset + 7] shl 25)
                    s9 = s9 or (uncompressed[inOffset + 8] shl 28)
                    s9 = s9 or (uncompressed[inOffset + 9] shl 31)
                    s9 = s9 or (uncompressed[inOffset + 10] shl 34)
                    s9 = s9 or (uncompressed[inOffset + 11] shl 37)
                    s9 = s9 or (uncompressed[inOffset + 12] shl 40)
                    s9 = s9 or (uncompressed[inOffset + 13] shl 43)
                    s9 = s9 or (uncompressed[inOffset + 14] shl 46)
                    s9 = s9 or (uncompressed[inOffset + 15] shl 49)
                    s9 = s9 or (uncompressed[inOffset + 16] shl 52)
                    s9 = s9 or (uncompressed[inOffset + 17] shl 55)
                    s9 = s9 or (uncompressed[inOffset + 18] shl 58)
                    s9 = s9 or (uncompressed[inOffset + 19] shl 61)
                    compressedBuffer.put(s9)
                    return 20
                }
                2 -> {
                    s9 = STATUS_30NUM_2BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 6)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 8)
                    s9 = s9 or (uncompressed[inOffset + 3] shl 10)
                    s9 = s9 or (uncompressed[inOffset + 4] shl 12)
                    s9 = s9 or (uncompressed[inOffset + 5] shl 14)
                    s9 = s9 or (uncompressed[inOffset + 6] shl 16)
                    s9 = s9 or (uncompressed[inOffset + 7] shl 18)
                    s9 = s9 or (uncompressed[inOffset + 8] shl 20)
                    s9 = s9 or (uncompressed[inOffset + 9] shl 22)
                    s9 = s9 or (uncompressed[inOffset + 10] shl 24)
                    s9 = s9 or (uncompressed[inOffset + 11] shl 26)
                    s9 = s9 or (uncompressed[inOffset + 12] shl 28)
                    s9 = s9 or (uncompressed[inOffset + 13] shl 30)
                    s9 = s9 or (uncompressed[inOffset + 14] shl 32)
                    s9 = s9 or (uncompressed[inOffset + 15] shl 34)
                    s9 = s9 or (uncompressed[inOffset + 16] shl 36)
                    s9 = s9 or (uncompressed[inOffset + 17] shl 38)
                    s9 = s9 or (uncompressed[inOffset + 18] shl 40)
                    s9 = s9 or (uncompressed[inOffset + 19] shl 42)
                    s9 = s9 or (uncompressed[inOffset + 20] shl 44)
                    s9 = s9 or (uncompressed[inOffset + 21] shl 46)
                    s9 = s9 or (uncompressed[inOffset + 22] shl 48)
                    s9 = s9 or (uncompressed[inOffset + 23] shl 50)
                    s9 = s9 or (uncompressed[inOffset + 24] shl 52)
                    s9 = s9 or (uncompressed[inOffset + 25] shl 54)
                    s9 = s9 or (uncompressed[inOffset + 26] shl 56)
                    s9 = s9 or (uncompressed[inOffset + 27] shl 58)
                    s9 = s9 or (uncompressed[inOffset + 28] shl 60)
                    s9 = s9 or (uncompressed[inOffset + 29] shl 62)
                    compressedBuffer.put(s9)
                    return 30
                }
                1 -> {
                    s9 = STATUS_60NUM_1BITS.toLong()
                    s9 = s9 or (uncompressed[inOffset] shl 4)
                    s9 = s9 or (uncompressed[inOffset + 1] shl 5)
                    s9 = s9 or (uncompressed[inOffset + 2] shl 6)
                    s9 = s9 or (uncompressed[inOffset + 3] shl 7)
                    s9 = s9 or (uncompressed[inOffset + 4] shl 8)
                    s9 = s9 or (uncompressed[inOffset + 5] shl 9)
                    s9 = s9 or (uncompressed[inOffset + 6] shl 10)
                    s9 = s9 or (uncompressed[inOffset + 7] shl 11)
                    s9 = s9 or (uncompressed[inOffset + 8] shl 12)
                    s9 = s9 or (uncompressed[inOffset + 9] shl 13)
                    s9 = s9 or (uncompressed[inOffset + 10] shl 14)
                    s9 = s9 or (uncompressed[inOffset + 11] shl 15)
                    s9 = s9 or (uncompressed[inOffset + 12] shl 16)
                    s9 = s9 or (uncompressed[inOffset + 13] shl 17)
                    s9 = s9 or (uncompressed[inOffset + 14] shl 18)
                    s9 = s9 or (uncompressed[inOffset + 15] shl 19)
                    s9 = s9 or (uncompressed[inOffset + 16] shl 20)
                    s9 = s9 or (uncompressed[inOffset + 17] shl 21)
                    s9 = s9 or (uncompressed[inOffset + 18] shl 22)
                    s9 = s9 or (uncompressed[inOffset + 19] shl 23)
                    s9 = s9 or (uncompressed[inOffset + 20] shl 24)
                    s9 = s9 or (uncompressed[inOffset + 21] shl 25)
                    s9 = s9 or (uncompressed[inOffset + 22] shl 26)
                    s9 = s9 or (uncompressed[inOffset + 23] shl 27)
                    s9 = s9 or (uncompressed[inOffset + 24] shl 28)
                    s9 = s9 or (uncompressed[inOffset + 25] shl 29)
                    s9 = s9 or (uncompressed[inOffset + 26] shl 30)
                    s9 = s9 or (uncompressed[inOffset + 27] shl 31)
                    s9 = s9 or (uncompressed[inOffset + 28] shl 32)
                    s9 = s9 or (uncompressed[inOffset + 29] shl 33)
                    s9 = s9 or (uncompressed[inOffset + 30] shl 34)
                    s9 = s9 or (uncompressed[inOffset + 31] shl 35)
                    s9 = s9 or (uncompressed[inOffset + 32] shl 36)
                    s9 = s9 or (uncompressed[inOffset + 33] shl 37)
                    s9 = s9 or (uncompressed[inOffset + 34] shl 38)
                    s9 = s9 or (uncompressed[inOffset + 35] shl 39)
                    s9 = s9 or (uncompressed[inOffset + 36] shl 40)
                    s9 = s9 or (uncompressed[inOffset + 37] shl 41)
                    s9 = s9 or (uncompressed[inOffset + 38] shl 42)
                    s9 = s9 or (uncompressed[inOffset + 39] shl 43)
                    s9 = s9 or (uncompressed[inOffset + 40] shl 44)
                    s9 = s9 or (uncompressed[inOffset + 41] shl 45)
                    s9 = s9 or (uncompressed[inOffset + 42] shl 46)
                    s9 = s9 or (uncompressed[inOffset + 43] shl 47)
                    s9 = s9 or (uncompressed[inOffset + 44] shl 48)
                    s9 = s9 or (uncompressed[inOffset + 45] shl 49)
                    s9 = s9 or (uncompressed[inOffset + 46] shl 50)
                    s9 = s9 or (uncompressed[inOffset + 47] shl 51)
                    s9 = s9 or (uncompressed[inOffset + 48] shl 52)
                    s9 = s9 or (uncompressed[inOffset + 49] shl 53)
                    s9 = s9 or (uncompressed[inOffset + 50] shl 54)
                    s9 = s9 or (uncompressed[inOffset + 51] shl 55)
                    s9 = s9 or (uncompressed[inOffset + 52] shl 56)
                    s9 = s9 or (uncompressed[inOffset + 53] shl 57)
                    s9 = s9 or (uncompressed[inOffset + 54] shl 58)
                    s9 = s9 or (uncompressed[inOffset + 55] shl 59)
                    s9 = s9 or (uncompressed[inOffset + 56] shl 60)
                    s9 = s9 or (uncompressed[inOffset + 57] shl 61)
                    s9 = s9 or (uncompressed[inOffset + 58] shl 62)
                    s9 = s9 or (uncompressed[inOffset + 59] shl 63)
                    compressedBuffer.put(s9)
                    return 60
                }
                else -> throw Error("S98b.compressSingle internal error: unknown minBits: $minBits")
            }
        }

        private fun decompressSingle(s9: Long, decompressed: LongArray, outOffset: Int): Int {
            when ((s9 and 15L).toInt()) {
                STATUS_1NUM_60BITS -> {
                    decompressed[outOffset] = s9.ushr(4)
                    return 1
                }
                STATUS_2NUM_30BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_30_MASK
                    decompressed[outOffset + 1] = s9.ushr(34) and BITS_30_MASK
                    return 2
                }
                STATUS_3NUM_20BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_20_MASK
                    decompressed[outOffset + 1] = s9.ushr(24) and BITS_20_MASK
                    decompressed[outOffset + 2] = s9.ushr(44) and BITS_20_MASK
                    return 3
                }
                STATUS_4NUM_15BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_15_MASK
                    decompressed[outOffset + 1] = s9.ushr(19) and BITS_15_MASK
                    decompressed[outOffset + 2] = s9.ushr(34) and BITS_15_MASK
                    decompressed[outOffset + 3] = s9.ushr(49) and BITS_15_MASK
                    return 4
                }
                STATUS_5NUM_12BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_12_MASK
                    decompressed[outOffset + 1] = s9.ushr(16) and BITS_12_MASK
                    decompressed[outOffset + 2] = s9.ushr(28) and BITS_12_MASK
                    decompressed[outOffset + 3] = s9.ushr(40) and BITS_12_MASK
                    decompressed[outOffset + 4] = s9.ushr(52) and BITS_12_MASK
                    return 5
                }
                STATUS_6NUM_10BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_10_MASK
                    decompressed[outOffset + 1] = s9.ushr(14) and BITS_10_MASK
                    decompressed[outOffset + 2] = s9.ushr(24) and BITS_10_MASK
                    decompressed[outOffset + 3] = s9.ushr(34) and BITS_10_MASK
                    decompressed[outOffset + 4] = s9.ushr(44) and BITS_10_MASK
                    decompressed[outOffset + 5] = s9.ushr(54) and BITS_10_MASK
                    return 6
                }
                STATUS_7NUM_8BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_8_MASK
                    decompressed[outOffset + 1] = s9.ushr(12) and BITS_8_MASK
                    decompressed[outOffset + 2] = s9.ushr(20) and BITS_8_MASK
                    decompressed[outOffset + 3] = s9.ushr(28) and BITS_8_MASK
                    decompressed[outOffset + 4] = s9.ushr(36) and BITS_8_MASK
                    decompressed[outOffset + 5] = s9.ushr(44) and BITS_8_MASK
                    decompressed[outOffset + 6] = s9.ushr(52) and BITS_12_MASK
                    return 7
                }
                STATUS_8NUM_7BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_7_MASK
                    decompressed[outOffset + 1] = s9.ushr(11) and BITS_7_MASK
                    decompressed[outOffset + 2] = s9.ushr(18) and BITS_7_MASK
                    decompressed[outOffset + 3] = s9.ushr(25) and BITS_7_MASK
                    decompressed[outOffset + 4] = s9.ushr(32) and BITS_7_MASK
                    decompressed[outOffset + 5] = s9.ushr(39) and BITS_7_MASK
                    decompressed[outOffset + 6] = s9.ushr(46) and BITS_7_MASK
                    decompressed[outOffset + 7] = s9.ushr(53) and BITS_11_MASK
                    return 8
                }
                STATUS_10NUM_6BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_6_MASK
                    decompressed[outOffset + 1] = s9.ushr(10) and BITS_6_MASK
                    decompressed[outOffset + 2] = s9.ushr(16) and BITS_6_MASK
                    decompressed[outOffset + 3] = s9.ushr(22) and BITS_6_MASK
                    decompressed[outOffset + 4] = s9.ushr(28) and BITS_6_MASK
                    decompressed[outOffset + 5] = s9.ushr(34) and BITS_6_MASK
                    decompressed[outOffset + 6] = s9.ushr(40) and BITS_6_MASK
                    decompressed[outOffset + 7] = s9.ushr(46) and BITS_6_MASK
                    decompressed[outOffset + 8] = s9.ushr(52) and BITS_6_MASK
                    decompressed[outOffset + 9] = s9.ushr(58) and BITS_6_MASK
                    return 10
                }
                STATUS_12NUM_5BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_5_MASK
                    decompressed[outOffset + 1] = s9.ushr(9) and BITS_5_MASK
                    decompressed[outOffset + 2] = s9.ushr(14) and BITS_5_MASK
                    decompressed[outOffset + 3] = s9.ushr(19) and BITS_5_MASK
                    decompressed[outOffset + 4] = s9.ushr(24) and BITS_5_MASK
                    decompressed[outOffset + 5] = s9.ushr(29) and BITS_5_MASK
                    decompressed[outOffset + 6] = s9.ushr(34) and BITS_5_MASK
                    decompressed[outOffset + 7] = s9.ushr(39) and BITS_5_MASK
                    decompressed[outOffset + 8] = s9.ushr(44) and BITS_5_MASK
                    decompressed[outOffset + 9] = s9.ushr(49) and BITS_5_MASK
                    decompressed[outOffset + 10] = s9.ushr(54) and BITS_5_MASK
                    decompressed[outOffset + 11] = s9.ushr(59) and BITS_5_MASK
                    return 12
                }
                STATUS_15NUM_4BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_4_MASK
                    decompressed[outOffset + 1] = s9.ushr(8) and BITS_4_MASK
                    decompressed[outOffset + 2] = s9.ushr(12) and BITS_4_MASK
                    decompressed[outOffset + 3] = s9.ushr(16) and BITS_4_MASK
                    decompressed[outOffset + 4] = s9.ushr(20) and BITS_4_MASK
                    decompressed[outOffset + 5] = s9.ushr(24) and BITS_4_MASK
                    decompressed[outOffset + 6] = s9.ushr(28) and BITS_4_MASK
                    decompressed[outOffset + 6] = s9.ushr(32) and BITS_4_MASK
                    decompressed[outOffset + 8] = s9.ushr(36) and BITS_4_MASK
                    decompressed[outOffset + 9] = s9.ushr(40) and BITS_4_MASK
                    decompressed[outOffset + 10] = s9.ushr(44) and BITS_4_MASK
                    decompressed[outOffset + 11] = s9.ushr(48) and BITS_4_MASK
                    decompressed[outOffset + 12] = s9.ushr(52) and BITS_4_MASK
                    decompressed[outOffset + 13] = s9.ushr(56) and BITS_4_MASK
                    decompressed[outOffset + 14] = s9.ushr(60) and BITS_4_MASK
                    return 15
                }
                STATUS_20NUM_3BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_3_MASK
                    decompressed[outOffset + 1] = s9.ushr(7) and BITS_3_MASK
                    decompressed[outOffset + 2] = s9.ushr(10) and BITS_3_MASK
                    decompressed[outOffset + 3] = s9.ushr(13) and BITS_3_MASK
                    decompressed[outOffset + 4] = s9.ushr(16) and BITS_3_MASK
                    decompressed[outOffset + 5] = s9.ushr(19) and BITS_3_MASK
                    decompressed[outOffset + 6] = s9.ushr(22) and BITS_3_MASK
                    decompressed[outOffset + 7] = s9.ushr(25) and BITS_3_MASK
                    decompressed[outOffset + 8] = s9.ushr(28) and BITS_3_MASK
                    decompressed[outOffset + 9] = s9.ushr(31) and BITS_3_MASK
                    decompressed[outOffset + 10] = s9.ushr(34) and BITS_3_MASK
                    decompressed[outOffset + 11] = s9.ushr(37) and BITS_3_MASK
                    decompressed[outOffset + 12] = s9.ushr(40) and BITS_3_MASK
                    decompressed[outOffset + 13] = s9.ushr(43) and BITS_3_MASK
                    decompressed[outOffset + 14] = s9.ushr(46) and BITS_3_MASK
                    decompressed[outOffset + 15] = s9.ushr(49) and BITS_3_MASK
                    decompressed[outOffset + 16] = s9.ushr(52) and BITS_3_MASK
                    decompressed[outOffset + 17] = s9.ushr(55) and BITS_3_MASK
                    decompressed[outOffset + 18] = s9.ushr(58) and BITS_3_MASK
                    decompressed[outOffset + 19] = s9.ushr(61) and BITS_3_MASK
                    return 20
                }
                STATUS_30NUM_2BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_2_MASK
                    decompressed[outOffset + 1] = s9.ushr(6) and BITS_2_MASK
                    decompressed[outOffset + 2] = s9.ushr(8) and BITS_2_MASK
                    decompressed[outOffset + 3] = s9.ushr(10) and BITS_2_MASK
                    decompressed[outOffset + 4] = s9.ushr(12) and BITS_2_MASK
                    decompressed[outOffset + 5] = s9.ushr(14) and BITS_2_MASK
                    decompressed[outOffset + 6] = s9.ushr(16) and BITS_2_MASK
                    decompressed[outOffset + 7] = s9.ushr(18) and BITS_2_MASK
                    decompressed[outOffset + 8] = s9.ushr(20) and BITS_2_MASK
                    decompressed[outOffset + 9] = s9.ushr(22) and BITS_2_MASK
                    decompressed[outOffset + 10] = s9.ushr(24) and BITS_2_MASK
                    decompressed[outOffset + 11] = s9.ushr(26) and BITS_2_MASK
                    decompressed[outOffset + 12] = s9.ushr(28) and BITS_2_MASK
                    decompressed[outOffset + 13] = s9.ushr(30) and BITS_2_MASK
                    decompressed[outOffset + 14] = s9.ushr(32) and BITS_2_MASK
                    decompressed[outOffset + 15] = s9.ushr(34) and BITS_2_MASK
                    decompressed[outOffset + 16] = s9.ushr(36) and BITS_2_MASK
                    decompressed[outOffset + 17] = s9.ushr(38) and BITS_2_MASK
                    decompressed[outOffset + 18] = s9.ushr(40) and BITS_2_MASK
                    decompressed[outOffset + 19] = s9.ushr(42) and BITS_2_MASK
                    decompressed[outOffset + 20] = s9.ushr(44) and BITS_2_MASK
                    decompressed[outOffset + 21] = s9.ushr(46) and BITS_2_MASK
                    decompressed[outOffset + 22] = s9.ushr(48) and BITS_2_MASK
                    decompressed[outOffset + 23] = s9.ushr(50) and BITS_2_MASK
                    decompressed[outOffset + 24] = s9.ushr(52) and BITS_2_MASK
                    decompressed[outOffset + 25] = s9.ushr(54) and BITS_2_MASK
                    decompressed[outOffset + 26] = s9.ushr(56) and BITS_2_MASK
                    decompressed[outOffset + 27] = s9.ushr(58) and BITS_2_MASK
                    decompressed[outOffset + 28] = s9.ushr(60) and BITS_2_MASK
                    decompressed[outOffset + 29] = s9.ushr(62) and BITS_2_MASK
                    return 30
                }
                STATUS_60NUM_1BITS -> {
                    decompressed[outOffset] = s9.ushr(4) and BITS_1_MASK
                    decompressed[outOffset + 1] = s9.ushr(5) and BITS_1_MASK
                    decompressed[outOffset + 2] = s9.ushr(6) and BITS_1_MASK
                    decompressed[outOffset + 3] = s9.ushr(7) and BITS_1_MASK
                    decompressed[outOffset + 4] = s9.ushr(8) and BITS_1_MASK
                    decompressed[outOffset + 5] = s9.ushr(9) and BITS_1_MASK
                    decompressed[outOffset + 6] = s9.ushr(10) and BITS_1_MASK
                    decompressed[outOffset + 7] = s9.ushr(11) and BITS_1_MASK
                    decompressed[outOffset + 8] = s9.ushr(12) and BITS_1_MASK
                    decompressed[outOffset + 9] = s9.ushr(13) and BITS_1_MASK
                    decompressed[outOffset + 10] = s9.ushr(14) and BITS_1_MASK
                    decompressed[outOffset + 11] = s9.ushr(15) and BITS_1_MASK
                    decompressed[outOffset + 12] = s9.ushr(16) and BITS_1_MASK
                    decompressed[outOffset + 13] = s9.ushr(17) and BITS_1_MASK
                    decompressed[outOffset + 14] = s9.ushr(18) and BITS_1_MASK
                    decompressed[outOffset + 15] = s9.ushr(19) and BITS_1_MASK
                    decompressed[outOffset + 16] = s9.ushr(20) and BITS_1_MASK
                    decompressed[outOffset + 17] = s9.ushr(21) and BITS_1_MASK
                    decompressed[outOffset + 18] = s9.ushr(22) and BITS_1_MASK
                    decompressed[outOffset + 19] = s9.ushr(23) and BITS_1_MASK
                    decompressed[outOffset + 20] = s9.ushr(24) and BITS_1_MASK
                    decompressed[outOffset + 21] = s9.ushr(25) and BITS_1_MASK
                    decompressed[outOffset + 22] = s9.ushr(26) and BITS_1_MASK
                    decompressed[outOffset + 23] = s9.ushr(27) and BITS_1_MASK
                    decompressed[outOffset + 24] = s9.ushr(28) and BITS_1_MASK
                    decompressed[outOffset + 25] = s9.ushr(29) and BITS_1_MASK
                    decompressed[outOffset + 26] = s9.ushr(30) and BITS_1_MASK
                    decompressed[outOffset + 27] = s9.ushr(31) and BITS_1_MASK
                    decompressed[outOffset + 28] = s9.ushr(32) and BITS_1_MASK
                    decompressed[outOffset + 29] = s9.ushr(33) and BITS_1_MASK
                    decompressed[outOffset + 30] = s9.ushr(34) and BITS_1_MASK
                    decompressed[outOffset + 31] = s9.ushr(35) and BITS_1_MASK
                    decompressed[outOffset + 32] = s9.ushr(36) and BITS_1_MASK
                    decompressed[outOffset + 33] = s9.ushr(37) and BITS_1_MASK
                    decompressed[outOffset + 34] = s9.ushr(38) and BITS_1_MASK
                    decompressed[outOffset + 35] = s9.ushr(39) and BITS_1_MASK
                    decompressed[outOffset + 36] = s9.ushr(40) and BITS_1_MASK
                    decompressed[outOffset + 37] = s9.ushr(41) and BITS_1_MASK
                    decompressed[outOffset + 38] = s9.ushr(42) and BITS_1_MASK
                    decompressed[outOffset + 39] = s9.ushr(43) and BITS_1_MASK
                    decompressed[outOffset + 40] = s9.ushr(44) and BITS_1_MASK
                    decompressed[outOffset + 41] = s9.ushr(45) and BITS_1_MASK
                    decompressed[outOffset + 42] = s9.ushr(46) and BITS_1_MASK
                    decompressed[outOffset + 43] = s9.ushr(47) and BITS_1_MASK
                    decompressed[outOffset + 44] = s9.ushr(48) and BITS_1_MASK
                    decompressed[outOffset + 45] = s9.ushr(49) and BITS_1_MASK
                    decompressed[outOffset + 46] = s9.ushr(50) and BITS_1_MASK
                    decompressed[outOffset + 47] = s9.ushr(51) and BITS_1_MASK
                    decompressed[outOffset + 48] = s9.ushr(52) and BITS_1_MASK
                    decompressed[outOffset + 49] = s9.ushr(53) and BITS_1_MASK
                    decompressed[outOffset + 50] = s9.ushr(54) and BITS_1_MASK
                    decompressed[outOffset + 51] = s9.ushr(55) and BITS_1_MASK
                    decompressed[outOffset + 52] = s9.ushr(56) and BITS_1_MASK
                    decompressed[outOffset + 53] = s9.ushr(57) and BITS_1_MASK
                    decompressed[outOffset + 54] = s9.ushr(58) and BITS_1_MASK
                    decompressed[outOffset + 55] = s9.ushr(59) and BITS_1_MASK
                    decompressed[outOffset + 56] = s9.ushr(60) and BITS_1_MASK
                    decompressed[outOffset + 57] = s9.ushr(61) and BITS_1_MASK
                    decompressed[outOffset + 58] = s9.ushr(62) and BITS_1_MASK
                    decompressed[outOffset + 59] = s9.ushr(63) and BITS_1_MASK
                    return 60
                }
                else -> throw IllegalArgumentException("Unknown Simple9 status: " + s9.ushr(NUM_DATA_BITS))
            }
        }

        fun compress(compressedBuffer: LongBuffer, unCompressedData: LongArray, offset: Int, size: Int) {
            var offset = offset
            var size = size
            var encoded: Int
            while (size > 0) {
                encoded = compressSingle(unCompressedData, offset, size, compressedBuffer)
                offset += encoded
                size -= encoded
            }
        }

        fun decompress(compressedBuffer: LongBuffer, unCompressedData: LongArray): Int {
            var totalOut = 0

            compressedBuffer.rewind()
            var unComprSize = unCompressedData.size
            while (unComprSize > 0) {
                val decoded = decompressSingle(compressedBuffer.get(), unCompressedData, totalOut)
                unComprSize -= decoded
                totalOut += decoded
            }
            return totalOut
        }
    }

}
