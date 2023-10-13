package cpu.alu;


import util.DataType;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {

    /**
     * 返回两个二进制整数的乘积(结果低位截取后32位)
     * dest * src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType mul(DataType src, DataType dest) {
        int[] destNum = new int[32];
        int[] srcNum = new int[65];
        int[] antiDest = new int[32];
        String srcStr = src.toString();
        String destStr = dest.toString();
        for(int i = 0; i < 32; i++) {
            destNum[i] = destStr.charAt(i) - '0';
            srcNum[i] = 0;
            srcNum[i+32] = srcStr.charAt(i) - '0';
            antiDest[i] = 1 - destNum[i];
        }
        srcNum[64] = 0;
        int C;
        for(int i = 1; i <= 32; i++) {
            switch (srcNum[64] - srcNum[63]) {
                case 0:
                    for(int j = 64; j > 0; j--)
                        srcNum[j] = srcNum[j-1];
                    break;
                case 1:
                    C = 0;
                    for(int j = 31; j >= 0; j--) {
                        int k = srcNum[j];
                        srcNum[j] = destNum[j] ^ srcNum[j] ^ C;
                        C = (destNum[j] & C) | (destNum[j] & k) | (k & C);
                    }
                    for(int j = 64; j > 0; j--)
                        srcNum[j] = srcNum[j-1];
                    break;
                case -1:
                    C = 1;
                    for(int j = 31; j >=0; j--) {
                        int k = srcNum[j];
                        srcNum[j] = antiDest[j] ^ srcNum[j] ^ C;
                        C = (antiDest[j] & C) | (antiDest[j] & k) | (k & C);
                    }
                    for(int j = 64; j > 0; j--)
                        srcNum[j] = srcNum[j-1];
                    break;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < 32; i++) {
            stringBuilder.append(srcNum[i+32]);
        }
        return new DataType(stringBuilder.toString());
    }

}
