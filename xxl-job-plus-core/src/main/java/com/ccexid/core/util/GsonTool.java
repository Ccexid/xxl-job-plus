package com.ccexid.core.util;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author xuxueli 2020-04-11 20:56:31
 */
public class GsonTool {

    private static final Gson GSON;

    static {
        GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    /**
     * Object 转成 json
     *
     * @param src source object
     * @return json string
     */
    public static String toJson(Object src) {
        return GSON.toJson(src);
    }

    /**
     * json 转成 特定的cls的Object
     *
     * @param json json string
     * @param classOfT target class
     * @return target object
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    /**
     * json 转成 特定的 rawClass<classOfT> 的Object
     *
     * @param json json string
     * @param classOfT raw class
     * @param argClassOfT argument class
     * @return target object
     */
    public static <T> T fromJson(String json, Class<T> classOfT, Class<?> argClassOfT) {
        Type type = new ParameterizedType4ReturnT(classOfT, new Class[]{argClassOfT});
        return GSON.fromJson(json, type);
    }

    public static class ParameterizedType4ReturnT implements ParameterizedType {
        private final Class<?> raw;
        private final Type[] args;

        public ParameterizedType4ReturnT(Class<?> raw, Type[] args) {
            this.raw = raw;
            this.args = args != null ? args : new Type[0];
        }

        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

    /**
     * json 转成 特定的cls的list
     *
     * @param json json string
     * @param classOfT element class
     * @return list of target objects
     */
    public static <T> List<T> fromJsonList(String json, Class<T> classOfT) {
        return GSON.fromJson(
                json,
                new TypeToken<List<T>>() {
                }.getType()
        );
    }

}
