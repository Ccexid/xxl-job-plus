package com.ccexid.core.handler.impl;

import com.ccexid.core.handler.AbstractJobHandler;

import java.lang.reflect.Method;

/**
 * 方法作业处理器，用于通过反射调用目标方法执行作业
 *
 * @author xuxueli 2019-12-11 21:12:18
 */
public class MethodJobHandler extends AbstractJobHandler {

    private final Object target;
    private final Method method;
    private final Method initMethod;
    private final Method destroyMethod;

    public MethodJobHandler(Object target, Method method, Method initMethod, Method destroyMethod) {
        this.target = target;
        this.method = method;
        this.initMethod = initMethod;
        this.destroyMethod = destroyMethod;
    }

    @Override
    public void execute() throws Exception {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 0) {
            method.invoke(target, new Object[parameterTypes.length]);
        } else {
            method.invoke(target);
        }
    }

    @Override
    public void init() throws Exception {
        if (initMethod != null) {
            initMethod.invoke(target);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (destroyMethod != null) {
            destroyMethod.invoke(target);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[" + target.getClass() + "#" + method.getName() + "]";
    }
}
