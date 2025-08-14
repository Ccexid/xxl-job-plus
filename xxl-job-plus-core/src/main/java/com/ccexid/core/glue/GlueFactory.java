package com.ccexid.core.glue;


import com.ccexid.core.glue.impl.SpringGlueFactory;
import com.ccexid.core.handler.AbstractJobHandler;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Glue工厂类，根据名称生产类/对象
 *
 * @author xuxueli 2016-1-2 20:02:27
 */
public class GlueFactory {


    private static volatile GlueFactory glueFactory = new GlueFactory();

    public static GlueFactory getInstance() {
        return glueFactory;
    }

    public static void refreshInstance(int type) {
        if (type == 0) {
            glueFactory = new GlueFactory();
        } else if (type == 1) {
            glueFactory = new SpringGlueFactory();
        }
    }


    /**
     * Groovy类加载器
     */
    private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    private final ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * 加载新的实例，原型模式
     *
     * @param codeSource 代码源
     * @return AbstractJobHandler
     * @throws Exception 异常
     */
    public AbstractJobHandler loadNewInstance(String codeSource) throws Exception {
        if (StringUtils.isNotBlank(codeSource)) {
            Class<?> clazz = getCodeSourceClass(codeSource);
            if (clazz != null) {
                Object instance = clazz.newInstance();
                if (instance != null) {
                    if (instance instanceof AbstractJobHandler) {
                        this.injectService(instance);
                        return (AbstractJobHandler) instance;
                    } else {
                        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, "
                                + "cannot convert from instance[" + instance.getClass() + "] to IJobHandler");
                    }
                }
            }
        }
        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, instance is null");
    }

    /**
     * 根据代码源获取类
     *
     * @param codeSource 代码源
     * @return 类对象
     */
    private Class<?> getCodeSourceClass(String codeSource) {
        try {
            // md5
            byte[] md5 = MessageDigest.getInstance("MD5").digest(codeSource.getBytes());
            String md5Str = new BigInteger(1, md5).toString(16);

            Class<?> clazz = CLASS_CACHE.get(md5Str);
            if (clazz == null) {
                clazz = groovyClassLoader.parseClass(codeSource);
                CLASS_CACHE.putIfAbsent(md5Str, clazz);
            }
            return clazz;
        } catch (Exception e) {
            return groovyClassLoader.parseClass(codeSource);
        }
    }

    /**
     * 注入Bean字段的服务
     *
     * @param instance 实例对象
     */
    public void injectService(Object instance) {
        // do something
    }

}
