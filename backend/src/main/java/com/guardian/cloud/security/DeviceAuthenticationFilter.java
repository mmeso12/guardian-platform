package com.guardian.cloud.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardian.cloud.entity.Device;
import com.guardian.cloud.entity.DeviceStatus;
import com.guardian.cloud.repository.DeviceRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class DeviceAuthenticationFilter extends OncePerRequestFilter {

    public static final String DEVICE_ID_HEADER = "X-Device-ID";
    public static final String DEVICE_KEY_HEADER = "X-Device-Key";

    private final DeviceRepository deviceRepository;
    private final DeviceKeyHasher deviceKeyHasher;
    private final ObjectMapper objectMapper;

    public DeviceAuthenticationFilter(
            DeviceRepository deviceRepository,
            DeviceKeyHasher deviceKeyHasher,
            ObjectMapper objectMapper
    ) {
        this.deviceRepository = deviceRepository;
        this.deviceKeyHasher = deviceKeyHasher;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI()
                .startsWith("/api/v1/device/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String deviceUid = request.getHeader(DEVICE_ID_HEADER);
        String deviceKey = request.getHeader(DEVICE_KEY_HEADER);

        if (deviceUid == null || deviceUid.isBlank()) {
            writeUnauthorized(response, "Missing X-Device-ID header");
            return;
        }

        if (deviceKey == null || deviceKey.isBlank()) {
            writeUnauthorized(response, "Missing X-Device-Key header");
            return;
        }

        Device device = deviceRepository
                .findByDeviceUid(deviceUid)
                .orElse(null);

        if (device == null) {
            writeUnauthorized(response, "Invalid device credentials");
            return;
        }

        if (device.getStatus() == DeviceStatus.DEACTIVATED) {
            writeUnauthorized(response, "Device is deactivated");
            return;
        }

        if (!deviceKeyHasher.matches(
                deviceKey,
                device.getDeviceKeyHash()
        )) {
            writeUnauthorized(response, "Invalid device credentials");
            return;
        }

        AuthenticatedDevice principal = new AuthenticatedDevice(
                device.getId(),
                device.getDeviceUid()
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_DEVICE"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpServletResponse.SC_UNAUTHORIZED,
                "error", "Unauthorized",
                "message", message
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}