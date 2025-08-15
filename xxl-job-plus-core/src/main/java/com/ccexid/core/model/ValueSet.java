package com.ccexid.core.model;

import lombok.Data;

/**
 * ValueSet类用于存储值和位置信息的模型类
 * 该类包含两个公共属性：value和pos，分别表示值和位置
 */
@Data
public class ValueSet {
    /**
     * 值属性，用于存储整数值
     */
    public int value;

    /**
     * 位置属性，用于存储位置信息
     */
    public int pos;
}
