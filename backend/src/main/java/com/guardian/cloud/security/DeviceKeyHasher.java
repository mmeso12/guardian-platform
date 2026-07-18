package com.guardian.cloud.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class DeviceKeyHasher {

    public String hash(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalArgumentException("Device key cannot be blank");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawKey.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }

    public boolean matches(String rawKey, String expectedHash) {
        if (
                rawKey == null ||
                expectedHash == null ||
                expectedHash.isBlank()
        ) {
            return false;
        }

        byte[] suppliedHash = HexFormat.of().parseHex(hash(rawKey));
        byte[] storedHash = HexFormat.of().parseHex(expectedHash);

        return MessageDigest.isEqual(suppliedHash, storedHash);
    }
}