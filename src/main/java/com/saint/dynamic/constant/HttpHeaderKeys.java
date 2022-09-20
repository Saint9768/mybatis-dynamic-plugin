package com.saint.dynamic.constant;

import lombok.Getter;

/**
 * TraceContext在HTTPHeader中的key
 *
 * @author Saint
 */
@Getter
public enum HttpHeaderKeys {

    CONTROLLER_ACTION("CONTROLLER_ACTION"),
    OPERATOR_ID("OPERATOR_ID"),
    TRACE_ID("TRACE_ID"),
    VISIT_IP("VISIT_IP"),
    APP_NAME("APP_NAME"),
    TRACE_CONTEXT("TRACE_CONTEXT");

    private String key;

    HttpHeaderKeys(String key) {
        this.key = key;
    }
}
