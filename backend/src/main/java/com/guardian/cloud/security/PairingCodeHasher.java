package com.guardian.cloud.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class PairingCodeHasher {

    public String hash(String rawPairingCode) {
        if (rawPairingCode == null || rawPairingCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Pairing code cannot be blank"
            );
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    normalize(rawPairingCode)
                            .getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }

    public boolean matches(
            String rawPairingCode,
            String storedHash
    ) {
        if (
                rawPairingCode == null
                || storedHash == null
                || storedHash.isBlank()
        ) {
            return false;
        }

        try {
            byte[] suppliedHash = HexFormat.of()
                    .parseHex(hash(rawPairingCode));

            byte[] expectedHash = HexFormat.of()
                    .parseHex(storedHash);

            return MessageDigest.isEqual(
                    suppliedHash,
                    expectedHash
            );
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String normalize(String pairingCode) {
        return pairingCode
                .trim()
                .toUpperCase();
    }
}