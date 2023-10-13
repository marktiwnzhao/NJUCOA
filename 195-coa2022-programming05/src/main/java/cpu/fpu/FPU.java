package cpu.fpu;

import util.DataType;
import util.IEEE754Float;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {

    private final String[][] addCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN}
    };

    private final String[][] subCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN}
    };


    /**
     * compute the float add of (dest + src)
     */
    public DataType add(DataType src, DataType dest) {
        String destStr = dest.toString();
        String srcStr = src.toString();
        String result = cornerCheck(addCorner, destStr, srcStr);
        if(result != null) return new DataType(result);
        if(destStr.matches(IEEE754Float.NaN_Regular) || srcStr.matches(IEEE754Float.NaN_Regular)) return new DataType(IEEE754Float.NaN);
        //取值
        String d_sign = destStr.substring(0, 1);
        String s_sign = srcStr.substring(0, 1);
        String d_exp = destStr.substring(1, 9);
        String s_exp = srcStr.substring(1, 9);
        if(d_exp.equals("11111111")) return new DataType(destStr);
        if(s_exp.equals("11111111")) return new DataType(srcStr);
        String d_sig;
        String s_sig;
        if(d_exp.equals("00000000")) {
            d_sig = "0" + destStr.substring(9) + "000";
            d_exp = "00000001";
        }
        else d_sig = "1" + destStr.substring(9) + "000";
        if(s_exp.equals("00000000")) {
            s_sig = "0" + srcStr.substring(9) + "000";
            s_exp = "00000001";
        } else s_sig = "1" + srcStr.substring(9) + "000";
        int d = Integer.parseInt(d_exp, 2) - 127;
        int s = Integer.parseInt(s_exp, 2) - 127;
        int diff = d > s ? d-s : s-d;
        for(int i = 0; i < diff; ++i) {
            if(d > s) {
                s_sig = rightShift(s_sig, 1);
                s_exp = oneAdder(s_exp).substring(1);
            } else {
                d_sig = rightShift(d_sig, 1);
                d_exp = oneAdder(d_exp).substring(1);
            }
        }
        //模拟运算
        if(d_sign.equals(s_sign)) {
            result = Adder(d_sig, s_sig, 0);
            if(result.charAt(0) == '1') {
                if(d_exp.equals("11111110"))
                    if(d_sign.equals("0")) return new DataType(IEEE754Float.P_INF);
                    else return new DataType(IEEE754Float.N_INF);
                d_exp = oneAdder(d_exp).substring(1);
                result = rightShift(result, 1).substring(1);
                return new DataType(round(d_sign.charAt(0), d_exp, result));
            }
            result = result.substring(1);
        } else {
            result = Adder(d_sig, Anti(s_sig), 1);
            if(result.charAt(0) == '1') {
                result = result.substring(1);
            } else {
                result = oneAdder(Anti(result.substring(1))).substring(1);
                d_sign = Anti(d_sign);
            }
        }
        //非规格化
        while(result.charAt(0) != '1') {
            d_exp = Adder(d_exp, "11111111", 0).substring(1);
            result = result.substring(1) + "0";
            if(d_exp.equals("00000000")) {
                result = rightShift(result, 1);
                break;
            }
        }
        result = round(d_sign.charAt(0), d_exp, result);
        if(result.equals(IEEE754Float.N_ZERO)) return new DataType(IEEE754Float.P_ZERO);
        else return new DataType(result);
    }

    /**
     * compute the float add of (dest - src)
     */
    public DataType sub(DataType src, DataType dest) {
        String destStr = dest.toString();
        String srcStr = src.toString();
        String result = cornerCheck(subCorner, destStr, srcStr);
        if(result != null) return new DataType(result);
        if(destStr.matches(IEEE754Float.NaN_Regular) || srcStr.matches(IEEE754Float.NaN_Regular)) return new DataType(IEEE754Float.NaN);
        //取值
        String d_sign = destStr.substring(0, 1);
        String s_sign = srcStr.substring(0, 1);
        String d_exp = destStr.substring(1, 9);
        String s_exp = srcStr.substring(1, 9);
        if(d_exp.equals("11111111")) return new DataType(destStr);
        if(s_exp.equals("11111111")) {
            if(s_sign.equals("0")) return new DataType("1" + srcStr.substring(1));
            else return new DataType("0" + srcStr.substring(1));
        }
        String d_sig;
        String s_sig;
        if(d_exp.equals("00000000")) {
            d_sig = "0" + destStr.substring(9) + "000";
            d_exp = "00000001";
        }
        else d_sig = "1" + destStr.substring(9) + "000";
        if(s_exp.equals("00000000")) {
            s_sig = "0" + srcStr.substring(9) + "000";
            s_exp = "00000001";
        } else s_sig = "1" + srcStr.substring(9) + "000";
        int d = Integer.parseInt(d_exp, 2) - 127;
        int s = Integer.parseInt(s_exp, 2) - 127;
        int diff = d > s ? d-s : s-d;
        for(int i = 0; i < diff; ++i) {
            if(d > s) {
                s_sig = rightShift(s_sig, 1);
                s_exp = oneAdder(s_exp).substring(1);
            } else {
                d_sig = rightShift(d_sig, 1);
                d_exp = oneAdder(d_exp).substring(1);
            }
        }
        //模拟运算
        if(!d_sign.equals(s_sign)) {
            result = Adder(d_sig, s_sig, 0);
            if(result.charAt(0) == '1') {
                if(d_exp.equals("11111110"))
                    if(d_sign.equals("0")) return new DataType(IEEE754Float.P_INF);
                    else return new DataType(IEEE754Float.N_INF);
                d_exp = oneAdder(d_exp).substring(1);
                result = rightShift(result, 1).substring(1);
                return new DataType(round(d_sign.charAt(0), d_exp, result));
            }
            result = result.substring(1);
        } else {
            result = Adder(d_sig, Anti(s_sig), 1);
            if(result.charAt(0) == '1') {
                result = result.substring(1);
            } else {
                result = oneAdder(Anti(result.substring(1))).substring(1);
                d_sign = Anti(d_sign);
            }
        }
        //非规格化
        while(result.charAt(0) != '1') {
            d_exp = Adder(d_exp, "11111111", 0).substring(1);
            result = result.substring(1) + "0";
            if(d_exp.equals("00000000")) {
                result = rightShift(result, 1);
                break;
            }
        }
        result = round(d_sign.charAt(0), d_exp, result);
        if(result.equals(IEEE754Float.N_ZERO)) return new DataType(IEEE754Float.P_ZERO);
        else return new DataType(result);
    }

    private String Anti(String s) {
        StringBuilder tmp = new StringBuilder();
        for(int i = 0; i < s.length(); ++i) {
            if(s.charAt(i) == '0') tmp.append(1);
            else tmp.append(0);
        }
        return tmp.toString();
    }

    private String Adder(String destStr, String srcStr, int C) {
        int len = destStr.length();
        int[] dest = new int[len];
        int[] src = new int[len];
        for(int i = 0; i < len; ++i) {
            dest[i] = destStr.charAt(i) - '0';
            src[i] = srcStr.charAt(i) - '0';
        }
        int[] res = new int[len];
        for(int i = 1; i <= len; ++i) {
            res[len-i] = dest[len-i] ^ src[len-i] ^ C;
            C = (dest[len-i] & src[len-i]) | (dest[len-i] & C) | (src[len-i] & C);
        }
        StringBuilder tmp = new StringBuilder();
        tmp.append(C);
        for(int i = 0; i < len; ++i) {
            tmp.append(res[i]);
        }
        return tmp.toString();
    }

    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) && oprB.equals(matrix[1])) {
                return matrix[2];
            }
        }
        return null;
    }

    /**
     * right shift a num without considering its sign using its string format
     *
     * @param operand to be moved
     * @param n       moving nums of bits
     * @return after moving
     */
    private String rightShift(String operand, int n) {
        StringBuilder result = new StringBuilder(operand);  //保证位数不变
        boolean sticky = false;
        for (int i = 0; i < n; i++) {
            sticky = sticky || result.toString().endsWith("1");
            result.insert(0, "0");
            result.deleteCharAt(result.length() - 1);
        }
        if (sticky) {
            result.replace(operand.length() - 1, operand.length(), "1");
        }
        return result.substring(0, operand.length());
    }

    /**
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24, 27), 2);
        if ((sig_grs.substring(27).contains("1")) && (grs % 2 == 0)) {
            grs++;
        }
        String sig = sig_grs.substring(0, 24); // 隐藏位+23位
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        if (exp.equals("11111111")) {
            return sign == '0' ? IEEE754Float.P_INF : IEEE754Float.N_INF;
        }

        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carray to the next) and the remains means the result
     */
    private String oneAdder(String operand) {
        int len = operand.length();
        StringBuilder temp = new StringBuilder(operand);
        temp.reverse();
        int[] num = new int[len];
        for (int i = 0; i < len; i++) num[i] = temp.charAt(i) - '0';  //先转化为反转后对应的int数组
        int bit = 0x0;
        int carry = 0x1;
        char[] res = new char[len];
        for (int i = 0; i < len; i++) {
            bit = num[i] ^ carry;
            carry = num[i] & carry;
            res[i] = (char) ('0' + bit);  //显示转化为char
        }
        String result = new StringBuffer(new String(res)).reverse().toString();
        return "" + (result.charAt(0) == operand.charAt(0) ? '0' : '1') + result;  //注意有进位不等于溢出，溢出要另外判断
    }

}
