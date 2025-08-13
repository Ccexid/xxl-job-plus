package com.ccexid.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 日志结果实体类
 * 用于封装日志查询的结果信息，包括日志内容和相关元数据
 */
@Data
public class LogResult implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 起始行号
     */
    private int fromLineNum;

    /**
     * 结束行号
     */
    private int toLineNum;

    /**
     * 日志内容
     */
    private String logContent;

    /**
     * 是否为最后一部分日志
     * true表示日志已全部读取完毕，false表示还有更多日志内容
     */
    private boolean isEnd;

    public LogResult(int fromLineNum, int toLineNum, String logContent, boolean isEnd) {
        this.fromLineNum = fromLineNum;
        this.toLineNum = toLineNum;
        this.logContent = logContent;
        this.isEnd = isEnd;
    }

    public LogResult() {
    }
}
