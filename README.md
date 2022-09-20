## Mybatis动态调整SQL结构和入参
相关介绍见博文：[拿来即用的MyBatis Plugin实现SQL语句结构动态新增/更改字段](https://saint.blog.csdn.net/article/details/124158058)

如果仅仅是使用MyBatis的Plugin，请看`MybatisPluginForTrace`类，由于项目中可能会是使用低版本的`jsqlparser`，见`MybatisPluginForTrace_jsqlparser_v1_2`类；

### Mybatis Plugin逻辑
当进行insert或update操作时，Plugin会对SQL语句进行动态调整：增加operator_id、controller_action、trace_id、app_name、visit_ip五个字段和数据。

比如通过mybatis执行的SQL是：
```sql
-- 参数：张三丰2(String), 220210(String), 18(Integer), 238484@163.com(String), 123321(Long)
INSERT INTO user (user_name, id_card, age, email, operator_id) VALUES (?, ?, ?, ?, ?)
```

会被plugin修改为：
```sql
-- 张三丰2(String), 220210(String), 18(Integer), 238484@163.com(String)
INSERT INTO user (user_name, id_card, age, email, operator_id, trace_id, controller_action, app_name, visit_ip) VALUES (?, ?, ?, ?, 2333, '22dbbd771040487398a9380d9286183b', 'mock-test-action', 'h5', '192.168.1.1')
```

**注意：当原Mybatis操作中含有我们要动态插入的字段，plugin中的数据会将原Mybatis操作中的字段数据覆盖**

## MyBatis Plugin获取最上层接口的数据
使用 SpringMVC的`HandlerInterceptor`将请求中的数据、或拦截器中生成的数据，放在一个ThreadLocal中，MyBatis Plugin从ThreadLocal中获取数据；

由于一个接口中可能存在多次Mybatis操作，所以ThreadLocal的清理放在`HandlerInterceptor`的afterCompletion()方法中。

## MyBatis Plugin测试

见测试类`MybatisDynamicPluginApplicationTests`，其中包含使用Mybatis做插入、更新、批量插入...的操作验证。

## 相关表结构
见 sql目录下的user.sql文件。

## MyBatis Plugin实现原理
见我的博客：
1. [从JDK动态代理一步步推导到MyBatis Plugin插件实现原理](https://saint.blog.csdn.net/article/details/123616756)
2. [MyBatis插件/拦截器（Plugin/Interceptyor）的实现原理](https://saint.blog.csdn.net/article/details/123621762)
3. [四种方式使通用SDK中自定义Mybatis Plugin生效](https://saint.blog.csdn.net/article/details/123770342)