package com.saint.dynamic.config.mvc;

import com.saint.dynamic.constant.HttpHeaderKeys;
import com.saint.dynamic.model.MybatisTraceContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 自定义MVC拦截器
 *
 * @author Saint
 */
public class MvcInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String controllerAction = request.getHeader(HttpHeaderKeys.CONTROLLER_ACTION.getKey());
        String traceId = request.getHeader(HttpHeaderKeys.TRACE_ID.getKey());
        String operatorIdString = request.getHeader(HttpHeaderKeys.OPERATOR_ID.getKey());
        String appName = request.getHeader(HttpHeaderKeys.APP_NAME.getKey());
        String visitIp = request.getHeader(HttpHeaderKeys.VISIT_IP.getKey());

        Long operatorId;

        // 1. controllerAction
        if (StringUtils.isEmpty(controllerAction)) {
            HandlerMethod chain = (HandlerMethod) handler;
            String controllerName = chain.getBeanType().getName();
            String methodName = chain.getMethod().getName();
            controllerAction = controllerName + "#" + methodName;
        }

        // 2. traceId
        if (StringUtils.isEmpty(traceId)) {
            traceId = UUID.randomUUID().toString().replaceAll("-", "");
        }

        // 3.userId
        if (StringUtils.isEmpty(operatorIdString)) {
            // todo 如果请求头中没传入用户ID，则用相应的逻辑获取，此处mock处理
            operatorId = 110119L;

        } else {
            try {
                operatorId = Long.valueOf(operatorIdString);
            } catch (Exception e) {
                // todo 如果请求头中没传入用户ID，则用相应的逻辑获取，此处mock处理
                operatorId = 110119L;
            }
        }
        // 4.visitIp
        if (StringUtils.isEmpty(visitIp)) {
            visitIp = getIP(request);
        }
        MybatisTraceContext.TraceContext traceContext = new MybatisTraceContext.TraceContext()
                .setControllerAction(controllerAction)
                .setOperatorId(operatorId)
                .setTraceId(traceId)
                .setAppName(appName)
                .setVisitIp(visitIp);

        // TraceContext内容放入到ThreadLocal
        MybatisTraceContext.setTraceContext(traceContext);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        // 清空ThreadLocal中的信息
        MybatisTraceContext.clear();
    }

    /**
     * 获取请求的IP地址
     *
     * @param request request
     * @return
     */
    private String getIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}