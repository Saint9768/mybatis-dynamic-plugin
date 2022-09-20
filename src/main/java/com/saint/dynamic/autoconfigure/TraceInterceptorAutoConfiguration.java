package com.saint.dynamic.autoconfigure;

import com.saint.dynamic.config.mvc.MvcInterceptorConfig;
import com.saint.dynamic.config.mybatis.MybatisPluginForTrace;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 自动装配类
 *
 * @author Saint
 */
@Configuration
@Import({MvcInterceptorConfig.class})
public class TraceInterceptorAutoConfiguration {

    /**
     * mybatis 自定义拦截器
     */
    @Bean
    @ConditionalOnMissingBean(MybatisPluginForTrace.class)
    public Interceptor getMybatisPluginForTrace() {
        return new MybatisPluginForTrace();
    }
}
