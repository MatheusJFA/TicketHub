package com.tickethub.infrastructure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
@ExtendWith(MongoCleanUpExtension.class)
// E2E suites share the cached application context (and its per-IP rate
// limiter); the generous test limit keeps suites hermetic. Production default
// stays strict. AuthRateLimitFilterTest covers the limiting behavior.
@TestPropertySource(properties = "tickethub.auth.login-rate-limit-per-minute=1000")
@Tag("e2eTest")
public @interface E2ETest {
}
