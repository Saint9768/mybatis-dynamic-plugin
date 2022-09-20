package com.saint.dynamic.constant;

import lombok.Getter;

/**
 * MyBatis拦截器中使用到的常量
 *
 * @author Saint
 */
@Getter
public enum MybatisPluginConst {
    /**
     * 在这里修改业务表字段名
     */
    TRACE_ID_PROPERTY("trace_id"),
    CONTROLLER_ACTION_PROPERTY("controller_action"),
    OPERATOR_ID_PROPERTY("operator_id"),
    VISIT_IP_PROPERTY("visit_ip"),
    APP_NAME_PROPERTY("app_name"),
    DELEGATE_BOUND_SQL("delegate.boundSql.sql"),
    BOUND_SQL("boundSql"),
    DELEGATE_MAPPED_STATEMENT("delegate.mappedStatement"),
    MAPPED_STATEMENT("mappedStatement"),
    METHOD_PREPARE("prepare"),
    METHOD_SET_PARAMETERS("setParameters");

    private String vale;

    MybatisPluginConst(String vale) {
        this.vale = vale;
    }
}
