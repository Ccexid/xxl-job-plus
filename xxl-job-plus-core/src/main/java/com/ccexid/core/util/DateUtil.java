package com.ccexid.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

/**
 * 日期工具类
 * <p>提供常用的日期格式化、解析和计算功能</p>
 *
 * @author xuxueli 2018-08-19 01:24:11
 */
@Slf4j
public class DateUtil {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 格式化日期为"yyyy-MM-dd"格式的字符串
     *
     * @param date 待格式化的日期
     * @return 格式化后的日期字符串，如果date为null则返回null
     */
    public static String formatDate(Date date) {
        return format(date, DATE_FORMAT);
    }

    /**
     * 格式化日期为"yyyy-MM-dd HH:mm:ss"格式的字符串
     *
     * @param date 待格式化的日期
     * @return 格式化后的日期时间字符串，如果date为null则返回null
     */
    public static String formatDateTime(Date date) {
        return format(date, DATETIME_FORMAT);
    }

    /**
     * 按指定格式格式化日期
     *
     * @param date    待格式化的日期
     * @param pattern 日期格式模式
     * @return 格式化后的日期字符串，如果date为null则返回null
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return DateFormatUtils.format(date, pattern);
    }

    /**
     * 解析日期字符串为Date对象，格式为"yyyy-MM-dd"
     *
     * @param dateString 待解析的日期字符串
     * @return 解析后的Date对象，解析失败返回null
     */
    public static Date parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return DateUtils.parseDate(dateString, DATE_FORMAT);
        } catch (Exception e) {
            log.warn("parse date error, dateString = {}; errorMsg = {}", dateString, e.getMessage());
            return null;
        }
    }

    /**
     * 解析日期时间字符串为Date对象，格式为"yyyy-MM-dd HH:mm:ss"
     *
     * @param dateString 待解析的日期时间字符串
     * @return 解析后的Date对象，解析失败返回null
     */
    public static Date parseDateTime(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return DateUtils.parseDate(dateString, DATETIME_FORMAT);
        } catch (Exception e) {
            log.warn("parse datetime error, dateString = {}; errorMsg = {}", dateString, e.getMessage());
            return null;
        }
    }

    /**
     * 按指定格式解析日期字符串
     *
     * @param dateString 待解析的日期字符串
     * @param pattern    日期格式模式
     * @return 解析后的Date对象，解析失败返回null
     */
    public static Date parse(String dateString, String pattern) {
        if (dateString == null || dateString.trim().isEmpty() || pattern == null || pattern.trim().isEmpty()) {
            return null;
        }
        try {
            return DateUtils.parseDate(dateString, pattern);
        } catch (Exception e) {
            log.warn("parse date error, dateString = {}, pattern={}; errorMsg = {}", dateString, pattern, e.getMessage());
            return null;
        }
    }

    /**
     * 为指定日期增加年数
     *
     * @param date   原始日期
     * @param amount 要增加的年数（可以为负数表示减少）
     * @return 增加年数后的新日期
     */
    public static Date addYears(final Date date, final int amount) {
        if (date == null) {
            return null;
        }
        return DateUtils.addYears(date, amount);
    }

    /**
     * 为指定日期增加月数
     *
     * @param date   原始日期
     * @param amount 要增加的月数（可以为负数表示减少）
     * @return 增加月数后的新日期
     */
    public static Date addMonths(final Date date, final int amount) {
        if (date == null) {
            return null;
        }
        return DateUtils.addMonths(date, amount);
    }

    /**
     * 为指定日期增加天数
     *
     * @param date   原始日期
     * @param amount 要增加的天数（可以为负数表示减少）
     * @return 增加天数后的新日期
     */
    public static Date addDays(final Date date, final int amount) {
        if (date == null) {
            return null;
        }
        return DateUtils.addDays(date, amount);
    }

    /**
     * 为指定日期增加小时数
     *
     * @param date   原始日期
     * @param amount 要增加的小时数（可以为负数表示减少）
     * @return 增加小时数后的新日期
     */
    public static Date addHours(final Date date, final int amount) {
        if (date == null) {
            return null;
        }
        return DateUtils.addHours(date, amount);
    }

    /**
     * 为指定日期增加分钟数
     *
     * @param date   原始日期
     * @param amount 要增加的分钟数（可以为负数表示减少）
     * @return 增加分钟数后的新日期
     */
    public static Date addMinutes(final Date date, final int amount) {
        if (date == null) {
            return null;
        }
        return DateUtils.addMinutes(date, amount);
    }

    /**
     * 为指定日期增加秒数
     *
     * @param date   原始日期
     * @param amount 要增加的秒数（可以为负数表示减少）
     * @return 增加秒数后的新日期
     */
    public static Date addSeconds(final Date date, final int amount) {
        if (date == null) {
            return null;
        }
        return DateUtils.addSeconds(date, amount);
    }

    /**
     * 计算两个日期之间的天数差
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 天数差（endDate - startDate）
     */
    public static int daysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    /**
     * 判断是否为同一天
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 如果是同一天返回true，否则返回false
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return DateUtils.isSameDay(date1, date2);
    }
}
