package com.tickethub.infrastructure;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.tickethub.infrastructure.audit.MongoAuditTrail;

public class MongoCleanUpExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(final ExtensionContext context) {
        final var applicationContext = SpringExtension.getApplicationContext(context);
        final var mongoTemplate = applicationContext.getBean(MongoTemplate.class);
        mongoTemplate.dropCollection(MongoAuditTrail.COLLECTION);
    }
}
