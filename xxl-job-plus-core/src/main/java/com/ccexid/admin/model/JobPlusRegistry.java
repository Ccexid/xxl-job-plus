package com.ccexid.admin.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * JobPlusRegistry类用于表示作业注册表信息
 * 该类存储了作业调度系统中的注册信息，包括注册组、注册键、注册值以及更新时间
 */
@Data
@TableName("xxl_job_registry")
public class JobPlusRegistry {
    /**
     * 注册表记录的唯一标识符
     */
    private Integer id;

    /**
     * 注册组名称，用于对注册项进行分组管理
     */
    private String registryGroup;

    /**
     * 注册键，用于唯一标识一个注册项
     */
    private String registryKey;

    /**
     * 注册值，存储具体的注册信息内容
     */
    private String registryValue;

    /**
     * 记录更新时间，表示该注册项最后更新的时间
     */
    private Date updateTime;
}

