package com.saint.dynamic.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 链路信息上下文
 *
 * @author Saint
 */
public class MybatisTraceContext implements Serializable {

    // 可继承的ThreadLocal，如果使用线程池做异步处理，需要用阿里开源的TransmittableThreadLocal
    private final static InheritableThreadLocal<TraceContext> traceContextHolder = new InheritableThreadLocal<>();

    public static InheritableThreadLocal<TraceContext> get() {
        return traceContextHolder;
    }

    /**
     * 设置traceContext
     *
     * @param traceContext traceContext
     */
    public static void setTraceContext(TraceContext traceContext) {
        traceContextHolder.set(traceContext);
    }

    /**
     * 获取traceContext
     *
     * @return traceContext
     */
    public static TraceContext getTraceContext() {
        return traceContextHolder.get();
    }

    /**
     * 清空trace上下文
     */
    public static void clear() {
        traceContextHolder.remove();
    }

    @Data
    @Accessors(chain = true)
    public static class TraceContext implements Serializable {
        private Long operatorId;
        private String traceId;
        private String controllerAction;
        private String visitIp;
        private String appName;
    }
}
