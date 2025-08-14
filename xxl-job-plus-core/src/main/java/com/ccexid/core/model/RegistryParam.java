package com.ccexid.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 注册参数类，用于封装注册到调度中心的参数信息
 *
 * @author xuxueli 2017-05-10 20:22:42
 */
@Data
public class RegistryParam implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 注册组
     */
    private String registryGroup;
    /**
     * 注册键
     */
    private String registryKey;
    /**
     * 注册值
     */
    private String registryValue;

    /**
     * 无参构造函数
     */
    public RegistryParam(){}
    
    /**
     * 有参构造函数，用于初始化注册参数
     *
     * @param registryGroup 注册组
     * @param registryKey   注册键
     * @param registryValue 注册值
     */
    public RegistryParam(String registryGroup, String registryKey, String registryValue) {
        this.registryGroup = registryGroup;
        this.registryKey = registryKey;
        this.registryValue = registryValue;
    }
}