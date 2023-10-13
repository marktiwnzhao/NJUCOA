package cpu.alu;

import util.DataType;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {

    DataType remainderReg;

    /**
     * 返回两个二进制整数的除法结果
     * dest ÷ src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType div(DataType src, DataType dest) {
        String srcStr = src.toString();
        String destStr = dest.toString();
        boolean isNeg = (srcStr.charAt(0) != destStr.charAt(0));
        if(srcStr.equals("00000000000000000000000000000000")) {
            throw new ArithmeticException();
        }
        String signStr = "00000000000000000000000000000000";
        if(destStr.charAt(0) == '1') signStr = "11111111111111111111111111111111";
        destStr = signStr + destStr;
        for(int i = 0; i < 32; i++) {
            destStr = destStr.substring(1);
            if(destStr.charAt(0) != srcStr.charAt(0)) destStr = Add(destStr, srcStr, 0, i+1);
            else destStr = Sub(destStr, srcStr, i+1);
        }
        String quo = destStr.substring(32);
        String rem = destStr.substring(0, 32);
        if(isNeg) {
            int[] num = new int[32];
            for (int i = 0; i < 32; i++) {
                num[i] = quo.charAt(i) == '0' ? 1 : 0;
            }
            for (int i = 31; i >= 0; --i) {
                if (num[i] == 0) {
                    num[i] = 1;
                    break;
                }
                num[i] = 0;
            }
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < 32; i++) sb.append(num[i]);
            quo = sb.toString();


//            boolean isZero = true;
//            for(int i = 0; i < 32; ++i) {
//                if(destStr.charAt(i) != '0') {
//                    isZero = false;
//                    break;
//                }
//            }
//            if(isZero && quo.charAt(0) == '0') {
//                int[] num = new int[32];
//                for (int i = 0; i < 32; i++) {
//                    num[i] = quo.charAt(i) == '0' ? 1 : 0;
//                }
//                for (int i = 31; i >= 0; --i) {
//                    if (num[i] == 0) {
//                        num[i] = 1;
//                        break;
//                    }
//                    num[i] = 0;
//                }
//                StringBuilder sb = new StringBuilder();
//                for(int i = 0; i < 32; i++) sb.append(num[i]);
//                quo = sb.toString();
//            }
//
//            if(!isZero) {
//                int[] num = new int[32];
//                for (int i = 0; i < 32; i++) {
//                    num[i] = quo.charAt(i) == '0' ? 1 : 0;
//                }
//                for (int i = 31; i >= 0; --i) {
//                    if (num[i] == 0) {
//                        num[i] = 1;
//                        break;
//                    }
//                    num[i] = 0;
//                }
//                if(destStr.charAt(0) != s) {
//                    for(int i = 31; i >= 0; --i) {
//                        if(num[i] == 0) {
//                            num[i] = 1;
//                            break;
//                        }
//                        num[i] = 0;
//                    }
//                    //除数和被除数相减
//                    StringBuilder stringBuilder = new StringBuilder();
//                    char[] res = new char[33];
//                    for(int i = 0; i < 33; ++i) {
//                        res[i] = 0;
//                    }
//                    res[32] = 1;
//                    for(int i = 31; i >= 0; --i) {
//                        switch (rem.charAt(i) - srcStr.charAt(i) + '1' - '0' + res[i+1]) {
//                            case 3:
//                                res[i+1] = '1';
//                                res[i]++;
//                                break;
//                            case 2:
//                                res[i+1] = '0';
//                                res[i]++;
//                                break;
//                            case 1:
//                                res[i+1] = '1';
//                                break;
//                            case 0:
//                                res[i+1] = '0';
//                                break;
//                        }
//                    }
//
//                    for(int i = 1; i < 33; i++) {
//                        stringBuilder.append(res[i]);
//                    }
//                    rem = stringBuilder.toString();
//                }
//                StringBuilder sb = new StringBuilder();
//                for(int i = 0; i < 32; i++) sb.append(num[i]);
//                quo = sb.toString();
//            }
        }
        remainderReg = new DataType(rem);
        return new DataType(quo);
    }

    public String Sub(String destStr, String srcStr, int k) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 32; i++) {
            if(srcStr.charAt(i) == '0') sb.append(1);
            else sb.append(0);
        }
        return Add(destStr, sb.toString(), 1, k);
    }

    public String Add(String destStr, String srcStr, int C, int k) {
        char[] res = new char[33];
        for(int i = 0; i < 33; ++i) {
            res[i] = 0;
        }
        res[32] = (char)C;
        for(int i = 31; i >= 0; --i) {
            switch (destStr.charAt(i) + srcStr.charAt(i) - '0' - '0' + res[i+1]) {
                case 3:
                    res[i+1] = '1';
                    res[i]++;
                    break;
                case 2:
                    res[i+1] = '0';
                    res[i]++;
                    break;
                case 1:
                    res[i+1] = '1';
                    break;
                case 0:
                    res[i+1] = '0';
                    break;
            }
        }
        boolean isZero = true;
        for(int i = 1; i < 33; ++i) {
            if(res[i] != '0') {
                isZero = false;
                break;
            }
        }
        //检验为0：余数和商中的余数全为0才能上商为1
        if(isZero) {
            for(int i = 0; i < 32 - k; i++) {
                if(destStr.charAt(32+i) != '0') {
                    isZero = false;
                    break;
                }
            }
        }
        if(res[1] != destStr.charAt(0) && !isZero) return destStr + "0";
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i < 33; i++) {
            sb.append(res[i]);
        }
        sb.append(destStr.substring(32));
        sb.append('1');
        return sb.toString();
    }

}
