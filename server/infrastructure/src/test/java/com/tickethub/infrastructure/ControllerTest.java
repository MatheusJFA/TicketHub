package com.tickethub.infrastructure;

import com.tickethub.infrastructure.api.controllers.GlobalExceptionHandler;
import com.tickethub.infrastructure.configuration.SecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.TestPropertySource;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@WebMvcTest
@Import({GlobalExceptionHandler.class, SecurityConfiguration.class})
@TestPropertySource(
        properties = {
            "tickethub.security.jwt.secret=test-secret-key-with-at-least-32-bytes!!",
            "tickethub.tickets.signature-secret=test-ticket-signing-secret-0123456789"
        })
@Tag("controllerTest")
public @interface ControllerTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}
