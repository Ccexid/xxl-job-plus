package com.ccexid.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * JDK序列化工具类
 * 提供对象与字节数组之间的序列化与反序列化功能
 *
 * @author xuxueli 2020-04-12 0:14:00
 */
public class JdkSerializeUtils {
    private static final Logger logger = LoggerFactory.getLogger(JdkSerializeUtils.class);

    /**
     * 将对象序列化为字节数组
     * （适用于需要字节数组存储的场景，如Redis存储）
     *
     * @param object 待序列化的对象（需实现Serializable接口）
     * @return 序列化后的字节数组，失败返回null
     */
    public static byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }

        // 使用try-with-resources自动关闭流资源
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream objectOut = new ObjectOutputStream(byteOut)) {

            objectOut.writeObject(object);
            objectOut.flush();
            return byteOut.toByteArray();

        } catch (Exception e) {
            logger.error("对象序列化失败", e);
            return null;
        }
    }

    /**
     * 将字节数组反序列化为对象
     *
     * @param bytes  待反序列化的字节数组
     * @param clazz  目标对象类型
     * @param <T>    泛型类型
     * @return 反序列化后的对象，失败返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        // 使用try-with-resources自动关闭流资源
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
             ObjectInputStream objectIn = new ObjectInputStream(byteIn)) {

            Object result = objectIn.readObject();
            return clazz.isInstance(result) ? (T) result : null;

        } catch (Exception e) {
            logger.error("字节数组反序列化失败", e);
            return null;
        }
    }
}
