package com.ccexid.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

/**
 * @author xuxueli 2020-04-12 0:14:00
 */
@Slf4j
public class JdkSerializeTool {

    // ------------------------ serialize and unserialize ------------------------

    /**
     * 将对象序列化为字节数组
     * 由于jedis中不支持直接存储object所以转换成byte[]存入
     *
     * @param object 待序列化的对象
     * @return 序列化后的字节数组，如果对象为null或序列化失败则返回null
     */
    public static byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        try {
            // 使用SerializationUtils工具类进行序列化操作
            return SerializationUtils.serialize((Serializable) object);
        } catch (Exception e) {
            log.error("serialize error", e);
            return null;
        }
    }


    /**
     * 将byte[]反序列化为Object
     *
     * @param bytes 需要反序列化的字节数组
     * @param clazz 目标对象的类类型
     * @return 反序列化后的对象，如果输入为空或反序列化失败则返回null
     */
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        // 检查输入参数是否为空
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            // 执行反序列化操作
            return SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
            // 记录反序列化异常日志
            log.error("deserialize error", e);
            return null;
        }
    }


}
