package org.jflame.commons.util;

import java.math.BigDecimal;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * 精度计算工具类
 * 
 * @author yucan.zhang
 */
public final strictfp class MathHelper {

    // 默认运算精度
    private static int DEF_SCALE = 10;

    /**
     * 字符串转换为BigDecimal
     * 
     * @param decimalStr
     * @return
     * @throws NumberFormatException 转换异常
     */
    public static final BigDecimal createBigDecimal(String decimalStr) {
        return NumberUtils.createBigDecimal(decimalStr);
    }

    /**
     * double转换为BigDecimal
     * 
     * @param number double数据
     * @return BigDecimal
     * @throws NumberFormatException 转换异常
     */
    public static final BigDecimal createBigDecimal(Number number) {
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        }
        return createBigDecimal(String.valueOf(number));
    }

    /**
     * float转换为BigDecimal
     * 
     * @param number float数据
     * @return
     */
    public static final BigDecimal createBigDecimal(float number) {
        return createBigDecimal(String.valueOf(number));
    }

    /**
     * double加法。
     * 
     * @param num1 被加数
     * @param num2 加数
     * @return 两个参数的和
     */
    public static final double add(Number num1, Number num2) {
        BigDecimal result = createBigDecimal(num1).add(createBigDecimal(num2));
        return result.setScale(DEF_SCALE, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    /**
     * double减法
     * 
     * @param num1 被减数
     * @param num2 减数
     * @return 差double
     */
    public static final double subtract(Number num1, Number num2) {
        BigDecimal result = createBigDecimal(num1).subtract(createBigDecimal(num2));
        return result.setScale(DEF_SCALE, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    /**
     * float减法
     * 
     * @param num1
     * @param num2
     * @return 差 float
     */
    public static final float subtract(float num1, float num2) {
        BigDecimal result = createBigDecimal(num1).subtract(createBigDecimal(num2));
        return result.setScale(DEF_SCALE, BigDecimal.ROUND_HALF_UP)
                .floatValue();
    }

    /**
     * 乘法,默认精度10
     * 
     * @param num1 被乘数
     * @param num2 乘数
     * @return 积
     */
    public static final double multiply(Number num1, Number num2) {
        BigDecimal result = createBigDecimal(num1).multiply(createBigDecimal(num2));
        return result.setScale(DEF_SCALE, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    /**
     * 乘法，返回double
     * 
     * @param num1 被乘数
     * @param num2 乘数
     * @param scale 精度
     * @return double积
     */
    public static final double multiply(Number num1, Number num2, int scale) {
        return multiplyDecimal(num1, num2, scale).doubleValue();
    }

    /**
     * 乘法，返回BigDecimal
     * 
     * @param num1 被乘数
     * @param num2 乘数
     * @param scale 精度
     * @return BigDecimal 积
     */
    public static final BigDecimal multiplyDecimal(Number num1, Number num2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("精度必须>=0");
        }
        BigDecimal result = createBigDecimal(num1).multiply(createBigDecimal(num2));
        return result.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 除法运算,默认精度10,超出精度以后的数字四舍五入.
     * 
     * @param num1 被除数
     * @param num2 除数
     * @return 商
     */
    public static final double divide(Number num1, Number num2) {
        return divide(num1, num2, DEF_SCALE);
    }

    /**
     * 除法运算,超出精度以后的数字四舍五入.返回double
     * 
     * @param num1 被除数
     * @param num2 除数
     * @param scale 精度
     * @return double 商
     */
    public static final double divide(Number num1, Number num2, int scale) {
        return divideDecimal(num1, num2, scale).doubleValue();
    }

    /**
     * 除法运算,超出精度以后的数字四舍五入，返回BigDecimal
     * 
     * @param num1 被除数
     * @param num2 除数
     * @param scale 精度
     * @return BigDecimal 商
     */
    public static final BigDecimal divideDecimal(Number num1, Number num2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("精度必须>=0");
        }
        BigDecimal dividend = createBigDecimal(num1);
        BigDecimal divisor = createBigDecimal(num2);
        return dividend.divide(divisor, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 提供精确的小数位四舍五入处理。
     * 
     * @param num 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static final double round(double num, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("精度必须>=0");
        }
        BigDecimal result = createBigDecimal(num).divide(createBigDecimal("1"), scale, BigDecimal.ROUND_HALF_UP);
        return result.doubleValue();
    }

    /**
     * 判断是否大于0的数,且不等于null
     * 
     * @param decimal
     * @return
     */
    public static boolean isPositiveNum(BigDecimal decimal) {
        return decimal != null && decimal.signum() == 1;
    }

    /**
     * 判断是否不等于0,且不等于null
     * 
     * @param decimal
     * @return
     */
    public static boolean isNotZero(BigDecimal decimal) {
        return decimal != null && decimal.signum() != 0;
    }

    /**
     * 小于
     * 
     * @param num1
     * @param num2
     * @return true=num1<num2
     */
    public static boolean lt(BigDecimal num1, BigDecimal num2) {
        return num1.compareTo(num2) == -1;
    }

    /**
     * 小于等于
     * 
     * @param num1
     * @param num2
     * @return true=num1<=num2
     */
    public static boolean le(BigDecimal num1, BigDecimal num2) {
        return num1.compareTo(num2) <= 0;
    }

    /**
     * 大于
     * 
     * @param num1
     * @param num2
     * @return true=num1>num2
     */
    public static boolean gt(BigDecimal num1, BigDecimal num2) {
        return num1.compareTo(num2) == 1;
    }

    /**
     * 大于等于
     * 
     * @param num1
     * @param num2
     * @return true=num1>=num2
     */
    public static boolean ge(BigDecimal num1, BigDecimal num2) {
        return num1.compareTo(num2) >= 0;
    }

    /**
     * 等于
     * 
     * @param num1
     * @param num2
     * @return true=num1==num2
     */
    public static boolean eq(BigDecimal num1, BigDecimal num2) {
        return (num1 == null && num2 == null) || num1.compareTo(num2) == 0;
    }

    /**
     * 判断integer类型不为null且大于0
     * 
     * @param i
     * @return
     */
    public static boolean gtZero(Integer i) {
        return i != null && i > 0;
    }

    /**
     * 判断Long类型不为null且大于0
     * 
     * @param i
     * @return
     */
    public static boolean gtZero(Long i) {
        return i != null && i > 0;
    }

    /**
     * 判断Double类型不为null且大于0
     * 
     * @param i
     * @return
     */
    public static boolean gtZero(Double i) {
        return i != null && i > 0;
    }
    /*
    public static void main(String[] args) {
        System.out.println(round(0.3959243086, 4));
    }*/
}
