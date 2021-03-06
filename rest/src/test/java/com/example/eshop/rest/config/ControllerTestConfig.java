package com.example.eshop.rest.config;

import com.example.eshop.rest.AppProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({ AuthConfig.class, MappersConfig.class })
@EnableConfigurationProperties(AppProperties.class)
public class ControllerTestConfig {
}
