package com.ccexid.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;
import java.util.function.Function;

public class JdkSerializeUtils {
    private static final Logger logger = LoggerFactory.getLogger(JdkSerializeUtils.class);
    private static final Function<Object, byte[]> SERIALIZER = obj -> {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream objectOut = new ObjectOutputStream(byteOut)) {
            objectOut.writeObject(obj);
            objectOut.flush();
            return byteOut.toByteArray();
        } catch (Exception e) {
            logger.error("对象序列化失败", e);
            return null;
        }
    };

    private static final Function<byte[], Object> DESERIALIZER = bytes -> {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
             ObjectInputStream objectIn = new ObjectInputStream(byteIn)) {
            return objectIn.readObject();
        } catch (Exception e) {
            logger.error("字节数组反序列化失败", e);
            return null;
        }
    };

    public static byte[] serialize(Object object) {
        return Optional.ofNullable(object)
                .map(SERIALIZER)
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return Optional.ofNullable(bytes)
                .filter(b -> b.length > 0)
                .map(DESERIALIZER)
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .orElse(null);
    }
}