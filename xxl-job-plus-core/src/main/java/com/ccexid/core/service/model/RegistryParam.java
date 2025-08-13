package com.ccexid.core.service.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 注册参数模型类
 * 用于封装注册相关的参数信息，包含注册组、注册键和注册值
 * 实现了Serializable接口，支持序列化操作
 */
@Data
public class RegistryParam implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 注册组名称
     */
    private String registryGroup;

    /**
     * 注册键名
     */
    private String registryKey;

    /**
     * 注册键值
     */
    private String registryValue;

    /**
     * 重写toString方法，返回RegistryParam对象的字符串表示
     * 格式为：RegistryParam{registryGroup='值', registryKey='值', registryValue='值'}
     *
     * @return 包含所有字段信息的字符串
     */
    @Override
    public String toString() {
        return "RegistryParam{" +
                "registryGroup='" + registryGroup + '\'' +
                ", registryKey='" + registryKey + '\'' +
                ", registryValue='" + registryValue + '\'' +
                '}';
    }
}

