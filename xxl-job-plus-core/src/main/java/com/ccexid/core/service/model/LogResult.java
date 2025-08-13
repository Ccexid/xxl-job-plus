package com.ccexid.core.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 日志结果封装类
 * 用于封装日志文件的读取结果信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogResult implements Serializable {
    private static final long serialVersionUID = 42L;
    private int fromLineNum;
    private int toLineNum;
    private String logContent;
    private boolean isEnd;
}

