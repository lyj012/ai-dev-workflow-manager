package com.aidev.workflowmanager.common.config;

import com.aidev.workflowmanager.common.logging.ApiAccessLogInterceptor;
import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.EnumParser;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.enums.TaskType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@org.springframework.context.annotation.Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiAccessLogInterceptor apiAccessLogInterceptor;

    @Autowired
    public WebConfig(ApiAccessLogInterceptor apiAccessLogInterceptor) {
        this.apiAccessLogInterceptor = apiAccessLogInterceptor;
    }

    public WebConfig() {
        this.apiAccessLogInterceptor = null;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (apiAccessLogInterceptor != null) {
            registry.addInterceptor(apiAccessLogInterceptor).addPathPatterns("/api/v1/**");
        }
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, TaskType>() {
            @Override
            public TaskType convert(String source) {
                return EnumParser.parseByCode(TaskType.class, source);
            }
        });
        registry.addConverter(new Converter<String, Complexity>() {
            @Override
            public Complexity convert(String source) {
                return EnumParser.parseByCode(Complexity.class, source);
            }
        });
        registry.addConverter(new Converter<String, TaskStatus>() {
            @Override
            public TaskStatus convert(String source) {
                return EnumParser.parseByCode(TaskStatus.class, source);
            }
        });
        registry.addConverter(new Converter<String, RiskTag>() {
            @Override
            public RiskTag convert(String source) {
                return EnumParser.parseByCode(RiskTag.class, source);
            }
        });
    }
}
