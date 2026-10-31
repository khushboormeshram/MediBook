package com.medibook.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DevelopmentSeedPasswordTest {
    private static final String ORIGINAL_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private static final String REPAIRED_HASH =
            "$2a$12$.bWdeFG47b3E8jI2ZxEQ6OeiQW0EJCASnhkuV/rfjORR3IMXE4HAi";

    @Test
    void repairedDevelopmentHashMatchesDocumentedPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        assertFalse(encoder.matches("password", ORIGINAL_HASH));
        assertTrue(encoder.matches("password", REPAIRED_HASH));
    }
}
