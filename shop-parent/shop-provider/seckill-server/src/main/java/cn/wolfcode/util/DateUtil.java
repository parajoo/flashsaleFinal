package cn.wolfcode.util;

import java.util.Calendar;
import java.util.Date;


public class DateUtil {
    /**
     * 根据日期和场次看是否在秒杀有效时间之内
     *
     * @param startDate
     * @param time
     * @return
     */
    public static boolean isLegalTime(Date startDate, int time) {
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.HOUR_OF_DAY, time);
        // 开始的时间戳
        long start = c.getTime().getTime();

        // 当前时间戳
        long now = new Date().getTime();

        // 为开始时间 + 2小时
        c.add(Calendar.HOUR_OF_DAY, 2);

        // 获取到开始时间两小时后的时间戳 == 结束时间
        long end = c.getTime().getTime();
        // 判断如果当前时间 >= 开始时间 && <= 结束时间
        return now >= start && now < end;
    }
}
