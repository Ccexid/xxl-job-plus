package com.ccexid.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author xuxueli 2018-10-20 20:07:26
 */
public class ThrowableUtils {
    private static final Function<Throwable, String> STACK_TRACE_EXTRACTOR = throwable -> {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    };

    public static String toString(Throwable e) {
        return Optional.ofNullable(e)
                .map(STACK_TRACE_EXTRACTOR)
                .orElse("");
    }
}