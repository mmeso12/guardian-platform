package com.guardian.cloud.security;

import com.guardian.cloud.entity.GuardianUser;
import com.guardian.cloud.repository.GuardianUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final GuardianUserRepository guardianUserRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            GuardianUserRepository guardianUserRepository
    ) {
        this.jwtService = jwtService;
        this.guardianUserRepository = guardianUserRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/v1/device/")
                || path.equals("/api/v1/health")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (
                authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (
                jwtService.isValid(token)
                && SecurityContextHolder.getContext()
                        .getAuthentication() == null
        ) {
            String email = jwtService.extractEmail(token);

            GuardianUser user = guardianUserRepository
                    .findByEmailIgnoreCase(email)
                    .orElse(null);

            if (user != null && user.isEnabled()) {
                AuthenticatedGuardian principal =
                        new AuthenticatedGuardian(
                                user.getId(),
                                user.getEmail(),
                                user.getRole()
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + user.getRole().name()
                                        )
                                )
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}