package cn.cxw.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by CXW-IBM on 2017/8/30.
 */

public class NumberUtil {

    /**
     * 将给定的数字按给定的形式输出
     *
     * @param d       double
     * @param pattern String
     *                <p>
     *                #:表示有数字则输出数字，没有则空，如果输出位数多于＃的位数，
     *                <p>
     *                则超长输入
     *                <p>
     *                0:有数字则输出数字，没有补0
     *                <p>
     *                对于小数，有几个＃或0，就保留几位的小数；
     *                <p>
     *                例如： "###.00" -->表示输出的数值保留两位小数，不足两位的
     *                <p>
     *                补0，多于两位的四舍五入
     *                <p>
     *                "###.0#" -->表示输出的数值可以保留一位或两位小数；
     *                <p>
     *                整数显示为有一位小数，一位或两位小数
     *                <p>
     *                的按原样显示，多于两位的四舍五入；
     *                <p>
     *                "###" --->表示为整数，小数部分四舍五入
     *                <p>
     *                ".###" -->12.234显示为.234
     *                <p>
     *                "#,###.0#" -->表示整数每隔3位加一个"，";
     * @return String
     */

    public static String formatNumber(double d, String pattern) {
        String s = "";
        try {
            DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
            nf.applyPattern(pattern);
            s = nf.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return s;

    }
}
