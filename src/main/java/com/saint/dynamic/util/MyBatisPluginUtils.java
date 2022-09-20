package com.saint.dynamic.util;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Proxy;

/**
 * MyBatis插件工具类：获取真正的委托类（目标类）
 *
 * @author Saint
 */
public final class MyBatisPluginUtils {

    private static final String HTarget = "h.target";

    /**
     * 获取真正的委托类（目标类），由于可能存在多层代理，所以采用递归方式
     */
    @SuppressWarnings("unchecked")
    public static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue(HTarget));
        }
        return (T) target;
    }
}
