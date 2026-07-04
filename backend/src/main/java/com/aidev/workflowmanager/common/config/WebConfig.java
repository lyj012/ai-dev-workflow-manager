package com.aidev.workflowmanager.common.config;

import com.aidev.workflowmanager.common.enums.Complexity;
import com.aidev.workflowmanager.common.enums.EnumParser;
import com.aidev.workflowmanager.common.enums.RiskTag;
import com.aidev.workflowmanager.common.enums.TaskStatus;
import com.aidev.workflowmanager.common.enums.TaskType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@org.springframework.context.annotation.Configuration
public class WebConfig implements WebMvcConfigurer {

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
