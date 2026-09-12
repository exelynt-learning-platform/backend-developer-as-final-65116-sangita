package com.exelent.booking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SeedSafetyGuard implements ApplicationRunner {

    private final Environment environment;
    private final SeedProperties seedProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!environment.matchesProfiles("prod", "production", "staging", "stage", "uat", "qa")) {
            return;
        }
        if (seedProperties.enabled()) {
            throw new IllegalStateException("Seed data cannot be enabled with a production-adjacent profile");
        }
        if (SeedData.DEMO_ADMIN_PASSWORD.equals(seedProperties.adminPassword())
                || SeedData.DEMO_USER_PASSWORD.equals(seedProperties.userPassword())) {
            throw new IllegalStateException(
                    "Documented demo passwords cannot be used with a production-adjacent profile"
            );
        }
    }
}
