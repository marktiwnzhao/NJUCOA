package cpu.fpu;

import util.DataType;
import util.IEEE754Float;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {

    private final String[][] mulCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN}
    };

    private final String[][] divCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
    };


    /**
     * compute the float mul of dest * src
     */
    public DataType mul(DataType src, DataType dest) {
        // TODO
        String srcStr = src.toString();
        String destStr = dest.toString();
        //非数正则检查
        if(destStr.matches(IEEE754Float.NaN_Regular) || srcStr.matches(IEEE754Float.NaN_Regular)) return new DataType(IEEE754Float.NaN);
        //检查表的内容
        String res = cornerCheck(mulCorner, destStr, srcStr);
        if(res != null) return new DataType(res);
        //取符号位，指数位和尾数位
        String dSign = destStr.substring(0, 1);
        String sSign = srcStr.substring(0, 1);
        String resSign = dSign.equals(sSign) ? "0" : "1";
        String dExp = destStr.substring(1, 9);
        String sExp = srcStr.substring(1, 9);
        //无穷判断
        if(dExp.equals("11111111")) return new DataType(resSign + destStr.substring(1));
        if(sExp.equals("11111111")) return new DataType(resSign + srcStr.substring(1));
        String dSig;
        String sSig;
        //尾数：1+23+3
        if(dExp.equals("00000000")) {
            dSig = "0" + destStr.substring(9) + "000";
            dExp = "00000001";
        }
        else dSig = "1" + destStr.substring(9) + "000";
        if(sExp.equals("00000000")) {
            sSig = "0" + srcStr.substring(9) + "000";
            sExp = "00000001";
        } else sSig = "1" + srcStr.substring(9) + "000";
        //如果为0，要和被乘数符号相同
        if(dSig.equals("000000000000000000000000000") || sSig.equals("000000000000000000000000000"))
            return new DataType(resSign + "0000000000000000000000000000000");
        int d = Integer.parseInt(dExp, 2) - 127;
        int s = Integer.parseInt(sExp, 2) - 127;
        d += s+127;//d是真值，所以这里用加
        //原码乘法
        String resSig = sigMul(dSig, sSig);
        d++;//阶码加1，小数点左移
        while(resSig.charAt(0) == '0' && d > 0) {
            resSig = resSig.substring(1) + "0";
            d--;
        }
        while(!resSig.substring(0, 27).equals("000000000000000000000000000") && d < 0) {
            resSig = rightShift(resSig, 1);
            d++;
        }
        if(d > 254) {
            if(resSign.equals("0")) return new DataType(IEEE754Float.P_INF);
            else return new DataType(IEEE754Float.N_INF);
        } else if(d < 0) {
            if(resSign.equals("0")) return new DataType(IEEE754Float.P_ZERO);
            else return new DataType(IEEE754Float.N_ZERO);
        } else if(d == 0) {
            resSig = rightShift(resSig, 1);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < 8; i++) {
            stringBuilder.append(d % 2);
            d /= 2;
        }
        String resExp = stringBuilder.reverse().toString();
        res = round(resSign.charAt(0), resExp, resSig);
        return new DataType(res);
    }

    private String sigMul(String sig1, String sig2) {
        //这里没有进行溢出判断
        int[] s1 = new int[54];
        int[] s2 = new int[27];
        for(int i = 0; i < 27; i++) {
            s1[i] = 0;
            s1[i+27] = sig1.charAt(i) - '0';
            s2[i] = sig2.charAt(i) - '0';
        }
        int C;
        for(int i = 0; i < 27; i++) {
            C = s1[53];
            if(C == 1) {
                C = 0;
                for(int j = 26; j >= 0; j--) {
                    int k = s1[j];
                    s1[j] = s1[j] ^ s2[j] ^ C;
                    C = (k & C) | (s2[j] & C) | (k & s2[j]);
                }
            }
            for(int j = 53; j > 0; j--) {
                s1[j] = s1[j-1];
            }
            //右移的时候补的是上次的进位，不是0！！！
            //布斯乘法是符号扩展
            s1[0] = C;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < 54; i++) {
            stringBuilder.append(s1[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * compute the float mul of dest / src
     */
    public DataType div(DataType src, DataType dest) {
        // TODO
        String srcStr = src.toString();
        String destStr = dest.toString();
        if(destStr.matches(IEEE754Float.NaN_Regular) || srcStr.matches(IEEE754Float.NaN_Regular)) return new DataType(IEEE754Float.NaN);
        String res = cornerCheck(divCorner, destStr, srcStr);
        if(res != null) return new DataType(res);

        String dSign = destStr.substring(0, 1);
        String sSign = srcStr.substring(0, 1);
        String resSign = dSign.equals(sSign) ? "0" : "1";
        String dExp = destStr.substring(1, 9);
        String sExp = srcStr.substring(1, 9);
        if(dExp.equals("11111111")) return new DataType(resSign + destStr.substring(1));
        if(sExp.equals("11111111")) return new DataType(resSign + "0000000000000000000000000000000");
        String dSig;
        String sSig;
        if(dExp.equals("00000000")) {
            dSig = "0" + destStr.substring(9) + "000";
            dExp = "00000001";
        }
        else dSig = "1" + destStr.substring(9) + "000";
        if(sExp.equals("00000000")) {
            sSig = "0" + srcStr.substring(9) + "000";
            sExp = "00000001";
        } else sSig = "1" + srcStr.substring(9) + "000";
        //除数为0时，被除数为0返回非数，不为0抛出异常
        if(sSig.equals("000000000000000000000000000")) {
            if(dSig.equals("000000000000000000000000000")) {
                return new DataType(IEEE754Float.NaN);
            } else throw new ArithmeticException();
        }
        //如果为0，要和被除数符号相同
        if(dSig.equals("000000000000000000000000000")) {
            if(dSign.equals("0")) return new DataType(IEEE754Float.P_ZERO);
            else return new DataType(IEEE754Float.N_ZERO);
        }
        int d = Integer.parseInt(dExp, 2) - 127;
        int s = Integer.parseInt(sExp, 2) - 127;
        d -= s;
        d += 127;//这里仍然为+127
        //无符号定点小数除法
        String resSig = sigDiv(dSig, sSig);
        while(resSig.charAt(0) == '0' && d > 0) {
            resSig = resSig.substring(1) + "0";
            d--;
        }
        while(!resSig.equals("000000000000000000000000000") && d < 0) {
            resSig = rightShift(resSig, 1);
            d++;
        }
        if(d > 254) {
            if(resSign.equals("0")) return new DataType(IEEE754Float.P_INF);
            else return new DataType(IEEE754Float.N_INF);
        } else if(d < 0) {
            if(resSign.equals("0")) return new DataType(IEEE754Float.P_ZERO);
            else return new DataType(IEEE754Float.N_ZERO);
        } else if(d == 0) {
            resSig = rightShift(resSig, 1);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < 8; i++) {
            stringBuilder.append(d % 2);
            d /= 2;
        }
        String resExp = stringBuilder.reverse().toString();
        res = round(resSign.charAt(0), resExp, resSig);
        return new DataType(res);
    }

    private String sigDiv(String sig1, String sig2) {
        //无符号定点小数除法
        //要在后面补0而不是在前面！！！
        //前面补一位0防止左移的时候丢弃掉1：详见test5
        //商放在第55位，而不是54位，因为我们是先上商后左移
        int[] res = new int[56];
        int[] s = new int[28];
        int[] tmp = new int[28];
        s[0] = 1;
        for(int i = 0; i < 27; i++) {
            res[i+1] = sig1.charAt(i) - '0';
            s[i+1] = '1' - sig2.charAt(i);
        }
        int C;
        //执行原码减法
        for(int i = 0; i < 27; i++) {
            C = 1;
            for(int j = 27; j >= 0; --j) {
                tmp[j] = res[j] ^ s[j] ^ C;
                C = (res[j] & C) | (s[j] & C) | (res[j] & s[j]);
            }
            //最高位有进位，表明够减
            if(C == 1) {
                for(int j = 0; j < 28; ++j) {
                    res[j] = tmp[j];
                }
                res[55] = 1;
            } else res[55] = 0;
            for(int j = 0; j < 55; j++) {
                res[j] = res[j+1];
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < 27; ++i) {
            stringBuilder.append(res[i+28]);
        }
        return stringBuilder.toString();
    }


    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) &&
                    oprB.equals(matrix[1])) {
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
        if (grs > 4 || (grs == 4 && sig.endsWith("1"))) {
            sig = oneAdder(sig);
            if (sig.charAt(0) == '1') {
                exp = oneAdder(exp).substring(1);
                sig = sig.substring(1);
            }
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
        StringBuffer temp = new StringBuffer(operand);
        temp = temp.reverse();
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
