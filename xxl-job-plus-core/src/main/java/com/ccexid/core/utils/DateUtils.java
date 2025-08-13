package com.ccexid.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * 日期时间工具类
 * 提供日期格式化、解析及日期加减等操作
 *
 * @author xuxueli 2018-08-19 01:24:11
 */
public class DateUtils {
    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    // 日期时间格式常量
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // 线程安全的日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /**
     * 格式化日期为"yyyy-MM-dd"字符串
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date) {
        return format(date, DATE_FORMATTER);
    }

    /**
     * 格式化日期时间为"yyyy-MM-dd HH:mm:ss"字符串
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatDateTime(Date date) {
        return format(date, DATETIME_FORMATTER);
    }

    /**
     * 按照指定格式格式化日期
     *
     * @param date    日期对象
     * @param pattern 格式模板
     * @return 格式化后的字符串
     */
    public static String format(Date date, String pattern) {
        return format(date, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 按照指定格式化器格式化日期
     *
     * @param date      日期对象
     * @param formatter 日期格式化器
     * @return 格式化后的字符串
     */
    private static String format(Date date, DateTimeFormatter formatter) {
        if (date == null) {
            return null;
        }
        // 将Date转换为LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return formatter.format(localDateTime);
    }

    /**
     * 解析"yyyy-MM-dd"格式的字符串为日期对象
     *
     * @param dateString 日期字符串
     * @return 日期对象
     */
    public static Date parseDate(String dateString) {
        return parse(dateString, DATE_FORMATTER, true);
    }

    /**
     * 解析"yyyy-MM-dd HH:mm:ss"格式的字符串为日期对象
     *
     * @param dateString 日期时间字符串
     * @return 日期对象
     */
    public static Date parseDateTime(String dateString) {
        return parse(dateString, DATETIME_FORMATTER, false);
    }

    /**
     * 按照指定格式解析字符串为日期对象
     *
     * @param dateString 日期字符串
     * @param pattern    格式模板
     * @return 日期对象
     */
    public static Date parse(String dateString, String pattern) {
        return parse(dateString, DateTimeFormatter.ofPattern(pattern), false);
    }

    /**
     * 按照指定格式化器解析字符串为日期对象
     *
     * @param dateString 日期字符串
     * @param formatter  日期格式化器
     * @param isDateOnly 是否仅日期（无时间部分）
     * @return 日期对象
     */
    private static Date parse(String dateString, DateTimeFormatter formatter, boolean isDateOnly) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            if (isDateOnly) {
                LocalDate localDate = LocalDate.parse(dateString, formatter);
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } else {
                LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);
                return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            }
        } catch (DateTimeParseException e) {
            logger.warn("解析日期失败，dateString={}, 格式={}", dateString, formatter.toFormat(), e);
            return null;
        }
    }

    // ---------------------- 日期加减操作 ----------------------

    /**
     * 为日期增加指定年数
     *
     * @param date   原始日期
     * @param amount 增加的年数（负数表示减少）
     * @return 处理后的日期
     */
    public static Date addYears(Date date, int amount) {
        return add(date, (localDateTime) -> localDateTime.plusYears(amount));
    }

    /**
     * 为日期增加指定月数
     *
     * @param date   原始日期
     * @param amount 增加的月数（负数表示减少）
     * @return 处理后的日期
     */
    public static Date addMonths(Date date, int amount) {
        return add(date, (localDateTime) -> localDateTime.plusMonths(amount));
    }

    /**
     * 为日期增加指定天数
     *
     * @param date   原始日期
     * @param amount 增加的天数（负数表示减少）
     * @return 处理后的日期
     */
    public static Date addDays(Date date, int amount) {
        return add(date, (localDateTime) -> localDateTime.plusDays(amount));
    }

    /**
     * 为日期增加指定小时数
     *
     * @param date   原始日期
     * @param amount 增加的小时数（负数表示减少）
     * @return 处理后的日期
     */
    public static Date addHours(Date date, int amount) {
        return add(date, (localDateTime) -> localDateTime.plusHours(amount));
    }

    /**
     * 为日期增加指定分钟数
     *
     * @param date   原始日期
     * @param amount 增加的分钟数（负数表示减少）
     * @return 处理后的日期
     */
    public static Date addMinutes(Date date, int amount) {
        return add(date, (localDateTime) -> localDateTime.plusMinutes(amount));
    }

    // 移除自定义DateOperator，使用UnaryOperator<LocalDateTime>
    private static Date add(Date date, UnaryOperator<LocalDateTime> operator) {
        return Optional.ofNullable(date)
                .map(d -> LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()))
                .map(operator)
                .map(ldt -> Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()))
                .orElse(null);
    }
}