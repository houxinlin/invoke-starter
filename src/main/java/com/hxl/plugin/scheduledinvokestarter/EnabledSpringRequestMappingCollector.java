package com.hxl.plugin.scheduledinvokestarter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerAdapter;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(HandlerAdapter.class)
public class EnabledSpringRequestMappingCollector {
    @Bean
    public Object springRequestMappingCollector() {
        try {
            URL[] urls = VersionUtils.isSpringBoot3Dot0() ? new URL[]{new File(Paths.get(Config.getLibPath(), Config.SPRING_TEST_6).toString()).toURI().toURL()} :
                    new URL[]{new File(Paths.get(Config.getLibPath(), Config.SPRING_TEST_5).toString()).toURI().toURL()};
            return Class.forName("com.hxl.plugin.scheduledinvokestarter.SpringRequestMappingCollector",
                    true, new MockClassLoader(urls, ClassLoader.getSystemClassLoader())).getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
