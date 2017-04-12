package org.jflame.toolkit.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jflame.toolkit.net.IPAddressHelper;

/**
 * 常用验证方法.支持规则:
 * <ul>
 * <li>正则表达式</li>
 * <li>手机</li>
 * <li>电话号</li>
 * <li>中国身份证(15或18位)</li>
 * <li>邮箱</li>
 * <li>是否含特殊字符：*%\=><`'?&!</li>
 * <li>字符串长度</li>
 * <li>是否是字母或数字或下划线</li>
 * <li>是否是数字</li>
 * </ul>
 * 
 * @author zyc
 */
public final class ValidatorHelper {

    /**
     * 手机号正则
     */
    public static final String REGEX_MOBLIE = "^((\\+86)|(86))?0{0,1}1[3|4|5|7|8]\\d{9}$";
    /**
     * 电话号正则
     */
    public static final String REGEX_TEL = "^(0\\d{2,3}-?){0,1}\\d{7,9}$";
    /**
     * 邮箱正则
     */
    public static final String REGEX_MAIL = "^[a-zA-Z0-9_\\.]+@[a-zA-Z0-9-]+[\\.a-zA-Z]+$";
    /**
     * sql特殊字符
     */
    public final static char[] UNSAFE_SQLCHARS = { '*','%','\'','=','<','>','`',';','?','&','!' };

    /**
     * 检验指定字符串是否符合指定的正则表达式
     *
     * @param value 待验证值
     * @param pattern 正则
     * @return 匹配返回true
     */
    public static boolean regex(String value, String pattern) {
        if (value == null) {
            return false;
        }
        try {
            return Pattern.matches(pattern, value);
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    /**
     * 验证手机或电话号格式
     *
     * @param contact 待验证字符串
     * @param type 验证类型:0手机格式,1电话格式,2手机或电话格式
     * @return
     */
    public static boolean isMobileOrTel(String contact, int type) {
        boolean result = false;
        switch (type) {
            case 0:
                result = regex(contact, REGEX_MOBLIE);
                break;
            case 1:
                result = regex(contact, REGEX_TEL);
                break;
            case 2:
                result = regex(contact, REGEX_MOBLIE) || regex(contact, REGEX_TEL);
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 验证邮箱格式
     *
     * @param mail 邮箱
     * @return
     */
    public static boolean isEmail(String mail) {
        return regex(mail, REGEX_MAIL);
    }

    /**
     * 验证身份证号码
     * <p>
     * 身份号码是特征组合码，由十七位数字本体码和一位数字校验码组成. 排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码.
     * <p>
     * 顺序码: 表示在同一地址码所标识的区域范围内，对同年、同月、同 日出生的人编定的顺序号，顺序码的奇数分配给男性，偶数分配 给女性.
     * <p>
     * 第十八位数字(校验码)的计算方法为： 1.将前面的身份证号码17位数分别乘以不同的系数。从第一位到第十七位的系数分别为：7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
     * </p>
     * <p>
     * 将这17位数字和系数相乘的结果相加。用加出来和除以11，看余数是多少?余数只可能有0 1 2 3 4 5 6 7 8 9 10这11个数字。其分别对应的最后一位身份证的号码为1 0 X 9 8 7 6 5 4 3
     * 通过上面得知如果余数是2，就会在身份证的第18位数字上出现罗马数字的Ⅹ。如果余数是10，身份证的最后一位号码就是2
     *
     * @param idcard 身份证号码
     * @return
     */
    public static boolean isIDCard(String idcard) {
        if (idcard == null) {
            return false;
        }
        int len = idcard.length();
        if (len != 15 && len != 18) {
            return false;
        }
        int[] power = { 7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2 };
        if (len == 15) {
            idcard = convertIdcarBy15bit(idcard, power);
        }
        return isValidate18Idcard(idcard, power);

    }

    /**
     * 将15位的身份证转成18位身份证
     *
     * @param idcard
     * @return
     */
    private static String convertIdcarBy15bit(String idcard, int[] power) {
        String idcard17 = null;
        if (NumberUtils.isDigits(idcard)) {
            // 获取出生年月日
            String birthday = idcard.substring(6, 12);
            Date birthdate = null;
            try {
                birthdate = new SimpleDateFormat("yyMMdd").parse(birthday);
            } catch (ParseException e) {
                return null;
            }
            Calendar cday = Calendar.getInstance();
            cday.setTime(birthdate);
            String year = String.valueOf(cday.get(Calendar.YEAR));
            idcard17 = idcard.substring(0, 6) + year + idcard.substring(8);

            char[] chars = idcard17.toCharArray();

            if (null != chars) {
                int[] bits = converCharToInt(chars);
                int sum17 = getPowerSum(bits, power);
                // 获取和值与11取模得到余数进行校验码
                char checkCode = getCheckCodeBySum(sum17);
                // 获取不到校验位
                if ('\n' == checkCode) {
                    return null;
                }

                // 将前17位与第18位校验码拼接
                idcard17 += checkCode;
            }
        } else { // 身份证包含数字
            return null;
        }
        return idcard17;
    }

    /**
     * 判断是否是汉字
     *
     * @param ch 字符
     * @return
     */
    public static boolean isChinese(char ch) {
        return ChineseHelper.isChinese(ch);
    }

    /**
     * 判断是否是数字包括十六制小数
     * 
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        return NumberUtils.isNumber(str);
    }

    /**
     * 检查是否包含可能引起sql注入的字符
     *
     * @param str
     * @return 不包含返回true
     */
    public static boolean safeChar(String str) {
        return !StringUtils.containsAny(str, UNSAFE_SQLCHARS);
    }

    /**
     * 验证字符串长度是否大于或等于minLength
     * 
     * @param str 字符串
     * @param minLength 最小长度
     * @return
     */
    public static boolean minLength(String str, int minLength) {
        return stringLength(str, minLength, -1);
    }

    /**
     * 验证字符串长度是否小于或等于maxLength
     * 
     * @param str 字符串
     * @param maxLength 最大长度
     * @return
     */
    public static boolean maxLength(String str, int maxLength) {
        return stringLength(str, -1, maxLength);
    }

    /**
     * 验证字符串长度
     *
     * @param str
     * @param min 最小长度,传入负数表示不计算
     * @param max 最大长度,传入负数表示不计算
     * @return
     */
    public static boolean stringLength(String str, int min, int max) {
        if (min <= 0 && max <= 0) {
            throw new IllegalArgumentException("参数min,max不能同时小于0");
        }
        if (max > 0 && min > max) {
            throw new IllegalArgumentException("参数min应小于max");
        }
        if (min < 0 && max > 0) {
            if (str == null) {
                return true;
            } else {
                return str.length() <= max;
            }
        } else if (min > 0 && max < 0) {
            if (str == null) {
                return false;
            } else {
                return str.length() >= min;
            }
        } else if (min > 0 && max > 0) {
            if (str == null) {
                return false;
            } else {
                int len = str.length();
                return len >= min && len <= max;
            }
        }
        return false;
    }

    /**
     * 判断字符串是否字母数字或下划线组成
     *
     * @return
     */
    public static boolean isLetterOrNumOrUnderline(String str) {
        return regex(str, "^\\w+$");
    }

    /**
     * 判断只有字母
     * 
     * @param str
     * @return
     */
    public static boolean isLetter(String str) {
        return StringUtils.isAlpha(str);
    }

    /**
     * 判断是否是ip地址字符串,ip v4 or v6
     * 
     * @param ip
     * @return
     */
    public static boolean isIPAddress(String ip) {
        return IPAddressHelper.isIP(ip);
    }

    private static int[] converCharToInt(char[] c) throws NumberFormatException {
        int[] a = new int[c.length];
        int k = 0;
        for (char temp : c) {
            a[k++] = Integer.parseInt(String.valueOf(temp));
        }
        return a;
    }

    /**
     * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
     *
     * @param bit bit
     * @param power power
     * @return
     */
    private static int getPowerSum(int[] bit, int[] power) {
        int sum = 0;
        if (power.length != bit.length) {
            return sum;
        }
        for (int i = 0; i < bit.length; i++) {
            for (int j = 0; j < power.length; j++) {
                if (i == j) {
                    sum = sum + bit[i] * power[j];
                }
            }
        }
        return sum;
    }

    /**
     * 将和值与11取模得到余数进行校验码判断
     *
     * @param checkCode
     * @param sum17
     * @return 校验位
     */
    private static char getCheckCodeBySum(int sum17) {
        char checkCode = '\n';
        switch (sum17 % 11) {
            case 10:
                checkCode = '2';
                break;
            case 9:
                checkCode = '3';
                break;
            case 8:
                checkCode = '4';
                break;
            case 7:
                checkCode = '5';
                break;
            case 6:
                checkCode = '6';
                break;
            case 5:
                checkCode = '7';
                break;
            case 4:
                checkCode = '8';
                break;
            case 3:
                checkCode = '9';
                break;
            case 2:
                checkCode = 'x';
                break;
            case 1:
                checkCode = '0';
                break;
            case 0:
                checkCode = '1';
                break;
            default:
                break;
        }
        return checkCode;
    }

    private static boolean isValidate18Idcard(String idcard, int[] power) {
        // 18位身份证号码 第7、8、9、10位为出生年份(四位数)，第11、第12位为出生月份，第13、14位代表出生日期，第17位代表性别，奇数为男，偶数为女
        // 获取前17位
        String idcard17 = idcard.substring(0, 17);
        // 获取第18位
        char idcard18Code = idcard.charAt(17);
        char[] chars = null;
        /**
         * 省，直辖市代码表： { 11:"北京",12:"天津",13:"河北",14:"山西",15:"内蒙古", 21:"辽宁",22:"吉林",23:"黑龙江",31:"上海",32:"江苏",
         * 33:"浙江",34:"安徽",35:"福建",36:"江西",37:"山东",41:"河南", 42:"湖北",43:"湖南",44:"广东",45:"广西",46:"海南",50:"重庆",
         * 51:"四川",52:"贵州",53:"云南",54:"西藏",61:"陕西",62:"甘肃", 63:"青海",64:"宁夏",65:"新疆",71:"台湾",81:"香港",82:"澳门",91:"国外"}
         */
        String[] cityCode = { "11","12","13","14","15","21","22","23","31","32","33","34","35","36","37","41","42","43",
                "44","45","46","50","51","52","53","54","61","62","63","64","65","71","81","82","91" };
        // 区域码检验
        if (!ArrayUtils.contains(cityCode, idcard.substring(0, 2))) {
            return false;
        }
        // 是否都为数字
        if (NumberUtils.isDigits(idcard17)) {
            chars = idcard17.toCharArray();
        } else {
            return false;
        }

        if (null != chars) {
            int[] bits = converCharToInt(chars);
            int sum17 = getPowerSum(bits, power);
            // 将和值与11取模得到余数进行校验码判断
            char checkCode = getCheckCodeBySum(sum17);
            if (checkCode == '\n') {
                return false;
            }
            // 将身份证的第18位与算出来的校码进行匹配，不相等就为假
            if (idcard18Code != checkCode) {
                return false;
            }
        }
        return true;
    }

}
