package com.hxl.plugin.scheduledinvokestarter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerAdapter;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(HandlerAdapter.class)
public class EnabledSpringRequestMappingCollector {
    @Bean
    public SpringRequestMappingCollector springRequestMappingCollector(){
        return  new SpringRequestMappingCollector();
    }
}
