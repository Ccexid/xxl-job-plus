package com.ccexid.core.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Gson JSON序列化与反序列化工具类
 * 提供对象与JSON字符串之间的转换功能
 *
 * @author xuxueli 2020-04-11 20:56:31
 */
public final class GsonUtils {
    /**
     * Gson实例（线程安全，全局共享）
     * 配置默认日期格式为"yyyy-MM-dd HH:mm:ss"
     */
    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    /**
     * 私有构造函数，防止实例化
     */
    private GsonUtils() {
        throw new AssertionError("工具类不允许实例化");
    }

    /**
     * 将对象序列化为JSON字符串
     *
     * @param source 要序列化的对象
     * @return JSON字符串，如果source为null则返回"null"
     */
    public static String toJson(Object source) {
        return GSON.toJson(source);
    }

    /**
     * 将JSON字符串反序列化为指定类型的对象
     *
     * @param json        JSON字符串
     * @param targetClass 目标对象类型
     * @param <T>         目标对象泛型
     * @return 反序列化后的对象
     */
    public static <T> T fromJson(String json, Class<T> targetClass) {
        return GSON.fromJson(json, targetClass);
    }

    /**
     * 将JSON字符串反序列化为带泛型参数的对象（如ReturnT<T>）
     *
     * @param json         JSON字符串
     * @param rawClass     原始类（如ReturnT.class）
     * @param genericClass 泛型参数类（如T.class）
     * @param <T>          目标对象泛型
     * @return 反序列化后的带泛型的对象
     */
    public static <T> T fromJsonWithGeneric(String json, Class<T> rawClass, Class<?> genericClass) {
        Type parameterizedType = new ParameterizedTypeImpl(rawClass, new Class[]{genericClass});
        return GSON.fromJson(json, parameterizedType);
    }

    /**
     * 将JSON字符串反序列化为指定类型的List集合
     *
     * @param json         JSON字符串
     * @param elementClass 集合元素类型
     * @param <T>          集合元素泛型
     * @return 反序列化后的List集合
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        Type listType = new TypeToken<List<T>>() {
        }.getType();
        return GSON.fromJson(json, listType);
    }

    /**
     * 泛型类型实现类，用于构建带泛型参数的Type
     */
    private static class ParameterizedTypeImpl implements ParameterizedType {
        private final Class<?> rawType;
        private final Type[] actualTypeArguments;

        /**
         * 构造带泛型参数的类型
         *
         * @param rawType             原始类
         * @param actualTypeArguments 泛型参数类型数组
         */
        ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments) {
            this.rawType = rawType;
            this.actualTypeArguments = actualTypeArguments;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments.clone(); // 返回副本防止外部修改
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
