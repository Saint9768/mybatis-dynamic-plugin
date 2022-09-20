//package com.saint.dynamic.config.mybatis;
//
//import com.saint.dynamic.constant.MybatisPluginConst;
//import com.saint.dynamic.model.MybatisTraceContext;
//import com.saint.dynamic.util.MyBatisPluginUtils;
//import com.saint.dynamic.util.StringUtil;
//import lombok.extern.slf4j.Slf4j;
//import net.sf.jsqlparser.expression.LongValue;
//import net.sf.jsqlparser.expression.StringValue;
//import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
//import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
//import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
//import net.sf.jsqlparser.parser.CCJSqlParserUtil;
//import net.sf.jsqlparser.schema.Column;
//import net.sf.jsqlparser.statement.Statement;
//import net.sf.jsqlparser.statement.insert.Insert;
//import net.sf.jsqlparser.statement.select.*;
//import net.sf.jsqlparser.statement.update.Update;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.ibatis.executor.parameter.ParameterHandler;
//import org.apache.ibatis.executor.statement.StatementHandler;
//import org.apache.ibatis.mapping.BoundSql;
//import org.apache.ibatis.mapping.MappedStatement;
//import org.apache.ibatis.mapping.ParameterMapping;
//import org.apache.ibatis.mapping.SqlCommandType;
//import org.apache.ibatis.plugin.*;
//import org.apache.ibatis.reflection.MetaObject;
//import org.apache.ibatis.reflection.SystemMetaObject;
//import org.springframework.beans.factory.annotation.Value;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.util.*;
//
///**
// * MyBatis拦截器；自定义TraceContext落盘业务表逻辑（jsqlparser1.2版本）
// * 为了兼容项目上的jsqlparser1.2版本，代码中存在部分注释。若要切回3.2，放开注释即可（Update.getTables也要改一下）
// * @author 周鑫(玖枭)
// */
//@Slf4j
//@Intercepts({@Signature(type = StatementHandler.class,
//        method = "prepare", args = {Connection.class, Integer.class}),
//        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class})
//})
//public class MybatisPluginForTrace_jsqlparser_v1_2 implements Interceptor {
//
//    /**
//     * Tables not intercepted
//     */
//    @Value("#{'${mybatis.plugin.ignoreTables:}'.split(',')}")
//    private List<String> ignoreTableList = Collections.emptyList();
//
//    @Override
//    public Object intercept(Invocation invocation) throws Throwable {
//        try {
//            String invocationName = invocation.getMethod().getName();
//
//            if (Objects.equals(invocationName, MybatisPluginConst.METHOD_PREPARE.getVale())) {
//
//                StatementHandler handler = (StatementHandler) invocation.getTarget();
//                MetaObject metaObject = SystemMetaObject.forObject(handler);
//                MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(MybatisPluginConst.DELEGATE_MAPPED_STATEMENT.getVale());
//                // sql type: UNKNOWN, INSERT, UPDATE, DELETE, SELECT, FLUSH
//                String sqlCommandType = mappedStatement.getSqlCommandType().toString();
//                // only intercept update and insert dml
//                if (!Objects.equals(sqlCommandType, SqlCommandType.UPDATE.toString())
//                        && !Objects.equals(sqlCommandType, SqlCommandType.INSERT.toString())) {
//                    return invocation.proceed();
//                }
//
//                // obtain original sql
//                String sql = metaObject.getValue(MybatisPluginConst.DELEGATE_BOUND_SQL.getVale()).toString();
//                Statement statement = CCJSqlParserUtil.parse(sql);
//                switch (sqlCommandType) {
//                    case "INSERT":
//                        prepareInsertSql(statement, metaObject);
//                        break;
//                    case "UPDATE":
//                        // can not handle, will not affect execute, but be elegant
//                        prepareUpdateSql(statement, metaObject);
//                        break;
//                    default:
//                        break;
//                }
//            } else if (Objects.equals(invocationName, MybatisPluginConst.METHOD_SET_PARAMETERS.getVale())) {
//                ParameterHandler handler = (ParameterHandler) MyBatisPluginUtils.realTarget(invocation.getTarget());
//                MetaObject metaObject = SystemMetaObject.forObject(handler);
//                MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(MybatisPluginConst.MAPPED_STATEMENT.getVale());
//                // sql type: UNKNOWN, INSERT, UPDATE, DELETE, SELECT, FLUSH
//                String sqlCommandType = mappedStatement.getSqlCommandType().toString();
//                // only intercept update and insert dml
//                if (!Objects.equals(sqlCommandType, SqlCommandType.UPDATE.toString())
//                        && !Objects.equals(sqlCommandType, SqlCommandType.INSERT.toString())) {
//                    return invocation.proceed();
//                }
//
//                BoundSql boundSql = (BoundSql) metaObject.getValue(MybatisPluginConst.BOUND_SQL.getVale());
//                Statement statement = CCJSqlParserUtil.parse(boundSql.getSql());
//                switch (sqlCommandType) {
//                    case "INSERT":
//                        Insert insert = (Insert) statement;
//                        if (!matchesIgnoreTables(insert.getTable().getName())) {
//                            handleParameterMapping(boundSql);
//                        }
//                        break;
//                    case "UPDATE":
//                        Update update = (Update) statement;
////                        if (!matchesIgnoreTables(update.getTable().getName())) {
//                        if (!matchesIgnoreTables(update.getTables().get(0).getName())) {
//                            handleParameterMapping(boundSql);
//                        }
//                        break;
//                    default:
//                        break;
//                }
//            }
//
//        } catch (Exception e) {
//            log.error("Exception in executing MyBatis Interceptor", e);
//        }
//
//        return invocation.proceed();
//    }
//
//    /**
//     * handle update sql in StatementHandler#prepare() phase
//     *
//     * @param statement  statement
//     * @param metaObject metaObject
//     */
//    private void prepareUpdateSql(Statement statement, MetaObject metaObject) {
//        Update update = (Update) statement;
////        if (matchesIgnoreTables(update.getTable().getName())) {
//        if (matchesIgnoreTables(update.getTables().get(0).getName())) {
//            return;
//        }
//
//        // if traceContext can't contain traceId、operatorId、controllerAction, directly return
//        MybatisTraceContext.TraceContext traceContext = MybatisTraceContext.getTraceContext();
//        String traceId = traceContext.getTraceId();
//        Long operatorId = traceContext.getOperatorId();
//        String appName = traceContext.getAppName();
//        String controllerAction = traceContext.getControllerAction();
//        String visitIp = traceContext.getVisitIp();
//
//        if (StringUtils.isEmpty(traceId)
//                || Objects.isNull(operatorId)
//                || StringUtils.isEmpty(controllerAction)) {
//            return;
//        }
//
//        boolean isContainsOperatorIdColumn = false;
//        int modifyDateColumnIndex = 0;
//
//        for (int i = 0; i < update.getColumns().size(); i++) {
//            Column column = update.getColumns().get(i);
//            if (column.getColumnName().equals(MybatisPluginConst.OPERATOR_ID_PROPERTY.getValue)) {
//                // sql中包含了设置的列名，则只需要设置值
//                isContainsOperatorIdColumn = true;
//                modifyDateColumnIndex = i;
//            }
//        }
//
//        if (isContainsOperatorIdColumn) {
//            updateValueWithIndex(modifyDateColumnIndex, operatorId, update);
//        } else {
//            updateValue(MybatisPluginConst.OPERATOR_ID_PROPERTY.getVale(), operatorId, update);
//        }
//
//        updateValue(MybatisPluginConst.TRACE_ID_PROPERTY.getVale(), traceId, update);
//        updateValue(MybatisPluginConst.CONTROLLER_ACTION_PROPERTY.getVale(), controllerAction, update);
//        if (StringUtils.isNotEmpty(appName)) {
//            updateValue(MybatisPluginConst.APP_NAME_PROPERTY.getVale(), appName, update);
//        }
//        if (StringUtils.isNotEmpty(visitIp)) {
//            updateValue(MybatisPluginConst.VISIT_IP_PROPERTY.getVale(), visitIp, update);
//        }
//
//        log.debug("intercept update sql is : {}", update);
//
//        metaObject.setValue("delegate.boundSql.sql", update.toString());
//
//    }
//
//    /**
//     * handle insert sql in StatementHandler#prepare() phase
//     *
//     * @param statement  statement
//     * @param metaObject metaObject
//     */
//    private void prepareInsertSql(Statement statement, MetaObject metaObject) {
//        Insert insert = (Insert) statement;
//        if (matchesIgnoreTables(insert.getTable().getName())) {
//            return;
//        }
//
//        // if traceContext can't contain traceId、operatorId、controllerAction, directly return
//        MybatisTraceContext.TraceContext traceContext = MybatisTraceContext.getTraceContext();
//        String traceId = traceContext.getTraceId();
//        Long operatorId = traceContext.getOperatorId();
//        String appName = traceContext.getAppName();
//        String controllerAction = traceContext.getControllerAction();
//        String visitIp = traceContext.getVisitIp();
//
//        if (StringUtils.isEmpty(traceId)
//                || Objects.isNull(operatorId)
//                || StringUtils.isEmpty(controllerAction)) {
//            return;
//        }
//
//        boolean isContainsOperatorIdColumn = false;
//        int createDateColumnIndex = 0;
//        for (int i = 0; i < insert.getColumns().size(); i++) {
//            Column column = insert.getColumns().get(i);
//            if (column.getColumnName().equals(MybatisPluginConst.OPERATOR_ID_PROPERTY.getValue)) {
//                // sql中包含了设置的列名，则只需要设置值
//                isContainsOperatorIdColumn = true;
//                createDateColumnIndex = i;
//            }
//        }
//
//        if (isContainsOperatorIdColumn) {
//            intoValueWithIndex(createDateColumnIndex, operatorId, insert);
//        } else {
//            intoValue(MybatisPluginConst.OPERATOR_ID_PROPERTY.getVale(), operatorId, insert);
//        }
//
//        intoValue(MybatisPluginConst.TRACE_ID_PROPERTY.getVale(), traceId, insert);
//        intoValue(MybatisPluginConst.CONTROLLER_ACTION_PROPERTY.getVale(), controllerAction, insert);
//        if (StringUtils.isNotEmpty(appName)) {
//            intoValue(MybatisPluginConst.APP_NAME_PROPERTY.getVale(), appName, insert);
//        }
//        if (StringUtils.isNotEmpty(visitIp)) {
//            intoValue(MybatisPluginConst.VISIT_IP_PROPERTY.getVale(), visitIp, insert);
//        }
//
//        log.debug("intercept insert sql is : {}", insert);
//
//        metaObject.setValue("delegate.boundSql.sql", insert.toString());
//    }
//
//    /**
//     * update sql update column value
//     *
//     * @param modifyDateColumnIndex
//     * @param columnValue
//     * @param update
//     */
//    private void updateValueWithIndex(int modifyDateColumnIndex, Object columnValue, Update update) {
//        if (columnValue instanceof Long) {
//            update.getExpressions().set(modifyDateColumnIndex, new LongValue((Long) columnValue));
//        } else if (columnValue instanceof String) {
//            update.getExpressions().set(modifyDateColumnIndex, new StringValue((String) columnValue));
//        } else {
//            // if you need to add other type data, add more if branch
//            update.getExpressions().set(modifyDateColumnIndex, new StringValue((String) columnValue));
//        }
//    }
//
//    /**
//     * update sql add column
//     *
//     * @param updateDateColumnName
//     * @param columnValue
//     * @param update
//     */
//    private void updateValue(String updateDateColumnName, Object columnValue, Update update) {
//        // 添加列
//        update.getColumns().add(new Column(updateDateColumnName));
//        if (columnValue instanceof Long) {
//            update.getExpressions().add(new LongValue((Long) columnValue));
//        } else if (columnValue instanceof String) {
//            update.getExpressions().add(new StringValue((String) columnValue));
//        } else {
//            // if you need to add other type data, add more if branch
//            update.getExpressions().add(new StringValue((String) columnValue));
//        }
//    }
//
//    /**
//     * insert sql add column
//     *
//     * @param columnName
//     * @param columnValue
//     * @param insert
//     */
//    private void intoValue(String columnName, final Object columnValue, Insert insert) {
//        // 添加列
//        insert.getColumns().add(new Column(columnName));
//        // 通过visitor设置对应的值
//        if (insert.getItemsList() == null) {
//            insert.getSelect().getSelectBody().accept(new PlainSelectVisitor(-1, columnValue));
//        } else {
//            insert.getItemsList().accept(new ItemsListVisitor() {
//                @Override
//                public void visit(SubSelect subSelect) {
//                    throw new UnsupportedOperationException("Not supported yet.");
//                }
//
//                @Override
//                public void visit(ExpressionList expressionList) {
//                    if (columnValue instanceof String) {
//                        expressionList.getExpressions().add(new StringValue((String) columnValue));
//                    } else if (columnValue instanceof Long) {
//                        expressionList.getExpressions().add(new LongValue((Long) columnValue));
//                    } else {
//                        // if you need to add other type data, add more if branch
//                        expressionList.getExpressions().add(new StringValue((String) columnValue));
//                    }
//                }
//
////                @Override
////                public void visit(NamedExpressionList namedExpressionList) {
////                    throw new UnsupportedOperationException("Not supported yet.");
////                }
//
//                @Override
//                public void visit(MultiExpressionList multiExpressionList) {
//                    for (ExpressionList expressionList : multiExpressionList.getExprList()) {
//                        if (columnValue instanceof String) {
//                            expressionList.getExpressions().add(new StringValue((String) columnValue));
//                        } else if (columnValue instanceof Long) {
//                            expressionList.getExpressions().add(new LongValue((Long) columnValue));
//                        } else {
//                            // if you need to add other type data, add more if branch
//                            expressionList.getExpressions().add(new StringValue((String) columnValue));
//                        }
//                    }
//                }
//            });
//        }
//    }
//
//    /**
//     * insert sql update column value
//     *
//     * @param index
//     * @param columnValue
//     * @param insert
//     */
//    private void intoValueWithIndex(final int index, final Object columnValue, Insert insert) {
//        // 通过visitor设置对应的值
//        if (insert.getItemsList() == null) {
//            insert.getSelect().getSelectBody().accept(new PlainSelectVisitor(index, columnValue));
//        } else {
//            insert.getItemsList().accept(new ItemsListVisitor() {
//                @Override
//                public void visit(SubSelect subSelect) {
//                    throw new UnsupportedOperationException("Not supported yet.");
//                }
//
//                @Override
//                public void visit(ExpressionList expressionList) {
//                    if (columnValue instanceof String) {
//                        expressionList.getExpressions().set(index, new StringValue((String) columnValue));
//                    } else if (columnValue instanceof Long) {
//                        expressionList.getExpressions().set(index, new LongValue((Long) columnValue));
//                    } else {
//                        // if you need to add other type data, add more if branch
//                        expressionList.getExpressions().set(index, new StringValue((String) columnValue));
//                    }
//                }
//
////                @Override
////                public void visit(NamedExpressionList namedExpressionList) {
////                    throw new UnsupportedOperationException("Not supported yet.");
////                }
//
//                @Override
//                public void visit(MultiExpressionList multiExpressionList) {
//                    for (ExpressionList expressionList : multiExpressionList.getExprList()) {
//                        if (columnValue instanceof String) {
//                            expressionList.getExpressions().set(index, new StringValue((String) columnValue));
//                        } else if (columnValue instanceof Long) {
//                            expressionList.getExpressions().set(index, new LongValue((Long) columnValue));
//                        } else {
//                            // if you need to add other type data, add more if branch
//                            expressionList.getExpressions().set(index, new StringValue((String) columnValue));
//                        }
//                    }
//                }
//            });
//        }
//    }
//
//    /**
//     * 将已经存在的列从ParameterMapping中移除
//     * 以解决原始sql语句中已包含自动添加的列 导致参数数量映射异常的问题
//     *
//     * @param boundSql
//     */
//    private void handleParameterMapping(BoundSql boundSql) {
//        List<ParameterMapping> parameterMappingList = boundSql.getParameterMappings();
//        Iterator<ParameterMapping> it = parameterMappingList.iterator();
//        String operatorIdProperty = StringUtil.snakeToCamelCase(MybatisPluginConst.OPERATOR_ID_PROPERTY.getVale());
//        while (it.hasNext()) {
//            ParameterMapping pm = it.next();
//            // 后面的条件为兼容批量插入操作
//            if (pm.getProperty().equals(operatorIdProperty) || pm.getProperty().endsWith("." + operatorIdProperty)) {
//                log.debug("原始Sql语句已包含自动添加的列: {}", operatorIdProperty);
//                it.remove();
//            }
//        }
//    }
//
//    /**
//     * 忽略处理配置的表
//     *
//     * @param tableName 当前执行的sql表
//     * @return true：表示匹配忽略的表，false：表示不匹配忽略的表
//     */
//    private boolean matchesIgnoreTables(String tableName) {
//        for (String ignoreTable : ignoreTableList) {
//            if (tableName.matches(ignoreTable)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 支持INSERT INTO SELECT 语句
//     */
//    private class PlainSelectVisitor implements SelectVisitor {
//        int index;
//        Object columnValue;
//
//        public PlainSelectVisitor(int index, Object columnValue) {
//            this.index = index;
//            this.columnValue = columnValue;
//        }
//
//        @Override
//        public void visit(PlainSelect plainSelect) {
//            if (index != -1) {
//                if (columnValue instanceof String) {
//                    plainSelect.getSelectItems().set(index, new SelectExpressionItem(new StringValue((String) columnValue)));
//                } else if (columnValue instanceof Long) {
//                    plainSelect.getSelectItems().set(index, new SelectExpressionItem(new LongValue((Long) columnValue)));
//                } else {
//                    // if you need to add other type data, add more if branch
//                    plainSelect.getSelectItems().set(index, new SelectExpressionItem(new StringValue((String) columnValue)));
//                }
//            } else {
//                if (columnValue instanceof String) {
//                    plainSelect.getSelectItems().add(new SelectExpressionItem(new StringValue((String) columnValue)));
//                } else if (columnValue instanceof Long) {
//                    plainSelect.getSelectItems().add(new SelectExpressionItem(new LongValue((Long) columnValue)));
//                } else {
//                    // if you need to add other type data, add more if branch
//                    plainSelect.getSelectItems().add(new SelectExpressionItem(new StringValue((String) columnValue)));
//                }
//            }
//        }
//
//        @Override
//        public void visit(SetOperationList setOperationList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public void visit(WithItem withItem) {
//            if (index != -1) {
//                if (columnValue instanceof String) {
//                    withItem.getWithItemList().set(index, new SelectExpressionItem(new StringValue((String) columnValue)));
//                } else if (columnValue instanceof Long) {
//                    withItem.getWithItemList().set(index, new SelectExpressionItem(new LongValue((Long) columnValue)));
//                } else {
//                    // if you need to add other type data, add more if branch
//                    withItem.getWithItemList().set(index, new SelectExpressionItem(new StringValue((String) columnValue)));
//                }
//            } else {
//                if (columnValue instanceof String) {
//                    withItem.getWithItemList().add(new SelectExpressionItem(new StringValue((String) columnValue)));
//                } else if (columnValue instanceof Long) {
//                    withItem.getWithItemList().add(new SelectExpressionItem(new LongValue((Long) columnValue)));
//                } else {
//                    // if you need to add other type data, add more if branch
//                    withItem.getWithItemList().add(new SelectExpressionItem(new StringValue((String) columnValue)));
//                }
//            }
//        }
//
////        @Override
////        public void visit(ValuesStatement valuesStatement) {
////            if (index != -1) {
////                if (columnValue instanceof String) {
////                    valuesStatement.getExpressions().set(index, new StringValue((String) columnValue));
////                } else if (columnValue instanceof Long) {
////                    valuesStatement.getExpressions().set(index, new LongValue((Long) columnValue));
////                } else {
////                    // if you need to add other type data, add more if branch
////                    valuesStatement.getExpressions().set(index, new StringValue((String) columnValue));
////                }
////            } else {
////                if (columnValue instanceof String) {
////                    valuesStatement.getExpressions().add(new StringValue((String) columnValue));
////                } else if (columnValue instanceof Long) {
////                    valuesStatement.getExpressions().add(new LongValue((Long) columnValue));
////                } else {
////                    // if you need to add other type data, add more if branch
////                    valuesStatement.getExpressions().add(new StringValue((String) columnValue));
////                }
////            }
////        }
//    }
//
//    @Override
//    public Object plugin(Object o) {
//        return Plugin.wrap(o, this);
//    }
//
//    @Override
//    public void setProperties(Properties properties) {
//        // 接收到配置文件的property参数
//    }
//}