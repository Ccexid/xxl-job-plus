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
     * 将对象-->byte[] (由于jedis中不支持直接存储object所以转换成byte[]存入)
     *
     * @param object
     * @return
     */
    public static byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return SerializationUtils.serialize((Serializable) object);
        } catch (Exception e) {
            log.error("serialize error", e);
            return null;
        }
    }


    /**
     * 将byte[] -->Object
     *
     * @param bytes
     * @return
     */
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
            log.error("deserialize error", e);
            return null;
        }
    }

}
