package cpu.alu;

import util.DataType;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {

    /**
     * 返回两个二进制整数的和
     * dest + src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType add(DataType src, DataType dest) {
        return new DataType(AddSub(src.toString(), dest.toString(), 0));
    }

    /**
     * 返回两个二进制整数的差
     * dest - src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType sub(DataType src, DataType dest) {
        return new DataType(AddSub(src.toString(), dest.toString(), 1));
    }

    private String AddSub(String src, String dest, int C) {
        StringBuilder stringBuilder = new StringBuilder();
        int[] x = new int[32];
        int[] y = new int[32];
        int[] c = new int[32];
        int[] s = new int[32];
        for(int i = 31; i >= 0; --i) {
            x[i] = src.charAt(i) - '0';
            y[i] = dest.charAt(i) - '0';
        }
        if(C == 1)
            for(int i = 0; i < 32; ++i) {
                x[i] = 1 - x[i];
            }
        int val;
        for(int i = 0; i < 32; ++i) {
            s[i] = x[i] ^ y[i];
            val = x[i] & y[i];
            y[i] = x[i] | y[i];
            x[i] = val;
        }
        c[31] = x[31] | (y[31] & C);
        for(int i = 30; i >= 0; --i) {
            c[i] = x[i] | (y[i] & c[i+1]);
        }
        s[31] ^= C;
        for(int i = 0; i < 31; ++i) {
            s[i] ^= c[i + 1];
            stringBuilder.append(s[i]);
        }
        stringBuilder.append(s[31]);
        return stringBuilder.toString();
    }

}
