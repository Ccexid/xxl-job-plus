package com.ccexid.core.enums;

import java.util.Arrays;

public interface IEnums {

    /**
     * 通过名称匹配枚举值（忽略大小写）
     *
     * @param enumClass   枚举类的Class对象
     * @param name        要匹配的名称
     * @param defaultItem 匹配失败时返回的默认值
     * @param <E>         枚举类型
     * @return 匹配到的枚举值或默认值
     */
    static <E extends Enum<E> & IEnums> E match(Class<E> enumClass, String name, E defaultItem) {
        if (name == null || enumClass == null) {
            return defaultItem;
        }
        // 获取枚举类的所有常量
        E[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null) {
            return defaultItem;
        }
        // 遍历匹配（支持忽略大小写）
        return Arrays.stream(enumConstants)
                .filter(item -> item.name().equalsIgnoreCase(name)) // 用name()而非toString()更可靠
                .findFirst()
                .orElse(defaultItem);
    }
}
