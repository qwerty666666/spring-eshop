package com.example.eshop.cart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration("cart-securityConfig")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
}
