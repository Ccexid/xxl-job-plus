package com.ccexid.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author xuxueli 2018-10-20 20:07:26
 */
public class ThrowableUtil {
    /**
     * 将Throwable异常转换为字符串形式
     *
     * @param e 需要转换的Throwable异常对象
     * @return 异常的完整堆栈信息字符串
     */
    public static String toString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}
