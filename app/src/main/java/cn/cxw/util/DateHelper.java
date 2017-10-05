package cn.cxw.util;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期相关类
 * @author cxw
 *
 */
@SuppressLint("SimpleDateFormat")
public class DateHelper {
	//1秒毫秒值
    public final static long SECOND_MS = 1000;
    //1分钟毫秒值
    public final static long MINUTE_MS = 60 * SECOND_MS;
    //1小时毫秒值
    public final static long HOUR_MS = 60 * MINUTE_MS;
    //1天毫秒值
    public final static long DAY_MS = 24 * HOUR_MS;

    private final static SimpleDateFormat  DF_DATETIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private final static SimpleDateFormat  DF_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private final static SimpleDateFormat  DF_TIME = new SimpleDateFormat("HH:mm:ss");
    private final static SimpleDateFormat  DF_DAY = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 将长整形时间值转换成日期格式
     * @param longtime
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String convertTime2Date(long longtime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = sdf.format(longtime);
        return str;
    }

    /**
     * 将长整形时间转换成只有日期的时间格式
     * @param longtime
     * @return
     */
    public static String convertTime2Day(long longtime) {
        return DF_DAY.format(longtime);
    }

    /**
     * 将长整形时间转换成不带秒的时间类型
     * @param longtime
     * @return
     */
    public static String convertTime2ShortDate(long longtime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
        String str = sdf.format(longtime);
        return str;
    }

    /**
     * 将时间字符串转换为长整数时间值
     * @param text
     * @param isAlignToMinutes
     * @return
     */
    public static long convertLongTextToTime(String text, boolean isAlignToMinutes){
        //convert the time like "2014-02-24T13:24:53.747" to unix time
        Date date;
        try {
            date = DF_DATETIME.parse(text);
        } catch (ParseException e) {
            try {
                date = DF_DATE.parse(text);
            } catch (ParseException e1) {
                date = new Date();
            }
        }
        return isAlignToMinutes ? ((date.getTime()  / MINUTE_MS) * MINUTE_MS) : date.getTime();
    }

    /**
     * convert the time like "2014-02-24T13:24:53.747" to unix time
     * @param text
     * @return
     */
    public static long convertTextToDate(String text) {
        return convertTextToDate(text, DF_DAY);
    }
    

    public static long convertTextToDate(String text, String format) {
        return convertTextToDate(text, new SimpleDateFormat(format));
    }
    
    public static long convertTextToDate(String text, SimpleDateFormat format){
    	 Date date;
         try {
             date = format.parse(text);
         } catch (ParseException e) {
             date = new Date();
         }
         return date.getTime();
    }
    
    public static long convertTextToDay(String text){
    	return getZeroHourOfDay(convertTextToDate(text));
    }

    /**
     * 将时间转换为长字符串
     * @param longtime
     * @return
     */
    public static String convertTimeToLongText(long longtime) {
        String str = DF_DATETIME.format(longtime);
        return str;
    }

    /**
     * 将时间值 转换为只有时间没有有日期的时间格式
     * @param longtime
     * @return
     */
    public static String convertTimeToTimeText(long longtime) {
        String str = DF_DATE.format(longtime);
        return str;
    }

    /**
     * 根据给定的格式将指定的时间值转换成日期
     * @param longtime
     * @param format
     * @return
     */
    public static String convertTime2Date(long longtime,String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String str = sdf.format(longtime);
        return str;
    }

    /**
     * 得到今天0点的时间值
     * @return
     */
    public static long getTodayZeroHourOfDay(){
        return getZeroHourOfDay(Calendar.getInstance());
    }

    /**
     * 得到明天0点的时间值
     * @return
     */
    public static long getTomorrowZeroHourOfDay(){
        Calendar day =Calendar.getInstance();
        day.add(Calendar.DAY_OF_MONTH, 1);
        return getZeroHourOfDay(day);
    }

    /**
     * 得到后天0点的时间值
     * @return
     */
    public static long getAfterTomorrewZeroHourOfDay(){
        Calendar day =Calendar.getInstance();
        day.add(Calendar.DAY_OF_MONTH, 2);
        return getZeroHourOfDay(day);
    }

    /**
     * 得到当前星期开始时间值
     * @return
     */
    public static long getStartOfThisWeek(){
        Calendar day =Calendar.getInstance();
        int week= day.get(Calendar.DAY_OF_WEEK);
        if(week==1){
            week+=7;
        }
        day.add(Calendar.DAY_OF_YEAR, -(week-2));
        return getZeroHourOfDay(day);
    }

    /**
     * 得到当前星期结束时间值
     * @return
     */
    public static long getEndOfThisWeek(){
        return getEndOfWeek(System.currentTimeMillis());
    }

    /**
     * 根据给定的时间得到所在星期结束的时间值
     * @param time
     * @return
     */
    public static long getEndOfWeek(long time){
        Calendar day =Calendar.getInstance();
        day.setTimeInMillis(time);
        int week= day.get(Calendar.DAY_OF_WEEK);
        if(week==1){
            week+=7;
        }
        day.add(Calendar.DAY_OF_YEAR, (8-week)+1);
        return getZeroHourOfDay(day);
    }

    /**
     * 得到当前是星期几
     * @return
     */
    public static int getDayOfThisWeek(){
        return getDayOfWeek(System.currentTimeMillis());
    }

    /**
     * 得到给定时间是星期几
     * @param time
     * @return
     */
    public static int getDayOfWeek(long time){
        Calendar day =Calendar.getInstance();
        day.setTimeInMillis(time);
        int weekDay = day.get(Calendar.DAY_OF_WEEK);
        weekDay -= 1;
        // 西方的日历是 周天->1, 周一->2, 周二->3, 周三->4, 周四->5, 周五->6, 周六->7
        // weekDay =0, 说明是周天.
        if (weekDay == 0) {
            weekDay = 7;
        }
        return weekDay;
    }

    /**
     * 得到给定的时间是一年中第几周
     * @param time
     * @return
     */
    public static int getWeekOfYear(long time){
        Calendar day =Calendar.getInstance();
        day.setTimeInMillis(time);
        int week = day.get(Calendar.WEEK_OF_YEAR);
        // 西方的日历是 周天->1, 周一->2, 周二->3, 周三->4, 周四->5, 周五->6, 周六->7
        //如果是周天,系统会算到下一周. 这里需要特殊处理, 算到前一周
        if (getDayOfWeek(time) == 7) {
           week--;
        }
        return week;
    }

    /**
     * 得到给定时间值所在的月份
     * @param time
     * @return
     */
    public static int getMonthDay(long time){
        Calendar day =Calendar.getInstance();
        day.setTimeInMillis(time);
        return day.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 得到一天0点的时间值
     * @param day
     * @return
     */
    public static long getZeroHourOfDay(Calendar day){
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);
        return day.getTimeInMillis();
    }

    /**
     * 根据给定的时间获取所在日期的0点时间值
     * @param time
     * @return
     */
    public static long getZeroHourOfDay(long time){
        Calendar day =Calendar.getInstance();
        day.setTimeInMillis(time);
        return getZeroHourOfDay(day);
    }


    /**
     * 根据给定的时间获取所在日期结束时间值
     * @param time
     * @return
     */
    public static long getEndTimeOfDay(Calendar day){
        day.set(Calendar.HOUR_OF_DAY, 23);
        day.set(Calendar.MINUTE, 59);
        day.set(Calendar.SECOND, 59);
        day.set(Calendar.MILLISECOND, 0);
        return day.getTimeInMillis();
    }
    
    /**
     * 根据给定的时间获取所在日期结束时间值
     * @param time
     * @return
     */
    public static long getEndTimeOfDay(long time){
        Calendar day =Calendar.getInstance();
        day.setTimeInMillis(time);
        day.set(Calendar.HOUR_OF_DAY, 23);
        day.set(Calendar.MINUTE, 59);
        day.set(Calendar.SECOND, 59);
        return day.getTimeInMillis();
    }

    /**
     * 得到当前日期所在月第1天的时间值
     * @return
     */
    public static long getFirstDayOfMonth(){
        return getFirstDayOfMonth(System.currentTimeMillis());
    }
    
    /**
     * 获得指定月第一天0点时间值
     * @param time
     * @return
     */
    public static long getFirstDayOfMonth(long time){
        Calendar day=Calendar.getInstance();
        day.setTimeInMillis(time);
        int year = day.get(Calendar.YEAR);
        int month = day.get(Calendar.MONTH);
        day.setTimeInMillis(0);
        day.set(year, month, 1, 0, 0 , 0);
        return day.getTimeInMillis();
    }

    /**
     * 得到给定的起始时间和结束时间之间的天数差
     * @param startDate
     * @param endDate
     * @return
     */
    public static long daysBetween(long startDate, long endDate) {
//        Calendar fromDay = Calendar.getInstance();
//        fromDay.setTimeInMillis(startDate);
//
//        Calendar endDay = Calendar.getInstance();
//        endDay.setTimeInMillis(endDate);
    	long offset = endDate - startDate;

//        return endDay.get(Calendar.DAY_OF_YEAR) - fromDay.get(Calendar.DAY_OF_YEAR);
    	return offset / DAY_MS;
    }

    /**
     * 得到当前分钟值
     * @return
     */
    public static long currentMinute(){
        // 调整到分
        return (System.currentTimeMillis() / MINUTE_MS) * MINUTE_MS;
    }

    /**
     * 得到当前小时值
     * @return
     */
    public static long currentHour(){
        // 调整到小时
        return (System.currentTimeMillis() / HOUR_MS) * HOUR_MS;
    }

    public static String getDayStringOffsetToday(int offsetDays){
    	long offsetTime = offsetDays * DAY_MS;
    	return convertTime2Day(System.currentTimeMillis() + offsetTime);
    }
    /**
     * 用于计算起始时间与结束时间之间的时间
     * @param startTime
     * @param endTime
     * @return
     */
    public static String getTimeDistance(String startTime, String endTime){
    	long distance = getMSDistanse(startTime, endTime);
    	return getTimeLengthString(distance);
    }
    
    public static long getMSDistanse(String startTime, String endTime){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	long start = convertTextToDate(startTime, sdf);
    	long end = convertTextToDate(endTime, sdf);
    	long distance = end + MINUTE_MS - start;
    	return distance;
    }
    
    /**
     * 得到起始时间与结束时间之间的小时值
     * @param startTime
     * @param endTime
     * @return
     */
    public static long getHoursDistanse(String startTime, String endTime){
    	return getMSDistanse(startTime, endTime) / HOUR_MS;
    }
    
    /**
     * 得到起始时间与结束时间之间的分钟值    		
     * @param startTime
     * @param endTime
     * @return
     */
    public static long getMinutesDistance(String startTime, String endTime){
    	return (getMSDistanse(startTime, endTime) % HOUR_MS) / MINUTE_MS;
    }
    
    /**
     * 获取时间长度的文本表示
     * @param ms
     * @return
     */
    public static String getTimeLengthString(long ms){
    	if(ms == 0)
    		return "0 分";
    	long hours = ms / HOUR_MS;
    	long minutes = (ms % HOUR_MS) / MINUTE_MS;
		long seconds = (ms % MINUTE_MS) / SECOND_MS;
    	if(hours == 0 && minutes == 0){
    		return seconds + "秒";
    	}
    	if(seconds >= 30)
    		minutes++;
    	String resHours = hours > 0 ? hours + " 小时" : ""; 
    	String resMinutes = minutes > 0 ? minutes + " 分" : ""; 
    	return resHours + " " + resMinutes;
    }
}
