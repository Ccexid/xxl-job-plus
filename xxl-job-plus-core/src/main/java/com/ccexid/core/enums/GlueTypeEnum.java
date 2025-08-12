package com.ccexid.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务执行器类型枚举类
 *
 * 该枚举定义了不同类型的代码执行方式，包括BEAN方式和各种脚本语言的GLUE方式
 *
 * @author xuxueli
 * @since 17/4/26
 */
@Getter
@AllArgsConstructor
public enum GlueTypeEnum implements IEnum {

    /**
     * BEAN类型 - 通过Spring Bean方式执行
     */
    BEAN("BEAN", false, null, null),

    /**
     * Java脚本类型 - 使用Groovy执行Java代码
     */
    GLUE_GROOVY("GLUE(Java)", false, null, null),

    /**
     * Shell脚本类型 - 执行Shell脚本
     */
    GLUE_SHELL("GLUE(Shell)", true, "bash", ".sh"),

    /**
     * Python脚本类型 - 执行Python脚本
     */
    GLUE_PYTHON("GLUE(Python)", true, "python", ".py"),

    /**
     * PHP脚本类型 - 执行PHP脚本
     */
    GLUE_PHP("GLUE(PHP)", true, "php", ".php"),

    /**
     * Node.js脚本类型 - 执行JavaScript脚本
     */
    GLUE_NODEJS("GLUE(Nodejs)", true, "node", ".js"),

    /**
     * PowerShell脚本类型 - 执行PowerShell脚本
     */
    GLUE_POWERSHELL("GLUE(PowerShell)", true, "powershell", ".ps1");

    /**
     * 类型描述信息
     */
    private final String desc;

    /**
     * 是否为脚本类型
     * true: 脚本类型，需要通过命令行执行
     * false: 非脚本类型，如BEAN方式直接调用
     */
    private final boolean isScript;

    /**
     * 执行命令前缀
     * 例如: bash, python, php等
     */
    private final String cmd;

    /**
     * 脚本文件后缀名
     * 例如: .sh, .py, .php等
     */
    private final String suffix;

}
