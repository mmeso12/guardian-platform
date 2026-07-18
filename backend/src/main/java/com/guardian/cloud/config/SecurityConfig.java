package com.guardian.cloud.config;

import com.guardian.cloud.security.DeviceAuthenticationFilter;
import com.guardian.cloud.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
public class SecurityConfig {

    private final DeviceAuthenticationFilter deviceAuthenticationFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            DeviceAuthenticationFilter deviceAuthenticationFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.deviceAuthenticationFilter = deviceAuthenticationFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
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
                        .requestMatchers("/api/v1/health")
                        .permitAll()

                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login"
                        )
                        .permitAll()

                        .requestMatchers("/api/v1/device/**")
                        .hasRole("DEVICE")

                        .requestMatchers("/api/v1/auth/me")
                        .authenticated()

                        .anyRequest()
                        .authenticated()
                )
                .addFilterAfter(
                        deviceAuthenticationFilter,
                        LogoutFilter.class
                )
                .addFilterAfter(
                        jwtAuthenticationFilter,
                        DeviceAuthenticationFilter.class
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .build();
    }
}