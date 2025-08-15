package com.ccexid.admin.model;

import lombok.Data;

import java.util.Date;

/**
 * JobPlusLogGlue类用于表示任务日志的GLUE信息
 * 该类存储了任务执行相关的GLUE代码信息，包括源代码、类型、备注等
 *
 * @author yourname
 * @date 2023-XX-XX
 */
@Data
public class JobPlusLogGlue {
    /**
     * 主键ID
     */
    private Integer id;
    
    /**
     * 任务主键ID
     */
    private Integer jobId;
    
    /**
     * GLUE类型 #com.xxl.job.core.glue.GlueTypeEnum
     */
    private String glueType;
    
    /**
     * GLUE源代码
     */
    private String glueSource;
    
    /**
     * GLUE备注信息
     */
    private String glueRemark;
    
    /**
     * 创建时间
     */
    private Date addTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}

