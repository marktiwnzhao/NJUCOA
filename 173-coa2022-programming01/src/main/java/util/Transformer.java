package util;


import java.util.HashMap;
import java.util.Map;

public class Transformer {

    public static String intToBinary(String numStr) {
        int a = Integer.parseInt(numStr);
        boolean isminus = false;
        if(a < 0) {
            a = -a;
            isminus = true;
        }
        int[] num = new int[32];
        int index = 31;
        while(a / 2 != 0) {
            num[index] = a % 2;
            index--;
            a = a / 2;
        }
        num[index] = a;
        if(isminus) {
            for(int i = 31; i >= 0; --i) num[i] = 1 - num[i];
            for(int i = 31; i >= 0; --i) {
                if(num[i] == 1) {
                    num[i] = 0;
                } else {
                    num[i] = 1;
                    break;
                }
            }
        }
        StringBuffer br = new StringBuffer();
        for(int i : num) {
            br.append(i);
        }
        return br.toString();
    }

    public static String binaryToInt(String binStr) {
        int value = -(binStr.charAt(0) - '0');
        for(int i = 1; i < binStr.length(); ++i) {
            value *= 2;
            value += binStr.charAt(i) - '0';
        }
        return String.valueOf(value);
    }

    public static String decimalToNBCD(String decimalStr) {
        int value = Integer.parseInt(decimalStr);
        String sign = value < 0 ? "1101" : "1100";
        if(value < 0) value = -value;
        Map<Integer, String> m = new HashMap<>();
        m.put(1, "0001");m.put(2, "0010");m.put(3, "0011");m.put(4,"0100");m.put(5, "0101");
        m.put(6, "0110");m.put(7, "0111");m.put(8, "1000");m.put(9, "1001");m.put(0, "0000");
        StringBuffer br = new StringBuffer();
        br.append(sign);
        int[] num = new int[7];
        for(int i = 6; i >= 0; --i) {
            num[i] = value % 10;
            value /= 10;
        }
        for(int i = 0; i < 7; i++) br.append(m.get(num[i]));
        return br.toString();
    }

    public static String NBCDToDecimal(String NBCDStr) {
        int sign;
        int value = 0;
        if((NBCDStr.substring(0, 4)).equals("1100")) {
            sign = 1;
        } else {
            sign = -1;
        }
        Map<String, Integer> m = new HashMap<>();
        m.put("0001", 1);m.put("0010", 2);m.put("0011", 3);m.put("0100", 4);m.put("0101", 5);
        m.put("0110", 6);m.put("0111", 7);m.put("1000", 8);m.put("1001", 9);m.put("0000", 0);
        for(int i = 1; i <= 7; ++i) {
            value *= 10;
            value += m.get(NBCDStr.substring(4*i, 4*i+4));
        }
        return String.valueOf(sign*value);
    }

    public static String floatToBinary(String floatStr) {
        int sLen = 23;
        int eLen = 8;
        Float f = Float.valueOf(floatStr);
        boolean isNeg = f < 0;
        if(Float.isInfinite(f)) return isNeg ? "-Inf" : "+Inf";
        StringBuffer br = new StringBuffer();
        if(isNeg) br.append("1");
        else br.append("0");
        if(f == 0.0) {
            for(int i = 0; i < (eLen + sLen); ++i) {
                br.append("0");
            }
            return br.toString();
        } else {
            if (isNeg) f = -f;
            if (f < Math.pow(2, -126)) {
                for (int i = 0; i < eLen; ++i) br.append("0");
                f *= (float)Math.pow(2, 126);
                for (int i = 0; i < sLen; ++i) {
                    f *= 2f;
                    if (f < 1f) br.append("0");
                    else {
                        f -= 1f;
                        br.append("1");
                    }
                }
                return br.toString();
            } else {
                int count = 0;
                int[] num = new int[8];
                if (f >= 2f) {
                    while (f >= 2f) {
                        f /= 2f;
                        count++;
                    }
                } else if (f < 1f) {
                    while (f < 1f) {
                        f *= 2f;
                        count--;
                    }
                }
                count += 127;
                for (int i = 7; i >= 0; --i) {
                    num[i] = count % 2;
                    count /= 2;
                }
                for(int i = 0; i < eLen; ++i) {
                    br.append(num[i]);
                }
                f -= 1f;
                for (int i = 0; i < sLen; i++) {
                    f *= 2f;
                    if (f < 1f) br.append("0");
                    else {
                        --f;
                        br.append("1");
                    }
                }
                return br.toString();
            }
        }
    }

    public static String binaryToFloat(String binStr) {
        int sign = binStr.charAt(0) == '0' ? 1 : -1;
        String eStr = binStr.substring(1, 9);
        String sStr = binStr.substring(9);
        int E = eStr.charAt(0) - '0';
        for(int i = 1; i <= 7; ++i) {
            E *= 2;
            E += eStr.charAt(i) - '0';
        }
        double d = 0.0;
        for(int i = 22; i >= 0; i--) {
            d += sStr.charAt(i) - '0';
            d /= 2;
        }
        if(E == 0) {
            if(d == 0.0) {
                return String.valueOf(0f);
            } else {
                d *= Math.pow(2, -126);
                return String.valueOf((float) (sign*d));
            }
        }
        if(E == 255) {
            return sign == 1 ? "+Inf" : "-Inf";
        }
        d += 1;
        d *= Math.pow(2, E - 127);
        return String.valueOf((float) (sign*d));
    }

}
