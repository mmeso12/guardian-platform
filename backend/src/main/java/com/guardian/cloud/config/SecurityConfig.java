package com.guardian.cloud.config;

import com.guardian.cloud.security.DeviceAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
public class SecurityConfig {

    private final DeviceAuthenticationFilter deviceAuthenticationFilter;

    public SecurityConfig(
            DeviceAuthenticationFilter deviceAuthenticationFilter
    ) {
        this.deviceAuthenticationFilter = deviceAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/health").permitAll()
                        .requestMatchers("/api/v1/device/**")
                        .hasRole("DEVICE")
                        .anyRequest()
                        .authenticated()
                )
                .addFilterAfter(
                        deviceAuthenticationFilter,
                        LogoutFilter.class
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .build();
    }
}