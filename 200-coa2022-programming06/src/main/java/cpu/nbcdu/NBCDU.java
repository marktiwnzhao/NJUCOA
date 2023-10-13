package cpu.nbcdu;

import util.DataType;

public class NBCDU {

    /**
     * @param src  A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest + src
     */
    DataType add(DataType src, DataType dest) {
        // TODO
        String srcStr = src.toString();
        String destStr = dest.toString();
        String sSign = srcStr.substring(0, 4);
        String dSign = destStr.substring(0, 4);
        String res;
        if(sSign.equals(dSign)) {//求和
            res = Compute(destStr.substring(4), srcStr.substring(4), 0).substring(1);
        } else {//求差
            res = Compute(destStr.substring(4), RevCode(srcStr.substring(4)), 1);
            if(res.charAt(0) == '1') {//有进位代表正确
                res = res.substring(1);
            } else {//无进位取反加1，符号取反
                res = RevCode(res.substring(1));
                res = Compute(res, "0000000000000000000000000001", 0).substring(1);
                dSign = dSign.equals("1100") ? "1101" : "1100";
            }
        }
        if(res.equals("0000000000000000000000000000")) dSign = "1100";//只要为0，均返回正0
        return new DataType(dSign + res);
    }

    //反转数字，这里不是二进制的反转
    private String RevCode(String s) {
        String tmp;
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < 7; i++) {
            tmp = s.substring(4*i, 4*i+4);
            if(tmp.equals("0000")) stringBuilder.append("1001");
            else if(tmp.equals("0001")) stringBuilder.append("1000");
            else if(tmp.equals("0010")) stringBuilder.append("0111");
            else if(tmp.equals("0011")) stringBuilder.append("0110");
            else if(tmp.equals("0100")) stringBuilder.append("0101");
            else if(tmp.equals("0101")) stringBuilder.append("0100");
            else if(tmp.equals("0110")) stringBuilder.append("0011");
            else if(tmp.equals("0111")) stringBuilder.append("0010");
            else if(tmp.equals("1000")) stringBuilder.append("0001");
            else if(tmp.equals("1001")) stringBuilder.append("0000");
        }
        return stringBuilder.toString();
    }

    //C来标识进行求和还是求差，最高位表示进位
    private String Compute(String s1, String s2, int C) {
        String[] tmp = new String[7];
        StringBuilder res = new StringBuilder();
        for(int i = 6; i >= 0; --i) {
            tmp[i] = fourBitsAdd(s1.substring(4*i, 4*i+4), s2.substring(4*i, 4*i+4), C);
            C = tmp[i].charAt(0) - '0';
            if(i != 0) tmp[i] = tmp[i].substring(1);
        }
        for(int i = 0; i < 7; i++) {
            res.append(tmp[i]);
        }
        return res.toString();
    }

    //4位加法，返回5位结果，最高位为进位
    private String fourBitsAdd(String s1, String s2, int C) {
        int[] a1 = new int[4];
        int[] a2 = new int[4];
        for(int i = 0; i < 4; ++i) {
            a1[i] = s1.charAt(i) - '0';
            a2[i] = s2.charAt(i) - '0';
        }
        int k;
        for(int i = 3; i >= 0; --i) {
            k = a1[i];
            a1[i] = a1[i] ^ a2[i] ^ C;
            C = (k & a2[i]) | (k & C) | (C & a2[i]);
        }
        C = C | (a1[0] & (a1[1] | a1[2]));
        if(C == 1) {
            if(++a1[2] > 1) {
                a1[2] -= 2;
                ++a1[1];
            }
            if(++a1[1] > 1) {
                a1[1] -= 2;
                ++a1[0];
            }
            if(a1[0] > 1) a1[0] -= 2;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(C);
        for(int i = 0; i < 4; ++i) {
            stringBuilder.append(a1[i]);
        }
        return stringBuilder.toString();
    }

    /***
     *
     * @param src A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest - src
     */
    DataType sub(DataType src, DataType dest) {
        // TODO
        String srcStr = src.toString();
        String destStr = dest.toString();
        String sSign = srcStr.substring(0, 4);
        String dSign = destStr.substring(0, 4);
        String res;
        if(!sSign.equals(dSign)) {
            res = Compute(destStr.substring(4), srcStr.substring(4), 0).substring(1);
        } else {
            res = Compute(destStr.substring(4), RevCode(srcStr.substring(4)), 1);
            if(res.charAt(0) == '1') {
                res = res.substring(1);
            } else {
                res = RevCode(res.substring(1));
                res = Compute(res, "0000000000000000000000000001", 0).substring(1);
                dSign = dSign.equals("1100") ? "1101" : "1100";
            }
        }
        if(res.equals("0000000000000000000000000000")) dSign = "1100";//为0，均返回正0
        return new DataType(dSign + res);
    }

}
