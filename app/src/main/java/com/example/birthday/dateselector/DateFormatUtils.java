package com.example.birthday.dateselector;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @class name：com.example.birthday
 * @description: 日期格式化工具
 * @anthor: snow
 * @time: 2019/7/2 12:10
 */
public class DateFormatUtils {
    private static final String DATE_FORMAT_PATTERN_YMD = "yyyy-MM-dd";
    private static final String DATE_FORMAT_PATTERN_YMD_HM = "yyyy-MM-dd HH:mm";

    /**
    * @description 时间戳转字符串
    * @param timestamp 时间戳
     * @param isPreciseTime 是否包含时分
    * @return 格式化的日期字符串
    * @author snow
    * @time 2019/7/2 16:20
    */
    public static String long2Str(long timestamp, boolean isPreciseTime){
        return long2Str(timestamp, getFormatPattern(isPreciseTime));
    }

    private static String long2Str(long timestamp, String pattern){
        return new SimpleDateFormat(pattern, Locale.CHINA).format(new Date(timestamp));
    }
    
    /**
    * @description 字符串转时间戳
    * @param dateStr 日期字符串
     * @param isPreciseTime 是否包含时分
    * @return 时间戳
    * @author snow
    * @time 2019/7/2 17:21
    */
    public static long str2Long(String dateStr, boolean isPreciseTime){
        return str2Long(dateStr, getFormatPattern(isPreciseTime));
    }

    private static long str2Long(String dateStr, String pattern){
        try{
            return new SimpleDateFormat(pattern, Locale.CHINA).parse(dateStr).getTime();
        }catch(Throwable ignored){
        }
        return 0;
    }

    private static String getFormatPattern(boolean showSpecificTime) {
        if(showSpecificTime){
            return DATE_FORMAT_PATTERN_YMD_HM;
        }else{
            return DATE_FORMAT_PATTERN_YMD;
        }
    }
}
